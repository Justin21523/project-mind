package com.justin.projectmind.task.service;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.enums.Priority;
import com.justin.projectmind.common.exception.BadRequestException;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.project.entity.Project;
import com.justin.projectmind.project.repository.ProjectRepository;
import com.justin.projectmind.task.dto.TaskRequest;
import com.justin.projectmind.task.dto.TaskResponse;
import com.justin.projectmind.task.entity.Task;
import com.justin.projectmind.task.entity.TaskStatus;
import com.justin.projectmind.task.mapper.TaskMapper;
import com.justin.projectmind.task.repository.TaskRepository;
import com.justin.projectmind.task.repository.TaskSpecifications;
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

/**
 * Business logic for tasks. A task always belongs to an owned workspace and may
 * optionally belong to a project within that same workspace.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private static final String ENTITY_TYPE = "TASK";

    private final TaskRepository taskRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final AuditRecorder auditRecorder;

    public TaskResponse create(Long ownerId, TaskRequest request) {
        Workspace workspace = requireOwnedWorkspace(ownerId, request.workspaceId());
        Project project = resolveProject(ownerId, request.projectId(), workspace);

        Task task = new Task();
        task.setOwner(userRepository.getReferenceById(ownerId));
        applyFields(task, request, workspace, project);

        Task saved = taskRepository.save(task);
        auditRecorder.record(ownerId, AuditAction.CREATE, ENTITY_TYPE, saved.getId());
        return taskMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long ownerId, Long id) {
        return taskMapper.toResponse(requireOwned(ownerId, id));
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> list(Long ownerId, Long workspaceId, Long projectId,
                                           TaskStatus status, Priority priority, String search,
                                           Pageable pageable) {
        Specification<Task> spec = Specification.where(TaskSpecifications.ownedBy(ownerId));
        if (workspaceId != null) {
            spec = spec.and(TaskSpecifications.inWorkspace(workspaceId));
        }
        if (projectId != null) {
            spec = spec.and(TaskSpecifications.inProject(projectId));
        }
        if (status != null) {
            spec = spec.and(TaskSpecifications.hasStatus(status));
        }
        if (priority != null) {
            spec = spec.and(TaskSpecifications.hasPriority(priority));
        }
        if (StringUtils.hasText(search)) {
            spec = spec.and(TaskSpecifications.titleContains(search));
        }
        Page<TaskResponse> page = taskRepository.findAll(spec, pageable).map(taskMapper::toResponse);
        return PageResponse.from(page);
    }

    public TaskResponse update(Long ownerId, Long id, TaskRequest request) {
        Task task = requireOwned(ownerId, id);
        Workspace workspace = requireOwnedWorkspace(ownerId, request.workspaceId());
        Project project = resolveProject(ownerId, request.projectId(), workspace);

        applyFields(task, request, workspace, project);
        auditRecorder.record(ownerId, AuditAction.UPDATE, ENTITY_TYPE, task.getId());
        return taskMapper.toResponse(task);
    }

    public void delete(Long ownerId, Long id) {
        Task task = requireOwned(ownerId, id);
        taskRepository.delete(task);
        auditRecorder.record(ownerId, AuditAction.DELETE, ENTITY_TYPE, id);
    }

    private void applyFields(Task task, TaskRequest request, Workspace workspace, Project project) {
        task.setWorkspace(workspace);
        task.setProject(project);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
    }

    private Project resolveProject(Long ownerId, Long projectId, Workspace workspace) {
        if (projectId == null) {
            return null;
        }
        Project project = projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Project", projectId));
        if (!project.getWorkspace().getId().equals(workspace.getId())) {
            throw new BadRequestException("Project does not belong to the given workspace");
        }
        return project;
    }

    private Task requireOwned(Long ownerId, Long id) {
        return taskRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Task", id));
    }

    private Workspace requireOwnedWorkspace(Long ownerId, Long workspaceId) {
        return workspaceRepository.findByIdAndOwnerId(workspaceId, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Workspace", workspaceId));
    }
}
