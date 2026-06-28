package br.com.helpdesk.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {

    private User user;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Suporte Técnico");
        user.setEmail("suporte@helpdesk.com");
        user.setPassword("hashed_password");
        user.setRole(2);

        ticket = new Ticket();
        ticket.setId(10L);
        ticket.setTitle("Problema de acesso");
        ticket.setDescription("Não consigo acessar o sistema");
        ticket.setStatus(1);
        ticket.setPriority(2);
        ticket.setRequester(user);
    }

    @Test
    void noArgsConstructor_createsInstanceWithNullFields() {
        Comment comment = new Comment();

        assertThat(comment.getId()).isNull();
        assertThat(comment.getTicket()).isNull();
        assertThat(comment.getUser()).isNull();
        assertThat(comment.getComment()).isNull();
        assertThat(comment.getCreatedAt()).isNull();
    }

    @Test
    void setId_andGetId_returnExpectedValue() {
        Comment comment = new Comment();
        comment.setId(7L);

        assertThat(comment.getId()).isEqualTo(7L);
    }

    @Test
    void setTicket_andGetTicket_returnExpectedTicket() {
        Comment comment = new Comment();
        comment.setTicket(ticket);

        assertThat(comment.getTicket()).isSameAs(ticket);
        assertThat(comment.getTicket().getId()).isEqualTo(10L);
        assertThat(comment.getTicket().getTitle()).isEqualTo("Problema de acesso");
    }

    @Test
    void setUser_andGetUser_returnExpectedUser() {
        Comment comment = new Comment();
        comment.setUser(user);

        assertThat(comment.getUser()).isSameAs(user);
        assertThat(comment.getUser().getId()).isEqualTo(1L);
        assertThat(comment.getUser().getName()).isEqualTo("Suporte Técnico");
    }

    @Test
    void setComment_andGetComment_returnExpectedText() {
        Comment comment = new Comment();
        comment.setComment("Estamos verificando o seu chamado.");

        assertThat(comment.getComment()).isEqualTo("Estamos verificando o seu chamado.");
    }

    @Test
    void setCreatedAt_andGetCreatedAt_returnExpectedTimestamp() {
        Comment comment = new Comment();
        LocalDateTime now = LocalDateTime.now();
        comment.setCreatedAt(now);

        assertThat(comment.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void allFields_setAndGet_workTogether() {
        Comment comment = new Comment();
        LocalDateTime now = LocalDateTime.now();

        comment.setId(3L);
        comment.setTicket(ticket);
        comment.setUser(user);
        comment.setComment("Problema identificado e sendo corrigido.");
        comment.setCreatedAt(now);

        assertThat(comment.getId()).isEqualTo(3L);
        assertThat(comment.getTicket()).isSameAs(ticket);
        assertThat(comment.getUser()).isSameAs(user);
        assertThat(comment.getComment()).isEqualTo("Problema identificado e sendo corrigido.");
        assertThat(comment.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void setTicket_withNull_allowsNullTicket() {
        Comment comment = new Comment();
        comment.setTicket(null);

        assertThat(comment.getTicket()).isNull();
    }

    @Test
    void setUser_withNull_allowsNullUser() {
        Comment comment = new Comment();
        comment.setUser(null);

        assertThat(comment.getUser()).isNull();
    }

    @Test
    void setComment_withLongText_storesFullContent() {
        Comment comment = new Comment();
        String longText = "a".repeat(5000);
        comment.setComment(longText);

        assertThat(comment.getComment()).hasSize(5000);
        assertThat(comment.getComment()).isEqualTo(longText);
    }

    @Test
    void setComment_withDifferentValues_updatesCorrectly() {
        Comment comment = new Comment();

        comment.setComment("Primeira resposta.");
        assertThat(comment.getComment()).isEqualTo("Primeira resposta.");

        comment.setComment("Resposta atualizada.");
        assertThat(comment.getComment()).isEqualTo("Resposta atualizada.");
    }

    @Test
    void ticket_withDifferentRequesterAndAssignee_storesCorrectly() {
        User assignee = new User();
        assignee.setId(2L);
        assignee.setName("Analista");
        assignee.setEmail("analista@helpdesk.com");
        assignee.setPassword("hashed_password");
        assignee.setRole(2);

        ticket.setAssignedTo(assignee);

        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setUser(assignee);
        comment.setComment("Chamado assumido.");

        assertThat(comment.getTicket().getAssignedTo()).isSameAs(assignee);
        assertThat(comment.getUser()).isSameAs(assignee);
    }
}
