package com.justin.projectmind.user.entity;

/**
 * Application roles. Stored without the "ROLE_" prefix; the prefix is added when
 * building Spring Security authorities.
 */
public enum Role {
    USER,
    ADMIN
}
