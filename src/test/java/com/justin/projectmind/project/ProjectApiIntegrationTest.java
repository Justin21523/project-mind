package com.justin.projectmind.project;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justin.projectmind.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ProjectApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;

    @BeforeEach
    void authenticate() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        String username = "bob" + suffix;
        String register = """
                {"username":"%s","email":"%s@example.com","password":"password123"}
                """.formatted(username, username);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(register))
                .andExpect(status().isCreated());

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usernameOrEmail":"%s","password":"password123"}
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn();
        accessToken = objectMapper.readTree(login.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();
    }

    @Test
    void create_workspace_then_project_and_audit_trail() throws Exception {
        // Create a workspace
        MvcResult wsResult = mockMvc.perform(post("/api/v1/workspaces")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Backend","description":"server work"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long workspaceId = objectMapper.readTree(wsResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();
        assertThat(workspaceId).isPositive();

        // Create a project in that workspace
        mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workspaceId": %d,
                                  "name": "ProjectMind API",
                                  "status": "IN_PROGRESS",
                                  "priority": "HIGH",
                                  "progress": 40,
                                  "techStack": ["Java", "Spring Boot"]
                                }
                                """.formatted(workspaceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("ProjectMind API"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.progress").value(40));

        // The project shows up in a filtered list
        mockMvc.perform(get("/api/v1/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("workspaceId", String.valueOf(workspaceId))
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("ProjectMind API"));

        // The audit trail recorded the create actions (written after commit)
        mockMvc.perform(get("/api/v1/audit-logs")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("entityType", "PROJECT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].action").value("CREATE"));
    }

    @Test
    void create_project_inUnknownWorkspace_returnsNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workspaceId": 999999, "name": "X", "status": "PLANNING", "priority": "LOW", "progress": 0}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
