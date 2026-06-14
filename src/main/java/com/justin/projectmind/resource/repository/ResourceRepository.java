package com.justin.projectmind.resource.repository;

import com.justin.projectmind.resource.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ResourceRepository extends JpaRepository<Resource, Long>,
        JpaSpecificationExecutor<Resource> {

    Optional<Resource> findByIdAndOwnerId(Long id, Long ownerId);
}
