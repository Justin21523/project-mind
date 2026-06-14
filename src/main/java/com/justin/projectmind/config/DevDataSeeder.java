package com.justin.projectmind.config;

import com.justin.projectmind.common.enums.Priority;
import com.justin.projectmind.modelregistry.entity.AiModel;
import com.justin.projectmind.modelregistry.entity.Modality;
import com.justin.projectmind.modelregistry.entity.ModelFormat;
import com.justin.projectmind.modelregistry.repository.AiModelRepository;
import com.justin.projectmind.note.entity.Note;
import com.justin.projectmind.note.entity.NoteType;
import com.justin.projectmind.note.repository.NoteRepository;
import com.justin.projectmind.project.entity.Project;
import com.justin.projectmind.project.entity.ProjectStatus;
import com.justin.projectmind.project.repository.ProjectRepository;
import com.justin.projectmind.prompt.entity.Prompt;
import com.justin.projectmind.prompt.repository.PromptRepository;
import com.justin.projectmind.resource.entity.Resource;
import com.justin.projectmind.resource.entity.ResourceType;
import com.justin.projectmind.resource.repository.ResourceRepository;
import com.justin.projectmind.tag.entity.Tag;
import com.justin.projectmind.tag.repository.TagRepository;
import com.justin.projectmind.task.entity.Task;
import com.justin.projectmind.task.entity.TaskStatus;
import com.justin.projectmind.task.repository.TaskRepository;
import com.justin.projectmind.user.entity.Role;
import com.justin.projectmind.user.entity.User;
import com.justin.projectmind.user.repository.UserRepository;
import com.justin.projectmind.workspace.entity.Workspace;
import com.justin.projectmind.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

