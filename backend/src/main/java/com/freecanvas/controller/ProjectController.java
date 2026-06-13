package com.freecanvas.controller;

import com.freecanvas.model.dto.ProjectDTO;
import com.freecanvas.model.entity.Project;
import com.freecanvas.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /** 获取所有项目 */
    @GetMapping
    public ResponseEntity<List<Project>> listAll() {
        return ResponseEntity.ok(projectService.listAll());
    }

    /** 获取单个项目 */
    @GetMapping("/{id}")
    public ResponseEntity<Project> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getById(id));
    }

    /** 创建项目 */
    @PostMapping
    public ResponseEntity<Project> create(@Valid @RequestBody ProjectDTO dto) {
        return ResponseEntity.ok(projectService.create(dto));
    }

    /** 更新项目 */
    @PutMapping("/{id}")
    public ResponseEntity<Project> update(@PathVariable Long id,
                                           @Valid @RequestBody ProjectDTO dto) {
        return ResponseEntity.ok(projectService.update(id, dto));
    }

    /** 删除项目 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
