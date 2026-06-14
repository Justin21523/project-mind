package com.justin.projectmind.tag.service;

import com.justin.projectmind.common.exception.BadRequestException;
import com.justin.projectmind.tag.entity.Tag;
import com.justin.projectmind.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves a set of tag ids into managed {@link Tag} entities, enforcing that every id
 * exists and belongs to the given owner. Shared by all taggable feature services so the
 * ownership rule lives in one place.
 */
@Component
@RequiredArgsConstructor
public class TagResolver {

    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public Set<Tag> resolveOwned(Long ownerId, Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Tag> found = tagRepository.findAllByIdInAndOwnerId(tagIds, ownerId);
        if (found.size() != tagIds.size()) {
            throw new BadRequestException("One or more tags do not exist or do not belong to you");
        }
        return new HashSet<>(found);
    }
}
