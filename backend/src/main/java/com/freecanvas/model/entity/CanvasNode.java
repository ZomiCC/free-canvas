package com.freecanvas.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.freecanvas.model.enums.NodeType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor @AllArgsConstructor
@TableName("canvas_node")
public class CanvasNode {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /** 节点类型 */
    private NodeType nodeType;

    /** 显示标签 */
    private String label;

    /** X 坐标 */
    private Double posX;

    /** Y 坐标 */
    private Double posY;

    private Double width;
    private Double height;

    /** 节点自定义数据，JSON 格式 */
    private String nodeData;

    /** 节点样式，JSON 格式 */
    private String style;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
