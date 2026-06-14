package com.justin.projectmind.workspace.service;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.exception.ConflictException;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.user.entity.User;
import com.justin.projectmind.user.repository.UserRepository;
import com.justin.projectmind.workspace.dto.WorkspaceRequest;
import com.justin.projectmind.workspace.dto.WorkspaceResponse;
import com.justin.projectmind.workspace.entity.Workspace;
import com.justin.projectmind.workspace.mapper.WorkspaceMapper;
import com.justin.projectmind.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Business logic for workspaces. Every operation is scoped to the owning user so
 * a caller can never read or mutate another user's data.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceService {

    private static final String ENTITY_TYPE = "WORKSPACE";

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final WorkspaceMapper workspaceMapper;
    private final AuditRecorder auditRecorder;

    public WorkspaceResponse create(Long ownerId, WorkspaceRequest request) {
        if (workspaceRepository.existsByOwnerIdAndNameIgnoreCase(ownerId, request.name())) {
            throw new ConflictException("A workspace with this name already exists");
        }
        User owner = userRepository.getReferenceById(ownerId);

        Workspace workspace = new Workspace();
        workspace.setName(request.name());
        workspace.setDescription(request.description());
        workspace.setOwner(owner);

        Workspace saved = workspaceRepository.save(workspace);
        auditRecorder.record(ownerId, AuditAction.CREATE, ENTITY_TYPE, saved.getId());
        return workspaceMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getById(Long ownerId, Long id) {
        return workspaceMapper.toResponse(getOwnedOrThrow(ownerId, id));
    }

    @Transactional(readOnly = true)
    public PageResponse<WorkspaceResponse> list(Long ownerId, String search, Pageable pageable) {
        Page<Workspace> page = StringUtils.hasText(search)
                ? workspaceRepository.findByOwnerIdAndNameContainingIgnoreCase(ownerId, search, pageable)
                : workspaceRepository.findByOwnerId(ownerId, pageable);
        return PageResponse.from(page.map(workspaceMapper::toResponse));
    }

    public WorkspaceResponse update(Long ownerId, Long id, WorkspaceRequest request) {
        Workspace workspace = getOwnedOrThrow(ownerId, id);
        if (!workspace.getName().equalsIgnoreCase(request.name())
                && workspaceRepository.existsByOwnerIdAndNameIgnoreCase(ownerId, request.name())) {
            throw new ConflictException("A workspace with this name already exists");
        }
        workspace.setName(request.name());
        workspace.setDescription(request.description());
        auditRecorder.record(ownerId, AuditAction.UPDATE, ENTITY_TYPE, workspace.getId());
        return workspaceMapper.toResponse(workspace);
    }

    public void delete(Long ownerId, Long id) {
        Workspace workspace = getOwnedOrThrow(ownerId, id);
        workspaceRepository.delete(workspace);
        auditRecorder.record(ownerId, AuditAction.DELETE, ENTITY_TYPE, id);
    }

    private Workspace getOwnedOrThrow(Long ownerId, Long id) {
        return workspaceRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Workspace", id));
    }
}
