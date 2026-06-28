package br.com.helpdesk.controllers;

import br.com.helpdesk.dtos.AuditResponse;
import br.com.helpdesk.dtos.UserSummaryResponse;
import br.com.helpdesk.services.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditController controller;

    private AuditResponse auditResponse;

    @BeforeEach
    void setUp() {
        UserSummaryResponse user = new UserSummaryResponse(1L, "Admin", "admin@helpdesk.com", 3);
        auditResponse = new AuditResponse(1L, user, "TICKET_CREATED", 1, 10L, null, null, LocalDateTime.now());
    }

    @Test
    void listAudits_returnsListFromService() {
        when(auditService.listAll()).thenReturn(List.of(auditResponse));

        List<AuditResponse> result = controller.listAudits();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).action()).isEqualTo("TICKET_CREATED");
    }

    @Test
    void listAudits_callsServiceListAll() {
        when(auditService.listAll()).thenReturn(List.of());

        controller.listAudits();

        verify(auditService).listAll();
    }

    @Test
    void listAudits_withEmptyList_returnsEmptyList() {
        when(auditService.listAll()).thenReturn(List.of());

        List<AuditResponse> result = controller.listAudits();

        assertThat(result).isEmpty();
    }

    @Test
    void listAudits_withMultipleRecords_returnsAll() {
        UserSummaryResponse user = new UserSummaryResponse(2L, "Supervisor", "sup@helpdesk.com", 2);
        AuditResponse second = new AuditResponse(2L, user, "TICKET_UPDATED", 1, 11L, null, null, LocalDateTime.now());
        when(auditService.listAll()).thenReturn(List.of(auditResponse, second));

        List<AuditResponse> result = controller.listAudits();

        assertThat(result).hasSize(2);
    }
}
