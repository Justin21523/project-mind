package com.justin.projectmind.project.repository;

import com.justin.projectmind.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long>,
        JpaSpecificationExecutor<Project> {

    Optional<Project> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByWorkspaceIdAndNameIgnoreCase(Long workspaceId, String name);
}
