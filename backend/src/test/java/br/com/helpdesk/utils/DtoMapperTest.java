package br.com.helpdesk.utils;

import br.com.helpdesk.dtos.AuditResponse;
import br.com.helpdesk.dtos.CommentResponse;
import br.com.helpdesk.dtos.TicketResponse;
import br.com.helpdesk.dtos.UserResponse;
import br.com.helpdesk.dtos.UserSummaryResponse;
import br.com.helpdesk.entities.Audit;
import br.com.helpdesk.entities.Comment;
import br.com.helpdesk.entities.Ticket;
import br.com.helpdesk.entities.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DtoMapperTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private User user;
    private User assignedTo;
    private Ticket ticket;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.of(2026, 6, 28, 10, 0);

        user = new User();
        user.setId(1L);
        user.setName("João Silva");
        user.setEmail("joao@helpdesk.com");
        user.setPassword("hashed");
        user.setRole(3);
        user.setActive(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        assignedTo = new User();
        assignedTo.setId(2L);
        assignedTo.setName("Técnico Carlos");
        assignedTo.setEmail("carlos@helpdesk.com");
        assignedTo.setPassword("hashed");
        assignedTo.setRole(2);
        assignedTo.setActive(true);
        assignedTo.setCreatedAt(now);
        assignedTo.setUpdatedAt(now);

        ticket = new Ticket();
        ticket.setId(10L);
        ticket.setTitle("Impressora quebrada");
        ticket.setDescription("A impressora do setor não funciona.");
        ticket.setStatus(0);
        ticket.setPriority(1);
        ticket.setRequester(user);
        ticket.setAssignedTo(assignedTo);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        ticket.setClosedAt(null);
    }

    // --- toUserSummary ---

    @Test
    void toUserSummary_withNull_returnsNull() {
        assertThat(DtoMapper.toUserSummary(null)).isNull();
    }

    @Test
    void toUserSummary_mapsAllFields() {
        UserSummaryResponse result = DtoMapper.toUserSummary(user);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("João Silva");
        assertThat(result.email()).isEqualTo("joao@helpdesk.com");
        assertThat(result.role()).isEqualTo(3);
    }

    // --- toUserResponse ---

    @Test
    void toUserResponse_mapsAllFields() {
        UserResponse result = DtoMapper.toUserResponse(user);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("João Silva");
        assertThat(result.email()).isEqualTo("joao@helpdesk.com");
        assertThat(result.active()).isTrue();
        assertThat(result.role()).isEqualTo(3);
        assertThat(result.createdAt()).isEqualTo(now);
        assertThat(result.updatedAt()).isEqualTo(now);
    }

    @Test
    void toUserResponse_withInactiveUser_mapsActiveFalse() {
        user.setActive(false);

        UserResponse result = DtoMapper.toUserResponse(user);

        assertThat(result.active()).isFalse();
    }

    // --- toTicketResponse ---

    @Test
    void toTicketResponse_mapsAllFields() {
        TicketResponse result = DtoMapper.toTicketResponse(ticket);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.title()).isEqualTo("Impressora quebrada");
        assertThat(result.description()).isEqualTo("A impressora do setor não funciona.");
        assertThat(result.status()).isEqualTo(0);
        assertThat(result.priority()).isEqualTo(1);
        assertThat(result.createdAt()).isEqualTo(now);
        assertThat(result.updatedAt()).isEqualTo(now);
        assertThat(result.closedAt()).isNull();
    }

    @Test
    void toTicketResponse_mapsRequesterAsSummary() {
        TicketResponse result = DtoMapper.toTicketResponse(ticket);

        assertThat(result.requester()).isNotNull();
        assertThat(result.requester().id()).isEqualTo(1L);
        assertThat(result.requester().name()).isEqualTo("João Silva");
        assertThat(result.requester().email()).isEqualTo("joao@helpdesk.com");
        assertThat(result.requester().role()).isEqualTo(3);
    }

    @Test
    void toTicketResponse_mapsAssignedToAsSummary() {
        TicketResponse result = DtoMapper.toTicketResponse(ticket);

        assertThat(result.assignedTo()).isNotNull();
        assertThat(result.assignedTo().id()).isEqualTo(2L);
        assertThat(result.assignedTo().name()).isEqualTo("Técnico Carlos");
        assertThat(result.assignedTo().email()).isEqualTo("carlos@helpdesk.com");
        assertThat(result.assignedTo().role()).isEqualTo(2);
    }

    @Test
    void toTicketResponse_withNullAssignedTo_assignedToIsNull() {
        ticket.setAssignedTo(null);

        TicketResponse result = DtoMapper.toTicketResponse(ticket);

        assertThat(result.assignedTo()).isNull();
    }

    @Test
    void toTicketResponse_withClosedAt_mapsClosedAt() {
        LocalDateTime closedAt = now.plusDays(2);
        ticket.setClosedAt(closedAt);

        TicketResponse result = DtoMapper.toTicketResponse(ticket);

        assertThat(result.closedAt()).isEqualTo(closedAt);
    }

    // --- toCommentResponse ---

    @Test
    void toCommentResponse_mapsAllFields() {
        Comment comment = new Comment();
        comment.setId(5L);
        comment.setTicket(ticket);
        comment.setUser(user);
        comment.setComment("Problema identificado.");
        comment.setCreatedAt(now);

        CommentResponse result = DtoMapper.toCommentResponse(comment);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.ticketId()).isEqualTo(10L);
        assertThat(result.comment()).isEqualTo("Problema identificado.");
        assertThat(result.createdAt()).isEqualTo(now);
    }

    @Test
    void toCommentResponse_mapsUserAsSummary() {
        Comment comment = new Comment();
        comment.setId(5L);
        comment.setTicket(ticket);
        comment.setUser(user);
        comment.setComment("Verificando o equipamento.");
        comment.setCreatedAt(now);

        CommentResponse result = DtoMapper.toCommentResponse(comment);

        assertThat(result.user()).isNotNull();
        assertThat(result.user().id()).isEqualTo(1L);
        assertThat(result.user().name()).isEqualTo("João Silva");
        assertThat(result.user().email()).isEqualTo("joao@helpdesk.com");
    }

    // --- toAuditResponse ---

    @Test
    void toAuditResponse_mapsAllFields() throws Exception {
        JsonNode oldValue = OBJECT_MAPPER.readTree("{\"status\": 0}");
        JsonNode newValue = OBJECT_MAPPER.readTree("{\"status\": 3}");

        Audit audit = new Audit();
        audit.setId(20L);
        audit.setUser(user);
        audit.setAction("TICKET_CLOSED");
        audit.setEntityType(1);
        audit.setEntityId(10L);
        audit.setOldValue(oldValue);
        audit.setNewValue(newValue);
        audit.setCreatedAt(now);

        AuditResponse result = DtoMapper.toAuditResponse(audit);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(20L);
        assertThat(result.action()).isEqualTo("TICKET_CLOSED");
        assertThat(result.entityType()).isEqualTo(1);
        assertThat(result.entityId()).isEqualTo(10L);
        assertThat(result.oldValue().get("status").asInt()).isEqualTo(0);
        assertThat(result.newValue().get("status").asInt()).isEqualTo(3);
        assertThat(result.createdAt()).isEqualTo(now);
    }

    @Test
    void toAuditResponse_mapsUserAsSummary() throws Exception {
        Audit audit = new Audit();
        audit.setId(21L);
        audit.setUser(user);
        audit.setAction("USER_CREATED");
        audit.setEntityType(0);
        audit.setEntityId(1L);
        audit.setOldValue(null);
        audit.setNewValue(OBJECT_MAPPER.readTree("{\"name\": \"João Silva\"}"));
        audit.setCreatedAt(now);

        AuditResponse result = DtoMapper.toAuditResponse(audit);

        assertThat(result.user()).isNotNull();
        assertThat(result.user().id()).isEqualTo(1L);
        assertThat(result.user().name()).isEqualTo("João Silva");
        assertThat(result.user().email()).isEqualTo("joao@helpdesk.com");
    }

    @Test
    void toAuditResponse_withNullOldAndNewValue_mapsNulls() {
        Audit audit = new Audit();
        audit.setId(22L);
        audit.setUser(user);
        audit.setAction("COMMENT_ADDED");
        audit.setEntityType(2);
        audit.setEntityId(5L);
        audit.setOldValue(null);
        audit.setNewValue(null);
        audit.setCreatedAt(now);

        AuditResponse result = DtoMapper.toAuditResponse(audit);

        assertThat(result.oldValue()).isNull();
        assertThat(result.newValue()).isNull();
    }
}
