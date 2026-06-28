package br.com.helpdesk.services;

import br.com.helpdesk.dtos.AuditResponse;
import br.com.helpdesk.entities.Audit;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.AuditEntityType;
import br.com.helpdesk.repositories.AuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditService auditService;

    private User actor;

    @BeforeEach
    void setUp() {
        actor = new User();
        actor.setId(1L);
        actor.setName("Admin");
        actor.setEmail("admin@helpdesk.com");
        actor.setRole(0);
        actor.setActive(true);
    }

    @Test
    void record_skipsWhenActorIsNull() {
        auditService.record(null, "ACTION", AuditEntityType.USER, 1L, null, null);

        verify(auditRepository, never()).saveAndFlush(any());
    }

    @Test
    void record_savesAuditWhenActorIsNotNull() {
        Audit saved = new Audit();
        saved.setId(10L);
        when(auditRepository.saveAndFlush(any(Audit.class))).thenReturn(saved);

        auditService.record(actor, "USER_CREATED", AuditEntityType.USER, 5L, null, null);

        verify(auditRepository).saveAndFlush(any(Audit.class));
    }

    @Test
    void record_setsAllFieldsOnAudit() {
        Audit saved = new Audit();
        when(auditRepository.saveAndFlush(any(Audit.class))).thenReturn(saved);
        ArgumentCaptor<Audit> captor = ArgumentCaptor.forClass(Audit.class);

        auditService.record(actor, "USER_UPDATED", AuditEntityType.USER, 5L, null, null);

        verify(auditRepository).saveAndFlush(captor.capture());
        Audit captured = captor.getValue();
        assertThat(captured.getUser()).isEqualTo(actor);
        assertThat(captured.getAction()).isEqualTo("USER_UPDATED");
        assertThat(captured.getEntityType()).isEqualTo(AuditEntityType.USER.getCode());
        assertThat(captured.getEntityId()).isEqualTo(5L);
    }

    @Test
    void listAll_returnsEmptyListWhenNoAudits() {
        when(auditRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        List<AuditResponse> result = auditService.listAll();

        assertThat(result).isEmpty();
    }

    @Test
    void listAll_returnsMappedAuditResponses() {
        Audit audit = new Audit();
        audit.setId(1L);
        audit.setUser(actor);
        audit.setAction("AUTH_LOGIN");
        audit.setEntityType(AuditEntityType.AUTH.getCode());
        audit.setEntityId(1L);
        when(auditRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(audit));

        List<AuditResponse> result = auditService.listAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).action()).isEqualTo("AUTH_LOGIN");
        assertThat(result.get(0).entityType()).isEqualTo(AuditEntityType.AUTH.getCode());
    }

    @Test
    void listAll_returnsMultipleAuditsInOrder() {
        Audit first = new Audit();
        first.setId(1L);
        first.setUser(actor);
        first.setAction("USER_CREATED");
        first.setEntityType(AuditEntityType.USER.getCode());
        first.setEntityId(1L);

        Audit second = new Audit();
        second.setId(2L);
        second.setUser(actor);
        second.setAction("AUTH_LOGIN");
        second.setEntityType(AuditEntityType.AUTH.getCode());
        second.setEntityId(1L);

        when(auditRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(first, second));

        List<AuditResponse> result = auditService.listAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).action()).isEqualTo("USER_CREATED");
        assertThat(result.get(1).action()).isEqualTo("AUTH_LOGIN");
    }
}
