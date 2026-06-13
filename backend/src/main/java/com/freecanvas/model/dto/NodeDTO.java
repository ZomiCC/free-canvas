package com.freecanvas.model.dto;

import com.freecanvas.model.enums.NodeType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NodeDTO {
    private Long id;

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "节点类型不能为空")
    private NodeType nodeType;

    private String label;

    @NotNull(message = "X坐标不能为空")
    private Double posX;

    @NotNull(message = "Y坐标不能为空")
    private Double posY;

    private Double width = 200.0;
    private Double height = 150.0;

    /** 节点自定义数据，JSON 字符串 */
    private String nodeData;

    /** 节点样式，JSON 字符串 */
    private String style;
}
