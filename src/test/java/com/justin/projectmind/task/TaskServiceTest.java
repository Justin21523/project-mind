package com.justin.projectmind.task;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.enums.Priority;
import com.justin.projectmind.common.exception.BadRequestException;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.project.entity.Project;
import com.justin.projectmind.project.repository.ProjectRepository;
import com.justin.projectmind.task.dto.TaskRequest;
import com.justin.projectmind.task.entity.Task;
import com.justin.projectmind.task.entity.TaskStatus;
import com.justin.projectmind.task.mapper.TaskMapper;
import com.justin.projectmind.task.repository.TaskRepository;
import com.justin.projectmind.task.service.TaskService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskMapper taskMapper;
    @Mock private AuditRecorder auditRecorder;

    @InjectMocks private TaskService taskService;

    private static final Long OWNER_ID = 1L;
    private static final Long WORKSPACE_ID = 5L;

    private Workspace workspace(Long id) {
        Workspace ws = new Workspace();
        ws.setId(id);
        return ws;
    }

    private TaskRequest request(Long projectId) {
        return new TaskRequest(WORKSPACE_ID, projectId, "Write tests", "desc",
                TaskStatus.TODO, Priority.MEDIUM, null);
    }

    @Test
    void create_standaloneTask_persistsAndRecordsAudit() {
        when(workspaceRepository.findByIdAndOwnerId(WORKSPACE_ID, OWNER_ID))
                .thenReturn(Optional.of(workspace(WORKSPACE_ID)));
        when(userRepository.getReferenceById(OWNER_ID)).thenReturn(new User());
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });
        when(taskMapper.toResponse(any(Task.class))).thenReturn(null);

        taskService.create(OWNER_ID, request(null));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        assertThat(captor.getValue().getProject()).isNull();
        assertThat(captor.getValue().getStatus()).isEqualTo(TaskStatus.TODO);
        verify(auditRecorder).record(OWNER_ID, AuditAction.CREATE, "TASK", 10L);
    }

    @Test
    void create_withProjectInDifferentWorkspace_throwsBadRequest() {
        when(workspaceRepository.findByIdAndOwnerId(WORKSPACE_ID, OWNER_ID))
                .thenReturn(Optional.of(workspace(WORKSPACE_ID)));
        Project foreignProject = new Project();
        foreignProject.setWorkspace(workspace(99L));
        when(projectRepository.findByIdAndOwnerId(7L, OWNER_ID)).thenReturn(Optional.of(foreignProject));

        assertThatThrownBy(() -> taskService.create(OWNER_ID, request(7L)))
                .isInstanceOf(BadRequestException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    void create_withUnownedWorkspace_throwsNotFound() {
        when(workspaceRepository.findByIdAndOwnerId(WORKSPACE_ID, OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(OWNER_ID, request(null)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    void getById_missing_throwsNotFound() {
        when(taskRepository.findByIdAndOwnerId(99L, OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getById(OWNER_ID, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
