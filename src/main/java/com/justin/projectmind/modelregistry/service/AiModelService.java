package com.justin.projectmind.modelregistry.service;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.exception.ConflictException;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.modelregistry.dto.AiModelRequest;
import com.justin.projectmind.modelregistry.dto.AiModelResponse;
import com.justin.projectmind.modelregistry.entity.AiModel;
import com.justin.projectmind.modelregistry.entity.Modality;
import com.justin.projectmind.modelregistry.mapper.AiModelMapper;
import com.justin.projectmind.modelregistry.repository.AiModelRepository;
import com.justin.projectmind.modelregistry.repository.AiModelSpecifications;
import com.justin.projectmind.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Business logic for the AI model registry. Models are scoped per owner (not per
 * workspace) and their names are unique within an owner's registry.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AiModelService {

    private static final String ENTITY_TYPE = "AI_MODEL";

    private final AiModelRepository aiModelRepository;
    private final UserRepository userRepository;
    private final AiModelMapper aiModelMapper;
    private final AuditRecorder auditRecorder;

    public AiModelResponse create(Long ownerId, AiModelRequest request) {
        if (aiModelRepository.existsByOwnerIdAndNameIgnoreCase(ownerId, request.name())) {
            throw new ConflictException("A model with this name already exists in your registry");
        }
        AiModel model = new AiModel();
        model.setOwner(userRepository.getReferenceById(ownerId));
        applyFields(model, request);

        AiModel saved = aiModelRepository.save(model);
        auditRecorder.record(ownerId, AuditAction.CREATE, ENTITY_TYPE, saved.getId());
        return aiModelMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AiModelResponse getById(Long ownerId, Long id) {
        return aiModelMapper.toResponse(requireOwned(ownerId, id));
    }

    @Transactional(readOnly = true)
    public PageResponse<AiModelResponse> list(Long ownerId, Modality modality, String provider,
                                              String search, Pageable pageable) {
        Specification<AiModel> spec = Specification.where(AiModelSpecifications.ownedBy(ownerId));
        if (modality != null) {
            spec = spec.and(AiModelSpecifications.hasModality(modality));
        }
        if (StringUtils.hasText(provider)) {
            spec = spec.and(AiModelSpecifications.hasProvider(provider));
        }
        if (StringUtils.hasText(search)) {
            spec = spec.and(AiModelSpecifications.nameContains(search));
        }
        Page<AiModelResponse> page = aiModelRepository.findAll(spec, pageable).map(aiModelMapper::toResponse);
        return PageResponse.from(page);
    }

    public AiModelResponse update(Long ownerId, Long id, AiModelRequest request) {
        AiModel model = requireOwned(ownerId, id);
        if (!model.getName().equalsIgnoreCase(request.name())
                && aiModelRepository.existsByOwnerIdAndNameIgnoreCase(ownerId, request.name())) {
            throw new ConflictException("A model with this name already exists in your registry");
        }
        applyFields(model, request);
        auditRecorder.record(ownerId, AuditAction.UPDATE, ENTITY_TYPE, model.getId());
        return aiModelMapper.toResponse(model);
    }

    public void delete(Long ownerId, Long id) {
        AiModel model = requireOwned(ownerId, id);
        aiModelRepository.delete(model);
        auditRecorder.record(ownerId, AuditAction.DELETE, ENTITY_TYPE, id);
    }

    private void applyFields(AiModel model, AiModelRequest request) {
        model.setName(request.name());
        model.setProvider(request.provider());
        model.setModality(request.modality());
        model.setFormat(request.format());
        model.setQuantization(request.quantization());
        model.setEstimatedVramMb(request.estimatedVramMb());
        model.setUseCase(request.useCase());
        model.setNotes(request.notes());
    }

    private AiModel requireOwned(Long ownerId, Long id) {
        return aiModelRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("AiModel", id));
    }
}
