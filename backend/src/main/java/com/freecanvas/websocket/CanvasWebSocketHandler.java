package com.freecanvas.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 画布 WebSocket 处理器
 * 实现多用户实时协作编辑通知
 */
@Component
public class CanvasWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(CanvasWebSocketHandler.class);

    /** projectId -> sessions 映射，支持按项目广播 */
    private final Map<Long, Set<WebSocketSession>> projectSessions = new ConcurrentHashMap<>();

    /** sessionId -> projectId 反向映射 */
    private final Map<String, Long> sessionProjects = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long projectId = extractProjectId(session);
        if (projectId != null) {
            projectSessions.computeIfAbsent(projectId, k -> ConcurrentHashMap.newKeySet()).add(session);
            sessionProjects.put(session.getId(), projectId);
            log.info("WebSocket 连接: session={}, projectId={}", session.getId(), projectId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long projectId = sessionProjects.get(session.getId());
        if (projectId != null) {
            // 广播消息给同一项目的所有连接
            sendMessageToProject(projectId, message.getPayload());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long projectId = sessionProjects.remove(session.getId());
        if (projectId != null) {
            Set<WebSocketSession> sessions = projectSessions.get(projectId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    projectSessions.remove(projectId);
                }
            }
            log.info("WebSocket 断开: session={}, projectId={}", session.getId(), projectId);
        }
    }

    /** 向指定项目所有连接推送消息 */
    public void sendMessageToProject(Long projectId, String message) {
        Set<WebSocketSession> sessions = projectSessions.get(projectId);
        if (sessions != null) {
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.warn("发送消息失败: session={}", session.getId());
                    }
                }
            }
        }
    }

    private Long extractProjectId(WebSocketSession session) {
        String path = session.getUri() != null ? session.getUri().getPath() : "";
        // /ws/canvas/{projectId}
        String[] parts = path.split("/");
        try {
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            return null;
        }
    }
}
