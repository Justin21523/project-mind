package com.justin.projectmind.common.exception;

import lombok.Getter;

/**
 * Base class for all expected, client-facing application exceptions. Carries an
 * {@link ErrorCode} so the global handler can map it to an HTTP status consistently.
 */
@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
