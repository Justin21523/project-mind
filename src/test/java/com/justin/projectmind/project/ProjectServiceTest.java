package com.justin.projectmind.project;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.enums.Priority;
import com.justin.projectmind.common.exception.ConflictException;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.project.dto.ProjectRequest;
import com.justin.projectmind.project.dto.ProjectResponse;
import com.justin.projectmind.project.entity.Project;
import com.justin.projectmind.project.entity.ProjectStatus;
import com.justin.projectmind.project.mapper.ProjectMapper;
import com.justin.projectmind.project.repository.ProjectRepository;
import com.justin.projectmind.project.service.ProjectService;
import com.justin.projectmind.tag.service.TagResolver;
import com.justin.projectmind.user.entity.User;
import com.justin.projectmind.user.repository.UserRepository;
import com.justin.projectmind.workspace.entity.Workspace;
import com.justin.projectmind.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private UserRepository userRepository;
    @Mock private TagResolver tagResolver;
    @Mock private ProjectMapper projectMapper;
    @Mock private AuditRecorder auditRecorder;

    @InjectMocks private ProjectService projectService;

    private static final Long OWNER_ID = 1L;
    private static final Long WORKSPACE_ID = 5L;

    private ProjectRequest request() {
        return new ProjectRequest(WORKSPACE_ID, "API", "desc", ProjectStatus.PLANNING,
                Priority.HIGH, null, null, null, 0, Set.of(), Set.of());
    }

    private Workspace ownedWorkspace() {
        Workspace ws = new Workspace();
        ws.setId(WORKSPACE_ID);
        return ws;
    }

    @Test
    void create_persistsProject_andRecordsAudit() {
        when(workspaceRepository.findByIdAndOwnerId(WORKSPACE_ID, OWNER_ID)).thenReturn(Optional.of(ownedWorkspace()));
        when(projectRepository.existsByWorkspaceIdAndNameIgnoreCase(WORKSPACE_ID, "API")).thenReturn(false);
        when(userRepository.getReferenceById(OWNER_ID)).thenReturn(new User());
        when(tagResolver.resolveOwned(eq(OWNER_ID), any())).thenReturn(Set.of());
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });
        ProjectResponse expected = new ProjectResponse(10L, WORKSPACE_ID, OWNER_ID, "API", "desc",
                ProjectStatus.PLANNING, Priority.HIGH, null, null, null, 0, Set.of(), Set.of(), null, null);
        when(projectMapper.toResponse(any(Project.class))).thenReturn(expected);

        ProjectResponse result = projectService.create(OWNER_ID, request());

        assertThat(result).isEqualTo(expected);
        verify(projectRepository).save(any(Project.class));
        verify(auditRecorder).record(OWNER_ID, AuditAction.CREATE, "PROJECT", 10L);
    }

    @Test
    void create_throwsConflict_whenNameExistsInWorkspace() {
        when(workspaceRepository.findByIdAndOwnerId(WORKSPACE_ID, OWNER_ID)).thenReturn(Optional.of(ownedWorkspace()));
        when(projectRepository.existsByWorkspaceIdAndNameIgnoreCase(WORKSPACE_ID, "API")).thenReturn(true);

        assertThatThrownBy(() -> projectService.create(OWNER_ID, request()))
                .isInstanceOf(ConflictException.class);

        verify(projectRepository, never()).save(any());
    }

    @Test
    void create_throwsNotFound_whenWorkspaceNotOwned() {
        when(workspaceRepository.findByIdAndOwnerId(WORKSPACE_ID, OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.create(OWNER_ID, request()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(projectRepository, never()).save(any());
    }
}
