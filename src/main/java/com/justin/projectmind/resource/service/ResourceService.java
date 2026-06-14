package com.justin.projectmind.resource.service;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.resource.dto.ResourceRequest;
import com.justin.projectmind.resource.dto.ResourceResponse;
import com.justin.projectmind.resource.entity.Resource;
import com.justin.projectmind.resource.entity.ResourceType;
import com.justin.projectmind.resource.mapper.ResourceMapper;
import com.justin.projectmind.resource.repository.ResourceRepository;
import com.justin.projectmind.resource.repository.ResourceSpecifications;
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

@Service
@RequiredArgsConstructor
@Transactional
public class ResourceService {

    private static final String ENTITY_TYPE = "RESOURCE";

    private final ResourceRepository resourceRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final TagResolver tagResolver;
    private final ResourceMapper resourceMapper;
    private final AuditRecorder auditRecorder;

    public ResourceResponse create(Long ownerId, ResourceRequest request) {
        Workspace workspace = requireOwnedWorkspace(ownerId, request.workspaceId());

        Resource resource = new Resource();
        resource.setOwner(userRepository.getReferenceById(ownerId));
        resource.setWorkspace(workspace);
        applyFields(ownerId, resource, request);

        Resource saved = resourceRepository.save(resource);
        auditRecorder.record(ownerId, AuditAction.CREATE, ENTITY_TYPE, saved.getId());
        return resourceMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ResourceResponse getById(Long ownerId, Long id) {
        return resourceMapper.toResponse(requireOwned(ownerId, id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ResourceResponse> list(Long ownerId, Long workspaceId, ResourceType type,
                                               String search, Pageable pageable) {
        Specification<Resource> spec = Specification.where(ResourceSpecifications.ownedBy(ownerId));
        if (workspaceId != null) {
            spec = spec.and(ResourceSpecifications.inWorkspace(workspaceId));
        }
        if (type != null) {
            spec = spec.and(ResourceSpecifications.hasType(type));
        }
        if (StringUtils.hasText(search)) {
            spec = spec.and(ResourceSpecifications.titleContains(search));
        }
        Page<ResourceResponse> page = resourceRepository.findAll(spec, pageable).map(resourceMapper::toResponse);
        return PageResponse.from(page);
    }

    public ResourceResponse update(Long ownerId, Long id, ResourceRequest request) {
        Resource resource = requireOwned(ownerId, id);
        resource.setWorkspace(requireOwnedWorkspace(ownerId, request.workspaceId()));
        applyFields(ownerId, resource, request);
        auditRecorder.record(ownerId, AuditAction.UPDATE, ENTITY_TYPE, resource.getId());
        return resourceMapper.toResponse(resource);
    }

    public void delete(Long ownerId, Long id) {
        Resource resource = requireOwned(ownerId, id);
        resourceRepository.delete(resource);
        auditRecorder.record(ownerId, AuditAction.DELETE, ENTITY_TYPE, id);
    }

    private void applyFields(Long ownerId, Resource resource, ResourceRequest request) {
        resource.setTitle(request.title());
        resource.setUrl(request.url());
        resource.setType(request.type());
        resource.setDescription(request.description());
        resource.setTags(tagResolver.resolveOwned(ownerId, request.tagIds()));
    }

    private Resource requireOwned(Long ownerId, Long id) {
        return resourceRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Resource", id));
    }

    private Workspace requireOwnedWorkspace(Long ownerId, Long workspaceId) {
        return workspaceRepository.findByIdAndOwnerId(workspaceId, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Workspace", workspaceId));
    }
}
