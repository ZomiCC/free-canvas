package com.freecanvas.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freecanvas.model.entity.NodeConnection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface NodeConnectionRepository extends BaseMapper<NodeConnection> {

    @Select("SELECT * FROM node_connection WHERE project_id = #{projectId} AND deleted = 0")
    List<NodeConnection> findByProjectId(Long projectId);
}
