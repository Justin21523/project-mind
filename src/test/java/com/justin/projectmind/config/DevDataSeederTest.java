package com.justin.projectmind.config;

import com.justin.projectmind.modelregistry.repository.AiModelRepository;
import com.justin.projectmind.note.repository.NoteRepository;
import com.justin.projectmind.project.repository.ProjectRepository;
import com.justin.projectmind.prompt.repository.PromptRepository;
import com.justin.projectmind.resource.repository.ResourceRepository;
import com.justin.projectmind.tag.entity.Tag;
import com.justin.projectmind.tag.repository.TagRepository;
import com.justin.projectmind.task.repository.TaskRepository;
import com.justin.projectmind.user.entity.User;
import com.justin.projectmind.user.repository.UserRepository;
import com.justin.projectmind.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevDataSeederTest {

    @Mock private UserRepository userRepository;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private TagRepository tagRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private NoteRepository noteRepository;
    @Mock private PromptRepository promptRepository;
    @Mock private ResourceRepository resourceRepository;
    @Mock private AiModelRepository aiModelRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private DevDataSeeder seeder;

    @Test
    void run_skips_whenAdminAlreadyExists() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        seeder.run();

        verify(userRepository, never()).save(any());
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void run_seeds_whenAdminAbsent() {
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

        seeder.run();

        verify(userRepository, times(2)).save(any(User.class)); // admin + demo
        verify(passwordEncoder, times(2)).encode(anyString());
        verify(workspaceRepository).save(any());
        verify(aiModelRepository, times(2)).save(any());
    }
}
