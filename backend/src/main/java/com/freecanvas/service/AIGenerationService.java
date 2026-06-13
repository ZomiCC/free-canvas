package com.freecanvas.service;

import com.freecanvas.model.dto.GenerateRequest;
import com.freecanvas.model.entity.AiTask;

public interface AIGenerationService {
    /** 提交图片生成任务 (Seedream) */
    AiTask generateImage(GenerateRequest request);

    /** 提交视频生成任务 (Seedance 2.0) */
    AiTask generateVideo(GenerateRequest request);

    /** 查询任务状态 */
    AiTask getTaskStatus(Long taskId);

    /** 处理待执行任务（异步） */
    void processPendingTasks();
}
