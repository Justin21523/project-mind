package com.justin.projectmind.note.repository;

import com.justin.projectmind.note.entity.Note;
import com.justin.projectmind.note.entity.NoteType;
import org.springframework.data.jpa.domain.Specification;

public final class NoteSpecifications {

    private NoteSpecifications() {
    }

    public static Specification<Note> ownedBy(Long ownerId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<Note> inWorkspace(Long workspaceId) {
        return (root, query, cb) -> cb.equal(root.get("workspace").get("id"), workspaceId);
    }

    public static Specification<Note> hasType(NoteType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Note> titleContains(String term) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + term.toLowerCase() + "%");
    }
}
