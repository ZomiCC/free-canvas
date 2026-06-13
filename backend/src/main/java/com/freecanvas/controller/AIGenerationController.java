package com.freecanvas.controller;

import com.freecanvas.model.dto.GenerateRequest;
import com.freecanvas.model.entity.AiTask;
import com.freecanvas.model.entity.CanvasNode;
import com.freecanvas.service.AIGenerationService;
import com.freecanvas.service.NodeService;
import com.freecanvas.service.PromptBuilder;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIGenerationController {

    private final AIGenerationService aiService;
    private final NodeService nodeService;
    private final PromptBuilder promptBuilder;

    public AIGenerationController(AIGenerationService aiService,
                                  NodeService nodeService,
                                  PromptBuilder promptBuilder) {
        this.aiService = aiService;
        this.nodeService = nodeService;
        this.promptBuilder = promptBuilder;
    }

    /** 生成图片 — 调用豆包 Seedream 模型 */
    @PostMapping("/generate-image")
    public ResponseEntity<AiTask> generateImage(@Valid @RequestBody GenerateRequest request) {
        return ResponseEntity.ok(aiService.generateImage(request));
    }

    /** 生成视频 — 调用豆包 Seedance 2.0 模型 */
    @PostMapping("/generate-video")
    public ResponseEntity<AiTask> generateVideo(@Valid @RequestBody GenerateRequest request) {
        return ResponseEntity.ok(aiService.generateVideo(request));
    }

    /**
     * 智能生成视频 — 自动拼接关联节点数据构建提示词
     * 根据视频节点连接的场景/角色/台词节点，自动生成复合提示词
     */
    @PostMapping("/smart-generate-video")
    public ResponseEntity<Map<String, Object>> smartGenerateVideo(@RequestBody Map<String, Object> body) {
        Long nodeId = Long.valueOf(body.get("nodeId").toString());
        Long projectId = Long.valueOf(body.get("projectId").toString());
        String userPrompt = Objects.toString(body.get("prompt"), "");

        // 获取视频节点
        CanvasNode videoNode = null;
        List<CanvasNode> projectNodes = nodeService.getProjectNodes(projectId);
        for (CanvasNode n : projectNodes) {
            if (n.getId().equals(nodeId)) {
                videoNode = n;
                break;
            }
        }
        if (videoNode == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "节点不存在"));
        }

        // 获取关联节点
        List<CanvasNode> connectedNodes = nodeService.getConnectedNodes(nodeId);

        // 构建智能提示词
        String fullPrompt = promptBuilder.buildVideoPrompt(videoNode, connectedNodes, userPrompt);

        // 提交生成任务
        GenerateRequest request = new GenerateRequest();
        request.setPrompt(fullPrompt);
        request.setProjectId(projectId);
        request.setNodeId(nodeId);
        request.setWidth(body.containsKey("width") ? Integer.valueOf(body.get("width").toString()) : 1920);
        request.setHeight(body.containsKey("height") ? Integer.valueOf(body.get("height").toString()) : 1080);
        request.setDuration(body.containsKey("duration") ? Integer.valueOf(body.get("duration").toString()) : 5);

        AiTask task = aiService.generateVideo(request);

        return ResponseEntity.ok(Map.of(
            "task", task,
            "fullPrompt", fullPrompt,
            "connectedNodeCount", connectedNodes.size(),
            "connectedNodes", connectedNodes.stream().map(n -> Map.of(
                "id", n.getId(),
                "type", n.getNodeType().name(),
                "label", n.getLabel()
            )).toList()
        ));
    }

    /** 预览智能提示词（不提交生成） */
    @GetMapping("/preview-prompt/{projectId}/{nodeId}")
    public ResponseEntity<Map<String, Object>> previewPrompt(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @RequestParam(required = false) String prompt) {

        CanvasNode videoNode = null;
        List<CanvasNode> projectNodes = nodeService.getProjectNodes(projectId);
        for (CanvasNode n : projectNodes) {
            if (n.getId().equals(nodeId)) {
                videoNode = n;
                break;
            }
        }
        if (videoNode == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "节点不存在"));
        }

        List<CanvasNode> connectedNodes = nodeService.getConnectedNodes(nodeId);
        String fullPrompt = promptBuilder.buildVideoPrompt(videoNode, connectedNodes,
                prompt != null ? prompt : "");

        return ResponseEntity.ok(Map.of(
            "fullPrompt", fullPrompt,
            "connectedNodeCount", connectedNodes.size(),
            "connectedNodes", connectedNodes.stream().map(n -> Map.of(
                "id", n.getId(),
                "type", n.getNodeType().name(),
                "label", n.getLabel()
            )).toList()
        ));
    }

    /** 查询任务状态 */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<AiTask> getTaskStatus(@PathVariable Long taskId) {
        return ResponseEntity.ok(aiService.getTaskStatus(taskId));
    }

    /** 健康检测 */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "models", Map.of(
                "image", "doubao-seedream-3-0-t2i-250415",
                "video", "doubao-seedance-2-0-t2v-250428"
            )
        ));
    }
}
