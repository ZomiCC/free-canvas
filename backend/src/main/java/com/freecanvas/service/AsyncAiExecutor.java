package com.freecanvas.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freecanvas.model.dto.GenerateRequest;
import com.freecanvas.model.entity.AiTask;
import com.freecanvas.repository.AiTaskRepository;
import com.freecanvas.websocket.CanvasWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * AI 任务异步执行器
 * 独立 Service，确保 @Async 通过 Spring AOP 代理正确拦截
 */
@Service
public class AsyncAiExecutor {

    private static final Logger log = LoggerFactory.getLogger(AsyncAiExecutor.class);

    private final AiTaskRepository taskRepository;
    private final CanvasWebSocketHandler wsHandler;
    private final ObjectMapper objectMapper;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.seedream.endpoint}")
    private String seedreamEndpoint;

    @Value("${ai.seedream.model}")
    private String seedreamModel;

    @Value("${ai.seedance.endpoint}")
    private String seedanceEndpoint;

    @Value("${ai.seedance.model}")
    private String seedanceModel;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AsyncAiExecutor(AiTaskRepository taskRepository,
                           CanvasWebSocketHandler wsHandler,
                           ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.wsHandler = wsHandler;
        this.objectMapper = objectMapper;
    }

    @Async("aiTaskExecutor")
    public void executeImageGeneration(AiTask task, GenerateRequest request) {
        try {
            task.setStatus("PROCESSING");
            taskRepository.updateById(task);
            notifyStatus(task, "PROCESSING");

            String reqBody = objectMapper.writeValueAsString(Map.of(
                "model", seedreamModel,
                "prompt", request.getPrompt(),
                "negative_prompt", request.getNegativePrompt() != null ? request.getNegativePrompt() : "",
                "size", request.getWidth() + "x" + request.getHeight(),
                "n", 1
            ));

            HttpRequest httpReq = HttpRequest.newBuilder()
                .uri(URI.create(seedreamEndpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                .build();

            HttpResponse<String> response = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode result = objectMapper.readTree(response.body());
                String imageUrl = extractUrl(result);
                task.setStatus("COMPLETED");
                task.setResultUrl(imageUrl);
                notifyStatus(task, "COMPLETED");
            } else {
                task.setStatus("FAILED");
                task.setErrorMsg("API " + response.statusCode() + ": " + response.body());
                notifyStatus(task, "FAILED");
            }

            taskRepository.updateById(task);

        } catch (Exception e) {
            log.error("图片生成失败: taskId={}", task.getId(), e);
            task.setStatus("FAILED");
            task.setErrorMsg(e.getMessage());
            taskRepository.updateById(task);
            notifyStatus(task, "FAILED");
        }
    }

    @Async("aiTaskExecutor")
    public void executeVideoGeneration(AiTask task, GenerateRequest request) {
        try {
            task.setStatus("PROCESSING");
            taskRepository.updateById(task);
            notifyStatus(task, "PROCESSING");

            String reqBody = objectMapper.writeValueAsString(Map.of(
                "model", seedanceModel,
                "prompt", request.getPrompt(),
                "negative_prompt", request.getNegativePrompt() != null ? request.getNegativePrompt() : "",
                "duration", request.getDuration(),
                "size", request.getWidth() + "x" + request.getHeight()
            ));

            HttpRequest httpReq = HttpRequest.newBuilder()
                .uri(URI.create(seedanceEndpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                .build();

            HttpResponse<String> response = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode result = objectMapper.readTree(response.body());
                String videoUrl = extractUrl(result);
                task.setStatus("COMPLETED");
                task.setResultUrl(videoUrl);
                notifyStatus(task, "COMPLETED");
            } else {
                task.setStatus("FAILED");
                task.setErrorMsg("API " + response.statusCode());
                notifyStatus(task, "FAILED");
            }

            taskRepository.updateById(task);

        } catch (Exception e) {
            log.error("视频生成失败: taskId={}", task.getId(), e);
            task.setStatus("FAILED");
            task.setErrorMsg(e.getMessage());
            taskRepository.updateById(task);
            notifyStatus(task, "FAILED");
        }
    }

    private String extractUrl(JsonNode result) {
        if (result.has("data") && result.get("data").isArray() && result.get("data").size() > 0) {
            JsonNode first = result.get("data").get(0);
            if (first.has("url")) return first.get("url").asText();
        }
        return result.toString();
    }

    private void notifyStatus(AiTask task, String status) {
        try {
            String msg = objectMapper.writeValueAsString(Map.of(
                "type", "AI_TASK_UPDATE",
                "taskId", task.getId(),
                "nodeId", task.getNodeId(),
                "projectId", task.getProjectId(),
                "status", status,
                "resultUrl", task.getResultUrl() != null ? task.getResultUrl() : "",
                "errorMsg", task.getErrorMsg() != null ? task.getErrorMsg() : ""
            ));
            wsHandler.sendMessageToProject(task.getProjectId(), msg);
        } catch (Exception e) {
            log.warn("WebSocket 推送失败: {}", e.getMessage());
        }
    }
}
