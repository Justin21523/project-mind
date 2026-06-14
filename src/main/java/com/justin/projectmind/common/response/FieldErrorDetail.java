package com.justin.projectmind.common.response;

/**
 * Describes a single field validation failure.
 */
public record FieldErrorDetail(String field, String message) {
}
