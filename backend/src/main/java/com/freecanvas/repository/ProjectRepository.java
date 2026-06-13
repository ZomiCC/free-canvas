package com.freecanvas.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freecanvas.model.entity.Project;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectRepository extends BaseMapper<Project> {
}
