package com.justin.projectmind.common.exception;

/**
 * Thrown when a requested resource does not exist (or is not visible to the caller).
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException("%s not found with id: %s".formatted(resource, id));
    }
}
