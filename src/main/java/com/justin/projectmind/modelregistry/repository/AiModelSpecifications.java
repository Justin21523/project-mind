package com.justin.projectmind.modelregistry.repository;

import com.justin.projectmind.modelregistry.entity.AiModel;
import com.justin.projectmind.modelregistry.entity.Modality;
import org.springframework.data.jpa.domain.Specification;

public final class AiModelSpecifications {

    private AiModelSpecifications() {
    }

    public static Specification<AiModel> ownedBy(Long ownerId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<AiModel> hasModality(Modality modality) {
        return (root, query, cb) -> cb.equal(root.get("modality"), modality);
    }

    public static Specification<AiModel> hasProvider(String provider) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("provider")), provider.toLowerCase());
    }

    public static Specification<AiModel> nameContains(String term) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + term.toLowerCase() + "%");
    }
}
