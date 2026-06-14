package com.justin.projectmind.prompt.repository;

import com.justin.projectmind.prompt.entity.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PromptRepository extends JpaRepository<Prompt, Long>,
        JpaSpecificationExecutor<Prompt> {

    Optional<Prompt> findByIdAndOwnerId(Long id, Long ownerId);
}
