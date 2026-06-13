package com.freecanvas.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freecanvas.model.entity.CanvasNode;
import com.freecanvas.model.enums.NodeType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 提示词智能构建器
 * 根据视频节点关联的角色、场景、台词节点，自动拼接复合提示词
 */
@Component
public class PromptBuilder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据视频节点及其关联节点，构建智能提示词
     *
     * @param videoNode 视频节点
     * @param connectedNodes 与该视频节点相连的所有节点
     * @param userPrompt 用户手动输入的提示词（可选，会附加到末尾）
     * @return 完整的提示词
     */
    public String buildVideoPrompt(CanvasNode videoNode,
                                   List<CanvasNode> connectedNodes,
                                   String userPrompt) {
        StringBuilder sb = new StringBuilder();

        // 1. 场景环境（优先，作为画面基础）
        List<CanvasNode> scenes = filterByType(connectedNodes, NodeType.SCENE);
        if (!scenes.isEmpty()) {
            sb.append(buildScenePrompt(scenes));
        }

        // 2. 角色描述
        List<CanvasNode> characters = filterByType(connectedNodes, NodeType.CHARACTER);
        if (!characters.isEmpty()) {
            if (sb.length() > 0) sb.append("，");
            sb.append(buildCharacterPrompt(characters));
        }

        // 3. 台词/剧情内容
        List<CanvasNode> texts = filterByType(connectedNodes, NodeType.TEXT);
        if (!texts.isEmpty()) {
            if (sb.length() > 0) sb.append("，");
            sb.append(buildTextPrompt(texts));
        }

        // 4. 视频节点自身的描述
        Map<String, Object> videoData = parseNodeData(videoNode);
        if (videoData.containsKey("description") && !videoData.get("description").toString().isBlank()) {
            if (sb.length() > 0) sb.append("，");
            sb.append(videoData.get("description"));
        }

        // 5. 用户手动输入的提示词（最高优先级，覆盖调整）
        if (userPrompt != null && !userPrompt.isBlank()) {
            if (sb.length() > 0) sb.append("，");
            sb.append(userPrompt);
        }

        // 6. 质量后缀
        sb.append("，高质量，电影级画质，4K分辨率");

        return sb.toString();
    }

    /**
     * 为 Seedream 图片生成构建提示词
     */
    public String buildImagePrompt(CanvasNode sourceNode,
                                   List<CanvasNode> connectedNodes,
                                   String userPrompt) {
        StringBuilder sb = new StringBuilder();

        // 场景背景
        List<CanvasNode> scenes = filterByType(connectedNodes, NodeType.SCENE);
        if (!scenes.isEmpty()) {
            sb.append(buildScenePrompt(scenes));
        }

        // 角色
        List<CanvasNode> characters = filterByType(connectedNodes, NodeType.CHARACTER);
        if (!characters.isEmpty()) {
            if (sb.length() > 0) sb.append("，");
            sb.append(buildCharacterPrompt(characters));
        }

        // 用户输入
        if (userPrompt != null && !userPrompt.isBlank()) {
            if (sb.length() > 0) sb.append("，");
            sb.append(userPrompt);
        }

        sb.append("，高质量插画，精美细节，电影光影");

        return sb.toString();
    }

    // ==================== 内部方法 ====================

    private String buildScenePrompt(List<CanvasNode> scenes) {
        return scenes.stream()
                .map(n -> {
                    Map<String, Object> d = parseNodeData(n);
                    String name = getStr(d, "name");
                    String location = getStr(d, "location");
                    String timeOfDay = getStr(d, "timeOfDay");
                    String atmosphere = getStr(d, "atmosphere");
                    String weather = getStr(d, "weather");
                    String desc = getStr(d, "description");

                    StringBuilder s = new StringBuilder();
                    s.append("场景");
                    if (!name.isBlank()) s.append("「").append(name).append("」");
                    if (!location.isBlank()) s.append("在").append(location);
                    if (!timeOfDay.isBlank()) s.append(timeOfDay);
                    if (!weather.isBlank() && !"晴".equals(weather)) s.append(weather).append("天");
                    if (!atmosphere.isBlank()) s.append(atmosphere).append("氛围");
                    if (!desc.isBlank()) s.append("(").append(desc).append(")");
                    return s.toString();
                })
                .collect(Collectors.joining("；"));
    }

    private String buildCharacterPrompt(List<CanvasNode> characters) {
        return characters.stream()
                .map(n -> {
                    Map<String, Object> d = parseNodeData(n);
                    String name = getStr(d, "name");
                    String gender = getStr(d, "gender");
                    String age = d.containsKey("age") ? d.get("age").toString() : "";
                    String personality = getStr(d, "personality");
                    String desc = getStr(d, "description");

                    StringBuilder s = new StringBuilder();
                    s.append("角色");
                    if (!name.isBlank()) s.append("「").append(name).append("」");
                    if (!gender.isBlank()) s.append(gender).append("性");
                    if (!age.isBlank() && !"0".equals(age)) s.append(age).append("岁");
                    if (!personality.isBlank()) s.append(personality);
                    if (!desc.isBlank()) s.append("(").append(desc).append(")");
                    return s.toString();
                })
                .collect(Collectors.joining("；"));
    }

    private String buildTextPrompt(List<CanvasNode> texts) {
        return texts.stream()
                .map(n -> {
                    Map<String, Object> d = parseNodeData(n);
                    String speaker = getStr(d, "speaker");
                    String content = getStr(d, "content");
                    String emotion = getStr(d, "emotion");

                    StringBuilder s = new StringBuilder();
                    if (!speaker.isBlank()) s.append(speaker);
                    if (!emotion.isBlank() && !"平静".equals(emotion)) s.append(emotion).append("地说");
                    if (!content.isBlank()) {
                        // 截取前 100 字避免提示词过长
                        String shortContent = content.length() > 100 ? content.substring(0, 100) + "..." : content;
                        s.append("「").append(shortContent).append("」");
                    }
                    return s.toString();
                })
                .collect(Collectors.joining("；"));
    }

    // ==================== 工具方法 ====================

    private List<CanvasNode> filterByType(List<CanvasNode> nodes, NodeType type) {
        return nodes.stream()
                .filter(n -> n.getNodeType() == type)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseNodeData(CanvasNode node) {
        try {
            if (node.getNodeData() != null) {
                return objectMapper.readValue(node.getNodeData(), Map.class);
            }
        } catch (Exception ignored) {}
        return Map.of();
    }

    private String getStr(Map<String, Object> data, String key) {
        Object val = data.get(key);
        return val != null ? val.toString() : "";
    }
}
