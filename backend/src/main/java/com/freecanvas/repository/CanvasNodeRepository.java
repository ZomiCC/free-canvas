package com.freecanvas.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freecanvas.model.entity.CanvasNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CanvasNodeRepository extends BaseMapper<CanvasNode> {

    @Select("SELECT * FROM canvas_node WHERE project_id = #{projectId} AND deleted = 0")
    List<CanvasNode> findByProjectId(Long projectId);
}
