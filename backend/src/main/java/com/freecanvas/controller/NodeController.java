package com.freecanvas.controller;

import com.freecanvas.model.dto.ConnectionDTO;
import com.freecanvas.model.dto.NodeDTO;
import com.freecanvas.model.entity.CanvasNode;
import com.freecanvas.model.entity.NodeConnection;
import com.freecanvas.service.NodeService;
import com.freecanvas.websocket.CanvasWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}")
@CrossOrigin(origins = "*")
public class NodeController {

    private final NodeService nodeService;
    private final CanvasWebSocketHandler wsHandler;
    private final ObjectMapper objectMapper;

    public NodeController(NodeService nodeService,
                          CanvasWebSocketHandler wsHandler,
                          ObjectMapper objectMapper) {
        this.nodeService = nodeService;
        this.wsHandler = wsHandler;
        this.objectMapper = objectMapper;
    }

    // ==================== 节点 ====================

    /** 获取项目所有节点 */
    @GetMapping("/nodes")
    public ResponseEntity<List<CanvasNode>> getNodes(@PathVariable Long projectId) {
        return ResponseEntity.ok(nodeService.getProjectNodes(projectId));
    }

    /** 创建节点 */
    @PostMapping("/nodes")
    public ResponseEntity<CanvasNode> createNode(@PathVariable Long projectId,
                                                  @Valid @RequestBody NodeDTO dto) {
        dto.setProjectId(projectId);
        CanvasNode node = nodeService.createNode(dto);
        notifyCanvasUpdate(projectId, "NODE_CREATED", node);
        return ResponseEntity.ok(node);
    }

    /** 更新节点 */
    @PutMapping("/nodes/{nodeId}")
    public ResponseEntity<CanvasNode> updateNode(@PathVariable Long projectId,
                                                  @PathVariable Long nodeId,
                                                  @Valid @RequestBody NodeDTO dto) {
        CanvasNode node = nodeService.updateNode(nodeId, dto);
        notifyCanvasUpdate(projectId, "NODE_UPDATED", node);
        return ResponseEntity.ok(node);
    }

    /** 删除节点 */
    @DeleteMapping("/nodes/{nodeId}")
    public ResponseEntity<Void> deleteNode(@PathVariable Long projectId,
                                           @PathVariable Long nodeId) {
        nodeService.deleteNode(nodeId);
        notifyCanvasUpdate(projectId, "NODE_DELETED", Map.of("nodeId", nodeId));
        return ResponseEntity.noContent().build();
    }

    // ==================== 连接 ====================

    /** 获取项目所有连接 */
    @GetMapping("/connections")
    public ResponseEntity<List<NodeConnection>> getConnections(@PathVariable Long projectId) {
        return ResponseEntity.ok(nodeService.getProjectConnections(projectId));
    }

    /** 创建连接 */
    @PostMapping("/connections")
    public ResponseEntity<NodeConnection> createConnection(@PathVariable Long projectId,
                                                            @Valid @RequestBody ConnectionDTO dto) {
        dto.setProjectId(projectId);
        NodeConnection conn = nodeService.createConnection(dto);
        notifyCanvasUpdate(projectId, "CONN_CREATED", conn);
        return ResponseEntity.ok(conn);
    }

    /** 删除连接 */
    @DeleteMapping("/connections/{connId}")
    public ResponseEntity<Void> deleteConnection(@PathVariable Long projectId,
                                                  @PathVariable Long connId) {
        nodeService.deleteConnection(connId);
        notifyCanvasUpdate(projectId, "CONN_DELETED", Map.of("connectionId", connId));
        return ResponseEntity.noContent().build();
    }

    // ==================== 内部 ====================

    private void notifyCanvasUpdate(Long projectId, String type, Object data) {
        try {
            String msg = objectMapper.writeValueAsString(Map.of(
                "type", type,
                "projectId", projectId,
                "data", data
            ));
            wsHandler.sendMessageToProject(projectId, msg);
        } catch (Exception ignored) {}
    }
}
