package com.freecanvas.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor @AllArgsConstructor
@TableName("node_connection")
public class NodeConnection {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /** 源节点 ID */
    private Long sourceNodeId;

    /** 目标节点 ID */
    private Long targetNodeId;

    /** 源连接点标识 */
    private String sourceHandle;

    /** 目标连接点标识 */
    private String targetHandle;

    /** 连接线标签 */
    private String label;

    /** 连接线样式，JSON */
    private String style;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
