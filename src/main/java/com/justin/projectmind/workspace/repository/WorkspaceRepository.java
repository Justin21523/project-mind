package com.justin.projectmind.workspace.repository;

import com.justin.projectmind.workspace.entity.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Optional<Workspace> findByIdAndOwnerId(Long id, Long ownerId);

    Page<Workspace> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Workspace> findByOwnerIdAndNameContainingIgnoreCase(Long ownerId, String name, Pageable pageable);

    boolean existsByOwnerIdAndNameIgnoreCase(Long ownerId, String name);
}
