package com.justin.projectmind.user.service;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.common.pagination.PageResponse;
import com.justin.projectmind.user.dto.UserResponse;
import com.justin.projectmind.user.entity.Role;
import com.justin.projectmind.user.entity.User;
import com.justin.projectmind.user.mapper.UserMapper;
import com.justin.projectmind.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Administrative operations on user accounts. Reachable only by callers with the ADMIN
 * role (enforced at the controller via method security); these operations are intentionally
 * not owner-scoped.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService {

    private static final String ENTITY_TYPE = "USER";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuditRecorder auditRecorder;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(String search, Pageable pageable) {
        Page<User> page = StringUtils.hasText(search)
                ? userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        search, search, pageable)
                : userRepository.findAll(pageable);
        return PageResponse.from(page.map(userMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return userMapper.toResponse(requireUser(id));
    }

    public UserResponse updateStatus(Long adminId, Long userId, boolean enabled) {
        User user = requireUser(userId);
        user.setEnabled(enabled);
        auditRecorder.record(adminId, AuditAction.UPDATE, ENTITY_TYPE, userId);
        return userMapper.toResponse(user);
    }

    public UserResponse updateRoles(Long adminId, Long userId, Set<Role> roles) {
        User user = requireUser(userId);
        user.setRoles(new HashSet<>(roles));
        auditRecorder.record(adminId, AuditAction.UPDATE, ENTITY_TYPE, userId);
        return userMapper.toResponse(user);
    }

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }
}
