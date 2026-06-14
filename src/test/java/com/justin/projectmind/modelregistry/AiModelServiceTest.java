package com.justin.projectmind.modelregistry;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.exception.ConflictException;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.modelregistry.dto.AiModelRequest;
import com.justin.projectmind.modelregistry.entity.AiModel;
import com.justin.projectmind.modelregistry.entity.Modality;
import com.justin.projectmind.modelregistry.entity.ModelFormat;
import com.justin.projectmind.modelregistry.mapper.AiModelMapper;
import com.justin.projectmind.modelregistry.repository.AiModelRepository;
import com.justin.projectmind.modelregistry.service.AiModelService;
import com.justin.projectmind.user.entity.User;
import com.justin.projectmind.user.repository.UserRepository;
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
class AiModelServiceTest {

    @Mock private AiModelRepository aiModelRepository;
    @Mock private UserRepository userRepository;
    @Mock private AiModelMapper aiModelMapper;
    @Mock private AuditRecorder auditRecorder;

    @InjectMocks private AiModelService aiModelService;

    private static final Long OWNER_ID = 1L;

    private AiModelRequest request(String name) {
        return new AiModelRequest(name, "ollama", Modality.TEXT, ModelFormat.GGUF,
                "Q4_K_M", 8192, "local chat", "notes");
    }

    @Test
    void create_persistsModel_andRecordsAudit() {
        when(aiModelRepository.existsByOwnerIdAndNameIgnoreCase(OWNER_ID, "llama3")).thenReturn(false);
        when(userRepository.getReferenceById(OWNER_ID)).thenReturn(new User());
        when(aiModelRepository.save(any(AiModel.class))).thenAnswer(inv -> {
            AiModel m = inv.getArgument(0);
            m.setId(10L);
            return m;
        });
        when(aiModelMapper.toResponse(any(AiModel.class))).thenReturn(null);

        aiModelService.create(OWNER_ID, request("llama3"));

        ArgumentCaptor<AiModel> captor = ArgumentCaptor.forClass(AiModel.class);
        verify(aiModelRepository).save(captor.capture());
        assertThat(captor.getValue().getModality()).isEqualTo(Modality.TEXT);
        verify(auditRecorder).record(OWNER_ID, AuditAction.CREATE, "AI_MODEL", 10L);
    }

    @Test
    void create_duplicateName_throwsConflict() {
        when(aiModelRepository.existsByOwnerIdAndNameIgnoreCase(OWNER_ID, "llama3")).thenReturn(true);

        assertThatThrownBy(() -> aiModelService.create(OWNER_ID, request("llama3")))
                .isInstanceOf(ConflictException.class);

        verify(aiModelRepository, never()).save(any());
    }

    @Test
    void update_toExistingName_throwsConflict() {
        AiModel existing = new AiModel();
        existing.setId(10L);
        existing.setName("Old");
        when(aiModelRepository.findByIdAndOwnerId(10L, OWNER_ID)).thenReturn(Optional.of(existing));
        when(aiModelRepository.existsByOwnerIdAndNameIgnoreCase(OWNER_ID, "llama3")).thenReturn(true);

        assertThatThrownBy(() -> aiModelService.update(OWNER_ID, 10L, request("llama3")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void getById_missing_throwsNotFound() {
        when(aiModelRepository.findByIdAndOwnerId(99L, OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiModelService.getById(OWNER_ID, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
