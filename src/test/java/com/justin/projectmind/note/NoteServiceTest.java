package com.justin.projectmind.note;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.note.dto.NoteRequest;
import com.justin.projectmind.note.entity.Note;
import com.justin.projectmind.note.entity.NoteType;
import com.justin.projectmind.note.mapper.NoteMapper;
import com.justin.projectmind.note.repository.NoteRepository;
import com.justin.projectmind.note.service.NoteService;
import com.justin.projectmind.tag.service.TagResolver;
import com.justin.projectmind.user.entity.User;
import com.justin.projectmind.user.repository.UserRepository;
import com.justin.projectmind.workspace.entity.Workspace;
import com.justin.projectmind.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock private NoteRepository noteRepository;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private UserRepository userRepository;
    @Mock private TagResolver tagResolver;
    @Mock private NoteMapper noteMapper;
    @Mock private AuditRecorder auditRecorder;

    @InjectMocks private NoteService noteService;

    private static final Long OWNER_ID = 1L;
    private static final Long WORKSPACE_ID = 5L;

    private Workspace workspace() {
        Workspace ws = new Workspace();
        ws.setId(WORKSPACE_ID);
        return ws;
    }

    @Test
    void create_persistsNoteWithResolvedTags_andRecordsAudit() {
        NoteRequest request = new NoteRequest(WORKSPACE_ID, "JPA notes", "# content",
                NoteType.CONCEPT, Set.of(7L));
        when(workspaceRepository.findByIdAndOwnerId(WORKSPACE_ID, OWNER_ID)).thenReturn(Optional.of(workspace()));
        when(userRepository.getReferenceById(OWNER_ID)).thenReturn(new User());
        when(tagResolver.resolveOwned(eq(OWNER_ID), eq(Set.of(7L)))).thenReturn(Set.of());
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> {
            Note n = inv.getArgument(0);
            n.setId(10L);
            return n;
        });
        when(noteMapper.toResponse(any(Note.class))).thenReturn(null);

        noteService.create(OWNER_ID, request);

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NoteType.CONCEPT);
        verify(tagResolver).resolveOwned(OWNER_ID, Set.of(7L));
        verify(auditRecorder).record(OWNER_ID, AuditAction.CREATE, "NOTE", 10L);
    }

    @Test
    void getById_missing_throwsNotFound() {
        when(noteRepository.findByIdAndOwnerId(99L, OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.getById(OWNER_ID, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
