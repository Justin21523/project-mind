package com.justin.projectmind.auth.service;

import com.justin.projectmind.auth.dto.AuthResponse;
import com.justin.projectmind.auth.dto.LoginRequest;
import com.justin.projectmind.auth.dto.RefreshTokenRequest;
import com.justin.projectmind.auth.dto.RegisterRequest;
import com.justin.projectmind.auth.entity.RefreshToken;
import com.justin.projectmind.auth.repository.RefreshTokenRepository;
import com.justin.projectmind.common.exception.BadRequestException;
import com.justin.projectmind.common.exception.ConflictException;
import com.justin.projectmind.security.JwtProperties;
import com.justin.projectmind.security.JwtTokenProvider;
import com.justin.projectmind.security.SecurityUserDetails;
import com.justin.projectmind.user.dto.UserResponse;
import com.justin.projectmind.user.entity.Role;
import com.justin.projectmind.user.entity.User;
import com.justin.projectmind.user.mapper.UserMapper;
import com.justin.projectmind.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Handles registration, authentication, and refresh-token lifecycle.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final UserMapper userMapper;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setEnabled(true);
        user.addRole(Role.USER);

        return userMapper.toResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password()));
        SecurityUserDetails principal = (SecurityUserDetails) authentication.getPrincipal();
        User user = userRepository.getReferenceById(principal.getId());

        return issueTokens(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        if (!stored.isActive()) {
            throw new BadRequestException("Refresh token is expired or revoked");
        }

        // Rotate: revoke the presented token and issue a fresh pair.
        stored.setRevoked(true);
        return issueTokens(stored.getUser());
    }

    public void logout(Long userId) {
        User user = userRepository.getReferenceById(userId);
        refreshTokenRepository.revokeAllForUser(user);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getUsername());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtProperties.refreshTokenExpirationMs()));
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.of(accessToken, refreshToken.getToken(),
                jwtProperties.accessTokenExpirationMs());
    }
}
