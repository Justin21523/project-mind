package com.justin.projectmind.modelregistry;

import com.justin.projectmind.AbstractApiIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AiModelApiIntegrationTest extends AbstractApiIntegrationTest {

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        token = registerAndLogin(uniqueUsername("model_user_"));
    }

    private void register(String name, String modality) throws Exception {
        mockMvc.perform(post("/api/v1/models")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","provider":"ollama","modality":"%s","format":"GGUF",
                                 "quantization":"Q4_K_M","estimatedVramMb":8192,"useCase":"local chat"}
                                """.formatted(name, modality)))
                .andExpect(status().isCreated());
    }

    @Test
    void register_duplicateName_returnsConflict() throws Exception {
        register("llama3", "TEXT");

        mockMvc.perform(post("/api/v1/models")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"llama3","modality":"TEXT"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void list_filtersByModality() throws Exception {
        register("text-model", "TEXT");
        register("vision-model", "VISION");

        mockMvc.perform(get("/api/v1/models")
                        .header("Authorization", "Bearer " + token)
                        .param("modality", "VISION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("vision-model"));
    }
}
