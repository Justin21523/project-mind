package com.justin.projectmind.user;

import com.justin.projectmind.AbstractApiIntegrationTest;
import com.justin.projectmind.user.entity.Role;
import com.justin.projectmind.user.entity.User;
import com.justin.projectmind.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminApiIntegrationTest extends AbstractApiIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    /** Promotes an already-registered user to ADMIN directly in the DB. */
    private long promoteToAdmin(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.addRole(Role.ADMIN);
        return userRepository.save(user).getId();
    }

    @Test
    void admin_canListAndDisableUsers() throws Exception {
        String adminName = uniqueUsername("admin_");
        String adminToken = registerAndLogin(adminName);
        promoteToAdmin(adminName);

        String targetName = uniqueUsername("target_");
        registerAndLogin(targetName);
        long targetId = userRepository.findByUsername(targetName).orElseThrow().getId();

        // Admin lists users filtered to the target
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("search", targetName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].username").value(targetName));

        // Admin disables the target user
        mockMvc.perform(patch("/api/v1/admin/users/" + targetId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"enabled": false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    void admin_canReadSystemAuditLogs() throws Exception {
        String adminName = uniqueUsername("admin_");
        String adminToken = registerAndLogin(adminName);
        promoteToAdmin(adminName);

        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void normalUser_isForbiddenFromAdminEndpoints() throws Exception {
        String userToken = registerAndLogin(uniqueUsername("plain_"));

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }
}
