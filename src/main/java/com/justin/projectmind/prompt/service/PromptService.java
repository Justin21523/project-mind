package com.justin.projectmind.prompt.service;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.prompt.dto.PromptRequest;
import com.justin.projectmind.prompt.dto.PromptResponse;
import com.justin.projectmind.prompt.entity.Prompt;
import com.justin.projectmind.prompt.mapper.PromptMapper;
import com.justin.projectmind.prompt.repository.PromptRepository;
import com.justin.projectmind.prompt.repository.PromptSpecifications;
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
public class PromptService {

    private static final String ENTITY_TYPE = "PROMPT";

    private final PromptRepository promptRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final TagResolver tagResolver;
    private final PromptMapper promptMapper;
    private final AuditRecorder auditRecorder;

    public PromptResponse create(Long ownerId, PromptRequest request) {
        Workspace workspace = requireOwnedWorkspace(ownerId, request.workspaceId());

        Prompt prompt = new Prompt();
        prompt.setOwner(userRepository.getReferenceById(ownerId));
        prompt.setWorkspace(workspace);
        prompt.setPromptVersion(1);
        applyFields(ownerId, prompt, request);

        Prompt saved = promptRepository.save(prompt);
        auditRecorder.record(ownerId, AuditAction.CREATE, ENTITY_TYPE, saved.getId());
        return promptMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PromptResponse getById(Long ownerId, Long id) {
        return promptMapper.toResponse(requireOwned(ownerId, id));
    }

    @Transactional(readOnly = true)
    public PageResponse<PromptResponse> list(Long ownerId, Long workspaceId, String targetModel,
                                             String search, Pageable pageable) {
        Specification<Prompt> spec = Specification.where(PromptSpecifications.ownedBy(ownerId));
        if (workspaceId != null) {
            spec = spec.and(PromptSpecifications.inWorkspace(workspaceId));
        }
        if (StringUtils.hasText(targetModel)) {
            spec = spec.and(PromptSpecifications.hasTargetModel(targetModel));
        }
        if (StringUtils.hasText(search)) {
            spec = spec.and(PromptSpecifications.titleContains(search));
        }
        Page<PromptResponse> page = promptRepository.findAll(spec, pageable).map(promptMapper::toResponse);
        return PageResponse.from(page);
    }

    public PromptResponse update(Long ownerId, Long id, PromptRequest request) {
        Prompt prompt = requireOwned(ownerId, id);
        prompt.setWorkspace(requireOwnedWorkspace(ownerId, request.workspaceId()));
        // Each edit bumps the editorial version so prompt revisions are visible.
        prompt.setPromptVersion(prompt.getPromptVersion() + 1);
        applyFields(ownerId, prompt, request);
        auditRecorder.record(ownerId, AuditAction.UPDATE, ENTITY_TYPE, prompt.getId());
        return promptMapper.toResponse(prompt);
    }

    public void delete(Long ownerId, Long id) {
        Prompt prompt = requireOwned(ownerId, id);
        promptRepository.delete(prompt);
        auditRecorder.record(ownerId, AuditAction.DELETE, ENTITY_TYPE, id);
    }

    private void applyFields(Long ownerId, Prompt prompt, PromptRequest request) {
        prompt.setTitle(request.title());
        prompt.setContent(request.content());
        prompt.setTargetModel(request.targetModel());
        prompt.setTaskType(request.taskType());
        prompt.setRating(request.rating());
        prompt.setNotes(request.notes());
        prompt.setTags(tagResolver.resolveOwned(ownerId, request.tagIds()));
    }

    private Prompt requireOwned(Long ownerId, Long id) {
        return promptRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Prompt", id));
    }

    private Workspace requireOwnedWorkspace(Long ownerId, Long workspaceId) {
        return workspaceRepository.findByIdAndOwnerId(workspaceId, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Workspace", workspaceId));
    }
}
