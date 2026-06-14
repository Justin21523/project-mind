package com.justin.projectmind.common.exception;

/**
 * Thrown when an operation conflicts with existing state, e.g. a duplicate
 * unique value such as an already-registered username or email.
 */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }
}
