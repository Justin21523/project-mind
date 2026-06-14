package com.justin.projectmind.common.exception;

/**
 * Thrown for client errors that are not field-validation failures
 * (e.g. an invalid state transition or a malformed token).
 */
public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
}
