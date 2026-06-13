package com.freecanvas.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freecanvas.model.dto.GenerateRequest;
import com.freecanvas.model.entity.AiTask;
import com.freecanvas.repository.AiTaskRepository;
import com.freecanvas.service.AIGenerationService;
import com.freecanvas.service.AsyncAiExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AIGenerationServiceImpl implements AIGenerationService {

    private static final Logger log = LoggerFactory.getLogger(AIGenerationServiceImpl.class);

    private final AiTaskRepository taskRepository;
    private final AsyncAiExecutor asyncExecutor;
    private final ObjectMapper objectMapper;

    public AIGenerationServiceImpl(AiTaskRepository taskRepository,
                                   AsyncAiExecutor asyncExecutor,
                                   ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.asyncExecutor = asyncExecutor;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public AiTask generateImage(GenerateRequest request) {
        AiTask task = buildTask(request, "IMAGE");
        taskRepository.insert(task);
        // 委托给独立 Service，确保 @Async 通过 AOP 代理正确拦截
        asyncExecutor.executeImageGeneration(task, request);
        return task;
    }

    @Override
    @Transactional
    public AiTask generateVideo(GenerateRequest request) {
        AiTask task = buildTask(request, "VIDEO");
        taskRepository.insert(task);
        asyncExecutor.executeVideoGeneration(task, request);
        return task;
    }

    @Override
    public AiTask getTaskStatus(Long taskId) {
        return taskRepository.selectById(taskId);
    }

    @Override
    public void processPendingTasks() {
        List<AiTask> pendingTasks = taskRepository.findByStatus("PENDING");
        for (AiTask task : pendingTasks) {
            task.setStatus("PROCESSING");
            taskRepository.updateById(task);
        }
    }

    private AiTask buildTask(GenerateRequest request, String type) {
        AiTask task = new AiTask();
        task.setProjectId(request.getProjectId());
        task.setNodeId(request.getNodeId());
        task.setTaskType(type);
        task.setPrompt(request.getPrompt());
        task.setStatus("PENDING");
        try {
            task.setParams(objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            task.setParams("{}");
        }
        return task;
    }
}
