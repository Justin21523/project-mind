package com.justin.projectmind.resource.repository;

import com.justin.projectmind.resource.entity.Resource;
import com.justin.projectmind.resource.entity.ResourceType;
import org.springframework.data.jpa.domain.Specification;

public final class ResourceSpecifications {

    private ResourceSpecifications() {
    }

    public static Specification<Resource> ownedBy(Long ownerId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<Resource> inWorkspace(Long workspaceId) {
        return (root, query, cb) -> cb.equal(root.get("workspace").get("id"), workspaceId);
    }

    public static Specification<Resource> hasType(ResourceType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Resource> titleContains(String term) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + term.toLowerCase() + "%");
    }
}