/**
 * Seeds demo data when running under the {@code dev} profile so the API is immediately
 * usable (and Swagger has something to show). Idempotent: it does nothing once the demo
 * admin account exists, so app restarts never duplicate data. Never active in test/prod.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final TagRepository tagRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final NoteRepository noteRepository;
    private final PromptRepository promptRepository;
    private final ResourceRepository resourceRepository;
    private final AiModelRepository aiModelRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByUsername("admin")) {
            return;
        }
        log.info("Seeding dev data (admin/demo accounts and sample content)...");

        createUser("admin", "admin@projectmind.local", "admin12345", "Demo Admin", Role.USER, Role.ADMIN);
        User demo = createUser("demo", "demo@projectmind.local", "demo12345", "Demo User", Role.USER);

        Workspace workspace = saveWorkspace(demo, "Learning Lab", "Personal R&D workspace");

        Tag java = saveTag(demo, "java", "#E76F00");
        Tag spring = saveTag(demo, "spring", "#6DB33F");
        Tag ai = saveTag(demo, "ai", "#10A37F");

        Project api = saveProject(demo, workspace, "ProjectMind API",
                "Personal knowledge & project operations backend",
                ProjectStatus.IN_PROGRESS, Priority.HIGH, 60,
                Set.of("Java", "Spring Boot", "PostgreSQL"), Set.of(java, spring));
        saveProject(demo, workspace, "Local LLM Playground",
                "Experiments with local models", ProjectStatus.PLANNING, Priority.MEDIUM, 10,
                Set.of("Python", "Ollama"), Set.of(ai));

        saveTask(demo, workspace, api, "Add RBAC admin endpoints", TaskStatus.IN_PROGRESS, Priority.HIGH,
                LocalDate.now().plusDays(3));
        saveTask(demo, workspace, api, "Write integration tests", TaskStatus.DONE, Priority.MEDIUM, null);
        saveTask(demo, workspace, null, "Read up on pgvector", TaskStatus.TODO, Priority.LOW,
                LocalDate.now().plusWeeks(2));

        saveNote(demo, workspace, "JWT vs sessions", "# JWT\nStateless auth notes...",
                NoteType.CONCEPT, Set.of(spring));
        saveNote(demo, workspace, "Fix: Flyway checksum mismatch",
                "Run `flyway repair` after editing an applied migration.", NoteType.ERROR_FIX, Set.of());

        savePrompt(demo, workspace, "Code reviewer",
                "Review the following diff for bugs and clarity:\n{{diff}}",
                "llm_provider-opus-4-8", "code-review", 5, Set.of(ai));
        savePrompt(demo, workspace, "Commit message",
                "Write a Conventional Commit for these changes:\n{{changes}}",
                "llm_provider-sonnet-4-6", "writing", 4, Set.of(ai));

        saveResource(demo, workspace, "Spring Boot reference", "https://docs.spring.io/spring-boot/",
                ResourceType.OFFICIAL_DOCS, Set.of(spring));
        saveResource(demo, workspace, "Testcontainers docs", "https://java.testcontainers.org/",
                ResourceType.OFFICIAL_DOCS, Set.of(java));

        saveModel(demo, "Llama 3.1 8B Instruct", "Meta", Modality.TEXT, ModelFormat.GGUF, "Q4_K_M",
                6144, "Local chat & drafting");
        saveModel(demo, "nomic-embed-text", "Nomic", Modality.EMBEDDING, ModelFormat.GGUF, "F16",
                512, "RAG embeddings");

        log.info("Dev data seeding complete.");
    }

    private User createUser(String username, String email, String rawPassword, String fullName,
                            Role... roles) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setEnabled(true);
        for (Role role : roles) {
            user.addRole(role);
        }
        return userRepository.save(user);
    }

    private Workspace saveWorkspace(User owner, String name, String description) {
        Workspace workspace = new Workspace();
        workspace.setOwner(owner);
        workspace.setName(name);
        workspace.setDescription(description);
        return workspaceRepository.save(workspace);
    }

    private Tag saveTag(User owner, String name, String color) {
        Tag tag = new Tag();
        tag.setOwner(owner);
        tag.setName(name);
        tag.setColor(color);
        return tagRepository.save(tag);
    }

    private Project saveProject(User owner, Workspace workspace, String name, String description,
                                ProjectStatus status, Priority priority, int progress,
                                Set<String> techStack, Set<Tag> tags) {
        Project project = new Project();
        project.setOwner(owner);
        project.setWorkspace(workspace);
        project.setName(name);
        project.setDescription(description);
        project.setStatus(status);
        project.setPriority(priority);
        project.setProgress(progress);
        project.setTechStack(new java.util.HashSet<>(techStack));
        project.setTags(new java.util.HashSet<>(tags));
        return projectRepository.save(project);
    }

    private void saveTask(User owner, Workspace workspace, Project project, String title,
                          TaskStatus status, Priority priority, LocalDate dueDate) {
        Task task = new Task();
        task.setOwner(owner);
        task.setWorkspace(workspace);
        task.setProject(project);
        task.setTitle(title);
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        taskRepository.save(task);
    }

    private void saveNote(User owner, Workspace workspace, String title, String content,
                          NoteType type, Set<Tag> tags) {
        Note note = new Note();
        note.setOwner(owner);
        note.setWorkspace(workspace);
        note.setTitle(title);
        note.setContent(content);
        note.setType(type);
        note.setTags(new java.util.HashSet<>(tags));
        noteRepository.save(note);
    }

    private void savePrompt(User owner, Workspace workspace, String title, String content,
                            String targetModel, String taskType, int rating, Set<Tag> tags) {
        Prompt prompt = new Prompt();
        prompt.setOwner(owner);
        prompt.setWorkspace(workspace);
        prompt.setTitle(title);
        prompt.setContent(content);
        prompt.setTargetModel(targetModel);
        prompt.setTaskType(taskType);
        prompt.setRating(rating);
        prompt.setPromptVersion(1);
        prompt.setTags(new java.util.HashSet<>(tags));
        promptRepository.save(prompt);
    }

    private void saveResource(User owner, Workspace workspace, String title, String url,
                              ResourceType type, Set<Tag> tags) {
        Resource resource = new Resource();
        resource.setOwner(owner);
        resource.setWorkspace(workspace);
        resource.setTitle(title);
        resource.setUrl(url);
        resource.setType(type);
        resource.setTags(new java.util.HashSet<>(tags));
        resourceRepository.save(resource);
    }

    private void saveModel(User owner, String name, String provider, Modality modality,
                           ModelFormat format, String quantization, int vramMb, String useCase) {
        AiModel model = new AiModel();
        model.setOwner(owner);
        model.setName(name);
        model.setProvider(provider);
        model.setModality(modality);
        model.setFormat(format);
        model.setQuantization(quantization);
        model.setEstimatedVramMb(vramMb);
        model.setUseCase(useCase);
        model.setNotes("Local model; weights managed outside the system (no real paths stored).");
        aiModelRepository.save(model);
    }
}
