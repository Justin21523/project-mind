package com.justin.projectmind.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Spring's annotation-driven cache management. The actual cache manager
 * (Redis) is auto-configured from {@code spring.cache.*} properties.
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
