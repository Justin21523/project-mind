package com.justin.projectmind.config;

import com.justin.projectmind.security.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Enables JPA auditing so {@code @CreatedDate}, {@code @LastModifiedDate},
 * {@code @CreatedBy} and {@code @LastModifiedBy} fields are populated automatically.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    /**
     * Resolves the current principal's username for the {@code createdBy}/{@code updatedBy}
     * audit columns. Falls back to "system" for unauthenticated contexts (e.g. registration).
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of(SecurityUtils.getCurrentUsername().orElse("system"));
    }
}
