package com.justin.projectmind.tag.repository;

import com.justin.projectmind.tag.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByIdAndOwnerId(Long id, Long ownerId);

    List<Tag> findAllByIdInAndOwnerId(Set<Long> ids, Long ownerId);

    Page<Tag> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Tag> findByOwnerIdAndNameContainingIgnoreCase(Long ownerId, String name, Pageable pageable);

    boolean existsByOwnerIdAndNameIgnoreCase(Long ownerId, String name);
}
