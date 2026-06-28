package br.com.helpdesk.controllers;

import br.com.helpdesk.dtos.CommentCreateRequest;
import br.com.helpdesk.dtos.CommentResponse;
import br.com.helpdesk.dtos.TicketCreateRequest;
import br.com.helpdesk.dtos.TicketResponse;
import br.com.helpdesk.dtos.TicketUpdateRequest;
import br.com.helpdesk.dtos.UserSummaryResponse;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.security.CurrentUserService;
import br.com.helpdesk.services.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private TicketController controller;

    private User user;
    private Authentication authentication;
    private TicketResponse ticketResponse;
    private UserSummaryResponse userSummary;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Ana");
        user.setEmail("ana@helpdesk.com");
        user.setRole(1);

        authentication = mock(Authentication.class);

        userSummary = new UserSummaryResponse(1L, "Ana", "ana@helpdesk.com", 1);
        ticketResponse = new TicketResponse(10L, "Sem acesso", "Não consigo entrar", 1, 2,
                userSummary, null, LocalDateTime.now(), LocalDateTime.now(), null);
    }

    @Test
    void listTickets_returnsListFromService() {
        when(currentUserService.requireUser(authentication)).thenReturn(user);
        when(ticketService.listVisibleTickets(user)).thenReturn(List.of(ticketResponse));

        List<TicketResponse> result = controller.listTickets(authentication);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(10L);
        assertThat(result.get(0).title()).isEqualTo("Sem acesso");
    }

    @Test
    void listTickets_callsCurrentUserServiceAndTicketService() {
        when(currentUserService.requireUser(authentication)).thenReturn(user);
        when(ticketService.listVisibleTickets(user)).thenReturn(List.of());

        controller.listTickets(authentication);

        verify(currentUserService).requireUser(authentication);
        verify(ticketService).listVisibleTickets(user);
    }

    @Test
    void getTicket_returnsTicketFromService() {
        when(currentUserService.requireUser(authentication)).thenReturn(user);
        when(ticketService.getTicket(10L, user)).thenReturn(ticketResponse);

        TicketResponse result = controller.getTicket(10L, authentication);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.title()).isEqualTo("Sem acesso");
    }

    @Test
    void getTicket_callsServiceWithCorrectId() {
        when(currentUserService.requireUser(authentication)).thenReturn(user);
        when(ticketService.getTicket(10L, user)).thenReturn(ticketResponse);

        controller.getTicket(10L, authentication);

        verify(ticketService).getTicket(10L, user);
    }

    @Test
    void createTicket_returnsCreatedTicketFromService() {
        TicketCreateRequest request = new TicketCreateRequest("Sem acesso", "Não consigo entrar", 2, null, null);
        when(currentUserService.requireUser(authentication)).thenReturn(user);
        when(ticketService.createTicket(user, request)).thenReturn(ticketResponse);

        TicketResponse result = controller.createTicket(request, authentication);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.title()).isEqualTo("Sem acesso");
    }

    @Test
    void createTicket_callsServiceWithUserAndRequest() {
        TicketCreateRequest request = new TicketCreateRequest("Sem acesso", "Não consigo entrar", 2, null, null);
        when(currentUserService.requireUser(authentication)).thenReturn(user);
        when(ticketService.createTicket(user, request)).thenReturn(ticketResponse);

        controller.createTicket(request, authentication);

        verify(ticketService).createTicket(user, request);
    }

    @Test
    void deleteTicket_callsServiceWithCorrectId() {
        when(currentUserService.requireUser(authentication)).thenReturn(user);

        controller.deleteTicket(10L, authentication);

        verify(ticketService).deleteTicket(10L, user);
    }

    @Test
    void updateTicket_returnsUpdatedTicketFromService() {
        TicketUpdateRequest updateRequest = mock(TicketUpdateRequest.class);
        TicketResponse updated = new TicketResponse(10L, "Sem acesso", "Não consigo entrar", 2, 2,
                userSummary, null, LocalDateTime.now(), LocalDateTime.now(), null);
        when(currentUserService.requireUser(authentication)).thenReturn(user);
        when(ticketService.updateTicket(10L, user, updateRequest)).thenReturn(updated);

        TicketResponse result = controller.updateTicket(10L, updateRequest, authentication);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.status()).isEqualTo(2);
        verify(ticketService).updateTicket(10L, user, updateRequest);
    }

    @Test
    void listComments_returnsCommentsFromService() {
        CommentResponse commentResponse = new CommentResponse(1L, 10L, userSummary, "Estou verificando.", LocalDateTime.now());
        when(currentUserService.requireUser(authentication)).thenReturn(user);
        when(ticketService.listComments(10L, user)).thenReturn(List.of(commentResponse));

        List<CommentResponse> result = controller.listComments(10L, authentication);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).comment()).isEqualTo("Estou verificando.");
        assertThat(result.get(0).ticketId()).isEqualTo(10L);
    }

    @Test
    void addComment_returnsCreatedCommentFromService() {
        CommentCreateRequest commentRequest = new CommentCreateRequest("Problema identificado.");
        CommentResponse commentResponse = new CommentResponse(2L, 10L, userSummary, "Problema identificado.", LocalDateTime.now());
        when(currentUserService.requireUser(authentication)).thenReturn(user);
        when(ticketService.addComment(10L, user, commentRequest)).thenReturn(commentResponse);

        CommentResponse result = controller.addComment(10L, commentRequest, authentication);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.comment()).isEqualTo("Problema identificado.");
        verify(ticketService).addComment(10L, user, commentRequest);
    }
}
