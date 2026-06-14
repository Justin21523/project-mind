package com.justin.projectmind.tag.service;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.exception.ConflictException;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.tag.dto.TagRequest;
import com.justin.projectmind.tag.dto.TagResponse;
import com.justin.projectmind.tag.entity.Tag;
import com.justin.projectmind.tag.mapper.TagMapper;
import com.justin.projectmind.tag.repository.TagRepository;
import com.justin.projectmind.user.entity.User;
import com.justin.projectmind.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Business logic for tags. Single-tag reads are cached in Redis, keyed by owner and id
 * so cached entries are never shared across users; writes evict the affected entry.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private static final String CACHE_NAME = "tags";

    private static final String ENTITY_TYPE = "TAG";

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final TagMapper tagMapper;
    private final AuditRecorder auditRecorder;

    public TagResponse create(Long ownerId, TagRequest request) {
        if (tagRepository.existsByOwnerIdAndNameIgnoreCase(ownerId, request.name())) {
            throw new ConflictException("A tag with this name already exists");
        }
        User owner = userRepository.getReferenceById(ownerId);

        Tag tag = new Tag();
        tag.setName(request.name());
        tag.setColor(request.color());
        tag.setOwner(owner);

        Tag saved = tagRepository.save(tag);
        auditRecorder.record(ownerId, AuditAction.CREATE, ENTITY_TYPE, saved.getId());
        return tagMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_NAME, key = "#ownerId + ':' + #id")
    public TagResponse getById(Long ownerId, Long id) {
        return tagMapper.toResponse(getOwnedOrThrow(ownerId, id));
    }

    @Transactional(readOnly = true)
    public PageResponse<TagResponse> list(Long ownerId, String search, Pageable pageable) {
        Page<Tag> page = StringUtils.hasText(search)
                ? tagRepository.findByOwnerIdAndNameContainingIgnoreCase(ownerId, search, pageable)
                : tagRepository.findByOwnerId(ownerId, pageable);
        return PageResponse.from(page.map(tagMapper::toResponse));
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#ownerId + ':' + #id")
    public TagResponse update(Long ownerId, Long id, TagRequest request) {
        Tag tag = getOwnedOrThrow(ownerId, id);
        if (!tag.getName().equalsIgnoreCase(request.name())
                && tagRepository.existsByOwnerIdAndNameIgnoreCase(ownerId, request.name())) {
            throw new ConflictException("A tag with this name already exists");
        }
        tag.setName(request.name());
        tag.setColor(request.color());
        auditRecorder.record(ownerId, AuditAction.UPDATE, ENTITY_TYPE, tag.getId());
        return tagMapper.toResponse(tag);
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#ownerId + ':' + #id")
    public void delete(Long ownerId, Long id) {
        Tag tag = getOwnedOrThrow(ownerId, id);
        tagRepository.delete(tag);
        auditRecorder.record(ownerId, AuditAction.DELETE, ENTITY_TYPE, id);
    }

    private Tag getOwnedOrThrow(Long ownerId, Long id) {
        return tagRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Tag", id));
    }
}
