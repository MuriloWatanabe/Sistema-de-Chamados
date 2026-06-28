package br.com.helpdesk.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TicketTest {

    private User requester;
    private User assignedTo;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(1L);
        requester.setName("João Silva");
        requester.setEmail("joao@helpdesk.com");
        requester.setPassword("hashed_password");
        requester.setRole(0);

        assignedTo = new User();
        assignedTo.setId(2L);
        assignedTo.setName("Suporte Técnico");
        assignedTo.setEmail("suporte@helpdesk.com");
        assignedTo.setPassword("hashed_password");
        assignedTo.setRole(1);
    }

    @Test
    void noArgsConstructor_createsInstanceWithNullFields() {
        Ticket ticket = new Ticket();

        assertThat(ticket.getId()).isNull();
        assertThat(ticket.getTitle()).isNull();
        assertThat(ticket.getDescription()).isNull();
        assertThat(ticket.getStatus()).isNull();
        assertThat(ticket.getPriority()).isNull();
        assertThat(ticket.getRequester()).isNull();
        assertThat(ticket.getAssignedTo()).isNull();
        assertThat(ticket.getCreatedAt()).isNull();
        assertThat(ticket.getUpdatedAt()).isNull();
        assertThat(ticket.getClosedAt()).isNull();
    }

    @Test
    void setId_andGetId_returnExpectedValue() {
        Ticket ticket = new Ticket();
        ticket.setId(10L);

        assertThat(ticket.getId()).isEqualTo(10L);
    }

    @Test
    void setTitle_andGetTitle_returnExpectedValue() {
        Ticket ticket = new Ticket();
        ticket.setTitle("Sistema fora do ar");

        assertThat(ticket.getTitle()).isEqualTo("Sistema fora do ar");
    }

    @Test
    void setDescription_andGetDescription_returnExpectedValue() {
        Ticket ticket = new Ticket();
        ticket.setDescription("O sistema está apresentando erro 500 ao tentar fazer login.");

        assertThat(ticket.getDescription()).isEqualTo("O sistema está apresentando erro 500 ao tentar fazer login.");
    }

    @Test
    void setStatus_andGetStatus_returnExpectedValue() {
        Ticket ticket = new Ticket();
        ticket.setStatus(1);

        assertThat(ticket.getStatus()).isEqualTo(1);
    }

    @Test
    void setStatus_withDifferentValues_updatesCorrectly() {
        Ticket ticket = new Ticket();

        ticket.setStatus(0);
        assertThat(ticket.getStatus()).isEqualTo(0);

        ticket.setStatus(2);
        assertThat(ticket.getStatus()).isEqualTo(2);

        ticket.setStatus(3);
        assertThat(ticket.getStatus()).isEqualTo(3);
    }

    @Test
    void setPriority_andGetPriority_returnExpectedValue() {
        Ticket ticket = new Ticket();
        ticket.setPriority(2);

        assertThat(ticket.getPriority()).isEqualTo(2);
    }

    @Test
    void setPriority_withDifferentValues_updatesCorrectly() {
        Ticket ticket = new Ticket();

        ticket.setPriority(1);
        assertThat(ticket.getPriority()).isEqualTo(1);

        ticket.setPriority(3);
        assertThat(ticket.getPriority()).isEqualTo(3);
    }

    @Test
    void setRequester_andGetRequester_returnExpectedUser() {
        Ticket ticket = new Ticket();
        ticket.setRequester(requester);

        assertThat(ticket.getRequester()).isSameAs(requester);
        assertThat(ticket.getRequester().getId()).isEqualTo(1L);
        assertThat(ticket.getRequester().getName()).isEqualTo("João Silva");
    }

    @Test
    void setAssignedTo_andGetAssignedTo_returnExpectedUser() {
        Ticket ticket = new Ticket();
        ticket.setAssignedTo(assignedTo);

        assertThat(ticket.getAssignedTo()).isSameAs(assignedTo);
        assertThat(ticket.getAssignedTo().getId()).isEqualTo(2L);
        assertThat(ticket.getAssignedTo().getName()).isEqualTo("Suporte Técnico");
    }

    @Test
    void setAssignedTo_withNull_allowsUnassignedTicket() {
        Ticket ticket = new Ticket();
        ticket.setAssignedTo(assignedTo);
        ticket.setAssignedTo(null);

        assertThat(ticket.getAssignedTo()).isNull();
    }

    @Test
    void setCreatedAt_andGetCreatedAt_returnExpectedTimestamp() {
        Ticket ticket = new Ticket();
        LocalDateTime now = LocalDateTime.now();
        ticket.setCreatedAt(now);

        assertThat(ticket.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void setUpdatedAt_andGetUpdatedAt_returnExpectedTimestamp() {
        Ticket ticket = new Ticket();
        LocalDateTime now = LocalDateTime.now();
        ticket.setUpdatedAt(now);

        assertThat(ticket.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void setClosedAt_andGetClosedAt_returnExpectedTimestamp() {
        Ticket ticket = new Ticket();
        LocalDateTime closedAt = LocalDateTime.of(2026, 6, 28, 15, 30);
        ticket.setClosedAt(closedAt);

        assertThat(ticket.getClosedAt()).isEqualTo(closedAt);
    }

    @Test
    void setClosedAt_withNull_allowsOpenTicket() {
        Ticket ticket = new Ticket();
        ticket.setClosedAt(null);

        assertThat(ticket.getClosedAt()).isNull();
    }

    @Test
    void allFields_setAndGet_workTogether() {
        Ticket ticket = new Ticket();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime closedAt = now.plusDays(1);

        ticket.setId(5L);
        ticket.setTitle("Impressora não funciona");
        ticket.setDescription("A impressora do setor financeiro não está imprimindo.");
        ticket.setStatus(2);
        ticket.setPriority(1);
        ticket.setRequester(requester);
        ticket.setAssignedTo(assignedTo);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        ticket.setClosedAt(closedAt);

        assertThat(ticket.getId()).isEqualTo(5L);
        assertThat(ticket.getTitle()).isEqualTo("Impressora não funciona");
        assertThat(ticket.getDescription()).isEqualTo("A impressora do setor financeiro não está imprimindo.");
        assertThat(ticket.getStatus()).isEqualTo(2);
        assertThat(ticket.getPriority()).isEqualTo(1);
        assertThat(ticket.getRequester()).isSameAs(requester);
        assertThat(ticket.getAssignedTo()).isSameAs(assignedTo);
        assertThat(ticket.getCreatedAt()).isEqualTo(now);
        assertThat(ticket.getUpdatedAt()).isEqualTo(now);
        assertThat(ticket.getClosedAt()).isEqualTo(closedAt);
    }

    @Test
    void requesterAndAssignedTo_canBeTheSameUser() {
        Ticket ticket = new Ticket();
        ticket.setRequester(requester);
        ticket.setAssignedTo(requester);

        assertThat(ticket.getRequester()).isSameAs(ticket.getAssignedTo());
    }
}
