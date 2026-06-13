package com.freecanvas.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor @AllArgsConstructor
@TableName("ai_task")
public class AiTask {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;
    private Long nodeId;

    /** 任务类型: IMAGE / VIDEO */
    private String taskType;

    /** 生成提示词 */
    private String prompt;

    /** 生成参数 JSON */
    private String params;

    /** 状态 */
    private String status;

    /** 生成结果 URL */
    private String resultUrl;

    /** 错误信息 */
    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
