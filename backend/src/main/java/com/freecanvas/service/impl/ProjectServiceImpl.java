package com.freecanvas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.freecanvas.model.dto.ProjectDTO;
import com.freecanvas.model.entity.Project;
import com.freecanvas.repository.ProjectRepository;
import com.freecanvas.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public List<Project> listAll() {
        return projectRepository.selectList(
            new LambdaQueryWrapper<Project>().orderByDesc(Project::getUpdatedAt)
        );
    }

    @Override
    public Project getById(Long id) {
        Project project = projectRepository.selectById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在: " + id);
        }
        return project;
    }

    @Override
    @Transactional
    public Project create(ProjectDTO dto) {
        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setThumbnail(dto.getThumbnail());
        projectRepository.insert(project);
        return project;
    }

    @Override
    @Transactional
    public Project update(Long id, ProjectDTO dto) {
        Project project = getById(id);
        if (dto.getName() != null) project.setName(dto.getName());
        if (dto.getDescription() != null) project.setDescription(dto.getDescription());
        if (dto.getThumbnail() != null) project.setThumbnail(dto.getThumbnail());
        projectRepository.updateById(project);
        return project;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        projectRepository.deleteById(id);
    }
}
