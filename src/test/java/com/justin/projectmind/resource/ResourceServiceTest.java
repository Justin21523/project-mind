package com.justin.projectmind.resource;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.resource.dto.ResourceRequest;
import com.justin.projectmind.resource.entity.Resource;
import com.justin.projectmind.resource.entity.ResourceType;
import com.justin.projectmind.resource.mapper.ResourceMapper;
import com.justin.projectmind.resource.repository.ResourceRepository;
import com.justin.projectmind.resource.service.ResourceService;
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
class ResourceServiceTest {

    @Mock private ResourceRepository resourceRepository;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private UserRepository userRepository;
    @Mock private TagResolver tagResolver;
    @Mock private ResourceMapper resourceMapper;
    @Mock private AuditRecorder auditRecorder;

    @InjectMocks private ResourceService resourceService;

    private static final Long OWNER_ID = 1L;
    private static final Long WORKSPACE_ID = 5L;

    private Workspace workspace() {
        Workspace ws = new Workspace();
        ws.setId(WORKSPACE_ID);
        return ws;
    }

    @Test
    void create_persistsResource_andRecordsAudit() {
        ResourceRequest request = new ResourceRequest(WORKSPACE_ID, "Spring docs",
                "https://docs.spring.io", ResourceType.OFFICIAL_DOCS, "reference", null);
        when(workspaceRepository.findByIdAndOwnerId(WORKSPACE_ID, OWNER_ID)).thenReturn(Optional.of(workspace()));
        when(userRepository.getReferenceById(OWNER_ID)).thenReturn(new User());
        when(tagResolver.resolveOwned(OWNER_ID, null)).thenReturn(java.util.Set.of());
        when(resourceRepository.save(any(Resource.class))).thenAnswer(inv -> {
            Resource r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });
        when(resourceMapper.toResponse(any(Resource.class))).thenReturn(null);

        resourceService.create(OWNER_ID, request);

        ArgumentCaptor<Resource> captor = ArgumentCaptor.forClass(Resource.class);
        verify(resourceRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(ResourceType.OFFICIAL_DOCS);
        assertThat(captor.getValue().getUrl()).isEqualTo("https://docs.spring.io");
        verify(auditRecorder).record(OWNER_ID, AuditAction.CREATE, "RESOURCE", 10L);
    }

    @Test
    void getById_missing_throwsNotFound() {
        when(resourceRepository.findByIdAndOwnerId(99L, OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resourceService.getById(OWNER_ID, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
