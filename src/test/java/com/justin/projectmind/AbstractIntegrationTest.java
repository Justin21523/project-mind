package com.justin.projectmind;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests. Boots the full application context against a real
 * PostgreSQL instance managed by Testcontainers; Flyway migrations run on startup.
 *
 * <p>The container follows the singleton pattern: it is started once in a static
 * initializer and intentionally never stopped, so it survives across test classes that
 * share the same (cached) Spring context. Testcontainers' Ryuk reaps it at JVM exit.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }
}
