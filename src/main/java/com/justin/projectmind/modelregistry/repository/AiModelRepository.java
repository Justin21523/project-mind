package com.justin.projectmind.modelregistry.repository;

import com.justin.projectmind.modelregistry.entity.AiModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AiModelRepository extends JpaRepository<AiModel, Long>,
        JpaSpecificationExecutor<AiModel> {

    Optional<AiModel> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByOwnerIdAndNameIgnoreCase(Long ownerId, String name);
}
