package com.justin.projectmind.workspace;

import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.exception.ConflictException;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.user.entity.User;
import com.justin.projectmind.user.repository.UserRepository;
import com.justin.projectmind.workspace.dto.WorkspaceRequest;
import com.justin.projectmind.workspace.dto.WorkspaceResponse;
import com.justin.projectmind.workspace.entity.Workspace;
import com.justin.projectmind.workspace.mapper.WorkspaceMapper;
import com.justin.projectmind.workspace.repository.WorkspaceRepository;
import com.justin.projectmind.workspace.service.WorkspaceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceMapper workspaceMapper;

    @Mock
    private AuditRecorder auditRecorder;

    @InjectMocks
    private WorkspaceService workspaceService;

    private static final Long OWNER_ID = 1L;

    @Test
    void create_persistsWorkspace_whenNameIsUnique() {
        WorkspaceRequest request = new WorkspaceRequest("Learning", "My learning space");
        when(workspaceRepository.existsByOwnerIdAndNameIgnoreCase(OWNER_ID, "Learning")).thenReturn(false);
        when(userRepository.getReferenceById(OWNER_ID)).thenReturn(new User());
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(inv -> inv.getArgument(0));
        WorkspaceResponse expected =
                new WorkspaceResponse(10L, "Learning", "My learning space", OWNER_ID, Instant.now(), Instant.now());
        when(workspaceMapper.toResponse(any(Workspace.class))).thenReturn(expected);

        WorkspaceResponse result = workspaceService.create(OWNER_ID, request);

        assertThat(result).isEqualTo(expected);
        verify(workspaceRepository).save(any(Workspace.class));
    }

    @Test
    void create_throwsConflict_whenNameAlreadyExists() {
        WorkspaceRequest request = new WorkspaceRequest("Learning", null);
        when(workspaceRepository.existsByOwnerIdAndNameIgnoreCase(OWNER_ID, "Learning")).thenReturn(true);

        assertThatThrownBy(() -> workspaceService.create(OWNER_ID, request))
                .isInstanceOf(ConflictException.class);

        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void getById_throwsNotFound_whenWorkspaceMissingOrNotOwned() {
        when(workspaceRepository.findByIdAndOwnerId(99L, OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceService.getById(OWNER_ID, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
