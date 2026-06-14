package com.justin.projectmind.prompt;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.prompt.dto.PromptRequest;
import com.justin.projectmind.prompt.entity.Prompt;
import com.justin.projectmind.prompt.mapper.PromptMapper;
import com.justin.projectmind.prompt.repository.PromptRepository;
import com.justin.projectmind.prompt.service.PromptService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptServiceTest {

    @Mock private PromptRepository promptRepository;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private UserRepository userRepository;
    @Mock private TagResolver tagResolver;
    @Mock private PromptMapper promptMapper;
    @Mock private AuditRecorder auditRecorder;

    @InjectMocks private PromptService promptService;

    private static final Long OWNER_ID = 1L;
    private static final Long WORKSPACE_ID = 5L;

    private Workspace workspace() {
        Workspace ws = new Workspace();
        ws.setId(WORKSPACE_ID);
        return ws;
    }

    private PromptRequest request() {
        return new PromptRequest(WORKSPACE_ID, "Summarize", "Summarize the following: {{input}}",
                "llm_provider-opus-4-8", "summarization", 5, "notes", null);
    }

    @Test
    void create_setsEditorialVersionToOne_andRecordsAudit() {
        when(workspaceRepository.findByIdAndOwnerId(WORKSPACE_ID, OWNER_ID)).thenReturn(Optional.of(workspace()));
        when(userRepository.getReferenceById(OWNER_ID)).thenReturn(new User());
        when(tagResolver.resolveOwned(OWNER_ID, null)).thenReturn(java.util.Set.of());
        when(promptRepository.save(any(Prompt.class))).thenAnswer(inv -> {
            Prompt p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });
        when(promptMapper.toResponse(any(Prompt.class))).thenReturn(null);

        promptService.create(OWNER_ID, request());

        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(promptRepository).save(captor.capture());
        assertThat(captor.getValue().getPromptVersion()).isEqualTo(1);
        verify(auditRecorder).record(OWNER_ID, AuditAction.CREATE, "PROMPT", 10L);
    }

    @Test
    void update_incrementsEditorialVersion_andRecordsAudit() {
        Prompt existing = new Prompt();
        existing.setId(10L);
        existing.setPromptVersion(1);
        when(promptRepository.findByIdAndOwnerId(10L, OWNER_ID)).thenReturn(Optional.of(existing));
        when(workspaceRepository.findByIdAndOwnerId(WORKSPACE_ID, OWNER_ID)).thenReturn(Optional.of(workspace()));
        when(tagResolver.resolveOwned(OWNER_ID, null)).thenReturn(java.util.Set.of());
        when(promptMapper.toResponse(any(Prompt.class))).thenReturn(null);

        promptService.update(OWNER_ID, 10L, request());

        assertThat(existing.getPromptVersion()).isEqualTo(2);
        verify(auditRecorder).record(OWNER_ID, AuditAction.UPDATE, "PROMPT", 10L);
    }

    @Test
    void getById_missing_throwsNotFound() {
        when(promptRepository.findByIdAndOwnerId(99L, OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promptService.getById(OWNER_ID, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
