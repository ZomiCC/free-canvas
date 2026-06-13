package com.freecanvas.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateRequest {
    @NotBlank(message = "提示词不能为空")
    private String prompt;

    private Long projectId;
    private Long nodeId;

    /** 负向提示词（可选） */
    private String negativePrompt;

    /** 生成图片: 宽高; 生成视频: 时长(秒) */
    private Integer width = 1024;
    private Integer height = 1024;
    private Integer duration = 5;

    /** 风格 */
    private String style;
}
