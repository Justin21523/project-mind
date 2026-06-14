package com.justin.projectmind.project.service;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.exception.ConflictException;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.project.dto.ProjectRequest;
import com.justin.projectmind.project.dto.ProjectResponse;
import com.justin.projectmind.project.entity.Project;
import com.justin.projectmind.project.entity.ProjectStatus;
import com.justin.projectmind.project.mapper.ProjectMapper;
import com.justin.projectmind.project.repository.ProjectRepository;
import com.justin.projectmind.project.repository.ProjectSpecifications;
import com.justin.projectmind.tag.service.TagResolver;
import com.justin.projectmind.user.repository.UserRepository;
import com.justin.projectmind.workspace.entity.Workspace;
import com.justin.projectmind.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;

/**
 * Business logic for projects. Validates that the target workspace and any referenced
 * tags belong to the caller before mutating, and emits audit events.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private static final String ENTITY_TYPE = "PROJECT";

    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final TagResolver tagResolver;
    private final ProjectMapper projectMapper;
    private final AuditRecorder auditRecorder;

    public ProjectResponse create(Long ownerId, ProjectRequest request) {
        Workspace workspace = requireOwnedWorkspace(ownerId, request.workspaceId());
        if (projectRepository.existsByWorkspaceIdAndNameIgnoreCase(workspace.getId(), request.name())) {
            throw new ConflictException("A project with this name already exists in the workspace");
        }

        Project project = new Project();
        project.setOwner(userRepository.getReferenceById(ownerId));
        project.setWorkspace(workspace);
        applyFields(ownerId, project, request);

        Project saved = projectRepository.save(project);
        auditRecorder.record(ownerId, AuditAction.CREATE, ENTITY_TYPE, saved.getId());
        return projectMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getById(Long ownerId, Long id) {
        return projectMapper.toResponse(requireOwned(ownerId, id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> list(Long ownerId, Long workspaceId, ProjectStatus status,
                                              String search, Pageable pageable) {
        Specification<Project> spec = Specification.where(ProjectSpecifications.ownedBy(ownerId));
        if (workspaceId != null) {
            spec = spec.and(ProjectSpecifications.inWorkspace(workspaceId));
        }
        if (status != null) {
            spec = spec.and(ProjectSpecifications.hasStatus(status));
        }
        if (StringUtils.hasText(search)) {
            spec = spec.and(ProjectSpecifications.nameContains(search));
        }
        Page<ProjectResponse> page = projectRepository.findAll(spec, pageable).map(projectMapper::toResponse);
        return PageResponse.from(page);
    }

    public ProjectResponse update(Long ownerId, Long id, ProjectRequest request) {
        Project project = requireOwned(ownerId, id);
        Workspace workspace = requireOwnedWorkspace(ownerId, request.workspaceId());

        boolean nameOrWorkspaceChanged = !project.getName().equalsIgnoreCase(request.name())
                || !project.getWorkspace().getId().equals(workspace.getId());
        if (nameOrWorkspaceChanged
                && projectRepository.existsByWorkspaceIdAndNameIgnoreCase(workspace.getId(), request.name())) {
            throw new ConflictException("A project with this name already exists in the workspace");
        }

        project.setWorkspace(workspace);
        applyFields(ownerId, project, request);

        auditRecorder.record(ownerId, AuditAction.UPDATE, ENTITY_TYPE, project.getId());
        return projectMapper.toResponse(project);
    }

    public void delete(Long ownerId, Long id) {
        Project project = requireOwned(ownerId, id);
        projectRepository.delete(project);
        auditRecorder.record(ownerId, AuditAction.DELETE, ENTITY_TYPE, id);
    }

    private void applyFields(Long ownerId, Project project, ProjectRequest request) {
        project.setName(request.name());
        project.setDescription(request.description());
        project.setStatus(request.status());
        project.setPriority(request.priority());
        project.setRepositoryUrl(request.repositoryUrl());
        project.setStartDate(request.startDate());
        project.setTargetDate(request.targetDate());
        project.setProgress(request.progress());
        project.setTechStack(request.techStack() == null ? new HashSet<>() : new HashSet<>(request.techStack()));
        project.setTags(tagResolver.resolveOwned(ownerId, request.tagIds()));
    }

    private Project requireOwned(Long ownerId, Long id) {
        return projectRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Project", id));
    }

    private Workspace requireOwnedWorkspace(Long ownerId, Long workspaceId) {
        return workspaceRepository.findByIdAndOwnerId(workspaceId, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Workspace", workspaceId));
    }
}
