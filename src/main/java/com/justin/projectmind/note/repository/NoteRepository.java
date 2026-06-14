package com.justin.projectmind.note.repository;

import com.justin.projectmind.note.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long>,
        JpaSpecificationExecutor<Note> {

    Optional<Note> findByIdAndOwnerId(Long id, Long ownerId);
}
