package com.freecanvas.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freecanvas.model.entity.AiTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AiTaskRepository extends BaseMapper<AiTask> {

    @Select("SELECT * FROM ai_task WHERE project_id = #{projectId} ORDER BY created_at DESC")
    List<AiTask> findByProjectId(Long projectId);

    @Select("SELECT * FROM ai_task WHERE status = #{status} ORDER BY created_at ASC")
    List<AiTask> findByStatus(String status);
}
