package com.justin.projectmind.task.repository;

import com.justin.projectmind.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>,
        JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndOwnerId(Long id, Long ownerId);
}
