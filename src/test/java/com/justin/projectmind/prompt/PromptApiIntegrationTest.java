package com.justin.projectmind.prompt;

import com.justin.projectmind.AbstractApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PromptApiIntegrationTest extends AbstractApiIntegrationTest {

    private String token;
    private long workspaceId;

    @BeforeEach
    void setUp() throws Exception {
        token = registerAndLogin(uniqueUsername("prompt_user_"));
        workspaceId = createWorkspace(token, "Prompts");
    }

    @Test
    void update_bumpsEditorialVersion() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/prompts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workspaceId":%d,"title":"Summarizer","content":"Summarize: {{input}}"}
                                """.formatted(workspaceId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.promptVersion").value(1))
                .andReturn();
        long promptId = objectMapper.readTree(created.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(put("/api/v1/prompts/" + promptId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workspaceId":%d,"title":"Summarizer v2","content":"Summarize clearly: {{input}}"}
                                """.formatted(workspaceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Summarizer v2"))
                .andExpect(jsonPath("$.data.promptVersion").value(2));
    }
}
