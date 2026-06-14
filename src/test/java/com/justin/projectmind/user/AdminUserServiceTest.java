package com.justin.projectmind.user;

import com.justin.projectmind.audit.AuditAction;
import com.justin.projectmind.audit.service.AuditRecorder;
import com.justin.projectmind.common.exception.ResourceNotFoundException;
import com.justin.projectmind.user.entity.Role;
import com.justin.projectmind.user.entity.User;
import com.justin.projectmind.user.mapper.UserMapper;
import com.justin.projectmind.user.repository.UserRepository;
import com.justin.projectmind.user.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private AuditRecorder auditRecorder;

    @InjectMocks private AdminUserService adminUserService;

    private static final Long ADMIN_ID = 1L;
    private static final Long TARGET_ID = 2L;

    @Test
    void updateStatus_disablesUser_andRecordsAudit() {
        User target = new User();
        target.setId(TARGET_ID);
        target.setEnabled(true);
        when(userRepository.findById(TARGET_ID)).thenReturn(Optional.of(target));
        when(userMapper.toResponse(any(User.class))).thenReturn(null);

        adminUserService.updateStatus(ADMIN_ID, TARGET_ID, false);

        assertThat(target.isEnabled()).isFalse();
        verify(auditRecorder).record(ADMIN_ID, AuditAction.UPDATE, "USER", TARGET_ID);
    }

    @Test
    void updateRoles_replacesRoles_andRecordsAudit() {
        User target = new User();
        target.setId(TARGET_ID);
        target.addRole(Role.USER);
        when(userRepository.findById(TARGET_ID)).thenReturn(Optional.of(target));
        when(userMapper.toResponse(any(User.class))).thenReturn(null);

        adminUserService.updateRoles(ADMIN_ID, TARGET_ID, Set.of(Role.USER, Role.ADMIN));

        assertThat(target.getRoles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
        verify(auditRecorder).record(ADMIN_ID, AuditAction.UPDATE, "USER", TARGET_ID);
    }

    @Test
    void getUser_missing_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.getUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
