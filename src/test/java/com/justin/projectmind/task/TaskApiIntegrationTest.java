package com.justin.projectmind.task;

import com.justin.projectmind.AbstractApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskApiIntegrationTest extends AbstractApiIntegrationTest {

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        token = registerAndLogin(uniqueUsername("task_user_"));
    }

    private long createProject(long workspaceId, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workspaceId":%d,"name":"%s","status":"PLANNING","priority":"LOW","progress":0}
                                """.formatted(workspaceId, name)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    @Test
    void create_standaloneTask_thenListByStatus() throws Exception {
        long workspaceId = createWorkspace(token, "Work");

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workspaceId":%d,"title":"Ship MVP","status":"TODO","priority":"HIGH"}
                                """.formatted(workspaceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Ship MVP"))
                .andExpect(jsonPath("$.data.projectId").doesNotExist());

        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .param("workspaceId", String.valueOf(workspaceId))
                        .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Ship MVP"));
    }

    @Test
    void create_taskWithProjectFromAnotherWorkspace_returnsBadRequest() throws Exception {
        long workspaceA = createWorkspace(token, "Alpha");
        long workspaceB = createWorkspace(token, "Beta");
        long projectInA = createProject(workspaceA, "ProjectA");

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workspaceId":%d,"projectId":%d,"title":"X","status":"TODO","priority":"LOW"}
                                """.formatted(workspaceB, projectInA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }
}
