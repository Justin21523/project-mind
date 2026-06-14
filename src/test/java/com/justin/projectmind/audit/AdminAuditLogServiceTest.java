package com.justin.projectmind.audit;

import com.justin.projectmind.audit.mapper.AuditLogMapper;
import com.justin.projectmind.audit.repository.AuditLogRepository;
import com.justin.projectmind.audit.service.AdminAuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuditLogServiceTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private AuditLogMapper auditLogMapper;

    @InjectMocks private AdminAuditLogService adminAuditLogService;

    private static final Pageable PAGEABLE = PageRequest.of(0, 20);

    @Test
    void list_noFilters_usesFindAll() {
        when(auditLogRepository.findAll(PAGEABLE)).thenReturn(Page.empty());

        adminAuditLogService.list(null, null, PAGEABLE);

        verify(auditLogRepository).findAll(PAGEABLE);
    }

    @Test
    void list_userIdOnly_usesFindByUserId() {
        when(auditLogRepository.findByUserId(7L, PAGEABLE)).thenReturn(Page.empty());

        adminAuditLogService.list(7L, null, PAGEABLE);

        verify(auditLogRepository).findByUserId(7L, PAGEABLE);
    }

    @Test
    void list_entityTypeOnly_usesFindByEntityType() {
        when(auditLogRepository.findByEntityType("PROJECT", PAGEABLE)).thenReturn(Page.empty());

        adminAuditLogService.list(null, "PROJECT", PAGEABLE);

        verify(auditLogRepository).findByEntityType("PROJECT", PAGEABLE);
    }

    @Test
    void list_bothFilters_usesCombinedQuery() {
        when(auditLogRepository.findByUserIdAndEntityType(7L, "PROJECT", PAGEABLE)).thenReturn(Page.empty());

        adminAuditLogService.list(7L, "PROJECT", PAGEABLE);

        verify(auditLogRepository).findByUserIdAndEntityType(7L, "PROJECT", PAGEABLE);
    }
}
