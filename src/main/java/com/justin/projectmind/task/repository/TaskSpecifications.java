package com.justin.projectmind.task.repository;

import com.justin.projectmind.common.enums.Priority;
import com.justin.projectmind.task.entity.Task;
import com.justin.projectmind.task.entity.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable {@link Specification} fragments for dynamic task filtering.
 */
public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> ownedBy(Long ownerId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<Task> inWorkspace(Long workspaceId) {
        return (root, query, cb) -> cb.equal(root.get("workspace").get("id"), workspaceId);
    }

    public static Specification<Task> inProject(Long projectId) {
        return (root, query, cb) -> cb.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(Priority priority) {
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> titleContains(String term) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + term.toLowerCase() + "%");
    }
}
