package com.justin.projectmind.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standard envelope returned by all error responses.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        String code,
        String message,
        String path,
        List<FieldErrorDetail> errors,
        Instant timestamp
) {

    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(false, code, message, path, null, Instant.now());
    }

    public static ErrorResponse of(String code, String message, String path, List<FieldErrorDetail> errors) {
        return new ErrorResponse(false, code, message, path, errors, Instant.now());
    }
}
