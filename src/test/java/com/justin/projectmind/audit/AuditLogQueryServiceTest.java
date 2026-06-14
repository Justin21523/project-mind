package com.justin.projectmind.audit;

import com.justin.projectmind.audit.mapper.AuditLogMapper;
import com.justin.projectmind.audit.repository.AuditLogRepository;
import com.justin.projectmind.audit.service.AuditLogQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogQueryServiceTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private AuditLogMapper auditLogMapper;

    @InjectMocks private AuditLogQueryService auditLogQueryService;

    private static final Long USER_ID = 1L;
    private static final Pageable PAGEABLE = PageRequest.of(0, 20);

    @Test
    void list_withEntityType_usesFilteredQuery() {
        when(auditLogRepository.findByUserIdAndEntityType(USER_ID, "PROJECT", PAGEABLE))
                .thenReturn(Page.empty());

        auditLogQueryService.list(USER_ID, "PROJECT", PAGEABLE);

        verify(auditLogRepository).findByUserIdAndEntityType(USER_ID, "PROJECT", PAGEABLE);
        verify(auditLogRepository, never()).findByUserId(USER_ID, PAGEABLE);
    }

    @Test
    void list_withoutEntityType_usesUnfilteredQuery() {
        when(auditLogRepository.findByUserId(USER_ID, PAGEABLE)).thenReturn(Page.empty());

        auditLogQueryService.list(USER_ID, null, PAGEABLE);

        verify(auditLogRepository).findByUserId(USER_ID, PAGEABLE);
    }
}
