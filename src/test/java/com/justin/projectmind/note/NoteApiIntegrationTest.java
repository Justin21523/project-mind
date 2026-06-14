package com.justin.projectmind.note;

import com.justin.projectmind.AbstractApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NoteApiIntegrationTest extends AbstractApiIntegrationTest {

    private String token;
    private long workspaceId;

    @BeforeEach
    void setUp() throws Exception {
        token = registerAndLogin(uniqueUsername("note_user_"));
        workspaceId = createWorkspace(token, "Knowledge");
    }

    private long createTag(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/tags")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","color":"#1A2B3C"}
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    @Test
    void create_noteWithTag_embedsTagInResponse() throws Exception {
        long tagId = createTag("spring");

        mockMvc.perform(post("/api/v1/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workspaceId":%d,"title":"Beans","content":"# beans","type":"CONCEPT","tagIds":[%d]}
                                """.formatted(workspaceId, tagId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("CONCEPT"))
                .andExpect(jsonPath("$.data.tags[0].id").value((int) tagId))
                .andExpect(jsonPath("$.data.tags[0].name").value("spring"));
    }

    @Test
    void create_noteWithUnknownTag_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workspaceId":%d,"title":"X","type":"REFERENCE","tagIds":[999999]}
                                """.formatted(workspaceId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }
}
