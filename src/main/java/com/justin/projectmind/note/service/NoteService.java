package com.justin.projectmind.note.service;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.note.dto.NoteRequest;
import com.justin.projectmind.note.dto.NoteResponse;
import com.justin.projectmind.note.entity.Note;
import com.justin.projectmind.note.entity.NoteType;
import com.justin.projectmind.note.mapper.NoteMapper;
import com.justin.projectmind.note.repository.NoteRepository;
import com.justin.projectmind.note.repository.NoteSpecifications;
import com.justin.projectmind.tag.service.TagResolver;
import com.justin.projectmind.user.repository.UserRepository;
import com.justin.projectmind.workspace.entity.Workspace;
import com.justin.projectmind.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class NoteService {

    private static final String ENTITY_TYPE = "NOTE";

    private final NoteRepository noteRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final TagResolver tagResolver;
    private final NoteMapper noteMapper;
    private final AuditRecorder auditRecorder;

    public NoteResponse create(Long ownerId, NoteRequest request) {
        Workspace workspace = requireOwnedWorkspace(ownerId, request.workspaceId());

        Note note = new Note();
        note.setOwner(userRepository.getReferenceById(ownerId));
        note.setWorkspace(workspace);
        applyFields(ownerId, note, request);

        Note saved = noteRepository.save(note);
        auditRecorder.record(ownerId, AuditAction.CREATE, ENTITY_TYPE, saved.getId());
        return noteMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public NoteResponse getById(Long ownerId, Long id) {
        return noteMapper.toResponse(requireOwned(ownerId, id));
    }

    @Transactional(readOnly = true)
    public PageResponse<NoteResponse> list(Long ownerId, Long workspaceId, NoteType type,
                                           String search, Pageable pageable) {
        Specification<Note> spec = Specification.where(NoteSpecifications.ownedBy(ownerId));
        if (workspaceId != null) {
            spec = spec.and(NoteSpecifications.inWorkspace(workspaceId));
        }
        if (type != null) {
            spec = spec.and(NoteSpecifications.hasType(type));
        }
        if (StringUtils.hasText(search)) {
            spec = spec.and(NoteSpecifications.titleContains(search));
        }
        Page<NoteResponse> page = noteRepository.findAll(spec, pageable).map(noteMapper::toResponse);
        return PageResponse.from(page);
    }

    public NoteResponse update(Long ownerId, Long id, NoteRequest request) {
        Note note = requireOwned(ownerId, id);
        note.setWorkspace(requireOwnedWorkspace(ownerId, request.workspaceId()));
        applyFields(ownerId, note, request);
        auditRecorder.record(ownerId, AuditAction.UPDATE, ENTITY_TYPE, note.getId());
        return noteMapper.toResponse(note);
    }

    public void delete(Long ownerId, Long id) {
        Note note = requireOwned(ownerId, id);
        noteRepository.delete(note);
        auditRecorder.record(ownerId, AuditAction.DELETE, ENTITY_TYPE, id);
    }

    private void applyFields(Long ownerId, Note note, NoteRequest request) {
        note.setTitle(request.title());
        note.setContent(request.content());
        note.setType(request.type());
        note.setTags(tagResolver.resolveOwned(ownerId, request.tagIds()));
    }

    private Note requireOwned(Long ownerId, Long id) {
        return noteRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Note", id));
    }

    private Workspace requireOwnedWorkspace(Long ownerId, Long workspaceId) {
        return workspaceRepository.findByIdAndOwnerId(workspaceId, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Workspace", workspaceId));
    }
}
