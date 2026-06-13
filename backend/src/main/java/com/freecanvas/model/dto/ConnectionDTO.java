package com.freecanvas.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConnectionDTO {
    private Long id;

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "源节点ID不能为空")
    private Long sourceNodeId;

    @NotNull(message = "目标节点ID不能为空")
    private Long targetNodeId;

    private String sourceHandle;
    private String targetHandle;
    private String label;
    private String style;
}
