package com.justin.projectmind.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justin.projectmind.common.exception.ErrorCode;
import com.justin.projectmind.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Returns the standard {@link ErrorResponse} envelope (instead of the default HTML)
 * when an unauthenticated request hits a protected endpoint.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = ErrorResponse.of(
                ErrorCode.UNAUTHORIZED.getCode(),
                "Authentication is required to access this resource",
                request.getRequestURI());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
