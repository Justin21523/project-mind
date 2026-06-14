package com.justin.projectmind.project.repository;

import com.justin.projectmind.project.entity.Project;
import com.justin.projectmind.project.entity.ProjectStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable {@link Specification} fragments for dynamic project filtering.
 */
public final class ProjectSpecifications {

    private ProjectSpecifications() {
    }

    public static Specification<Project> ownedBy(Long ownerId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<Project> inWorkspace(Long workspaceId) {
        return (root, query, cb) -> cb.equal(root.get("workspace").get("id"), workspaceId);
    }

    public static Specification<Project> hasStatus(ProjectStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Project> nameContains(String term) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + term.toLowerCase() + "%");
    }
}
