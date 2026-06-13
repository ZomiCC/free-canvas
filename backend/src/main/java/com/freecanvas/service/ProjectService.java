package com.freecanvas.service;

import com.freecanvas.model.dto.ProjectDTO;
import com.freecanvas.model.entity.Project;
import java.util.List;

public interface ProjectService {
    List<Project> listAll();
    Project getById(Long id);
    Project create(ProjectDTO dto);
    Project update(Long id, ProjectDTO dto);
    void delete(Long id);
}
