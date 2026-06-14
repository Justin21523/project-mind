package com.justin.projectmind.prompt.repository;

import com.justin.projectmind.prompt.entity.Prompt;
import org.springframework.data.jpa.domain.Specification;

public final class PromptSpecifications {

    private PromptSpecifications() {
    }

    public static Specification<Prompt> ownedBy(Long ownerId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<Prompt> inWorkspace(Long workspaceId) {
        return (root, query, cb) -> cb.equal(root.get("workspace").get("id"), workspaceId);
    }

    public static Specification<Prompt> hasTargetModel(String targetModel) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("targetModel")), targetModel.toLowerCase());
    }

    public static Specification<Prompt> titleContains(String term) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + term.toLowerCase() + "%");
    }
}
