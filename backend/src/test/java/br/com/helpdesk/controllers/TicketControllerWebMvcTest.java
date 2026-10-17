package br.com.helpdesk.controllers;

import br.com.helpdesk.config.SecurityConfig;
import br.com.helpdesk.dtos.CommentResponse;
import br.com.helpdesk.dtos.TicketCreateRequest;
import br.com.helpdesk.dtos.TicketResponse;
import br.com.helpdesk.dtos.TicketUpdateRequest;
import br.com.helpdesk.dtos.UserSummaryResponse;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.UserRole;
import br.com.helpdesk.exceptions.ForbiddenOperationException;
import br.com.helpdesk.exceptions.ResourceNotFoundException;
import br.com.helpdesk.security.CurrentUserService;
import br.com.helpdesk.security.JwtService;
import br.com.helpdesk.services.TicketService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@Import({SecurityConfig.class, CurrentUserService.class})
class TicketControllerWebMvcTest extends WebMvcSecurityTestSupport {

    @MockitoBean
    private TicketService ticketService;

    private final User admin = user(1L, UserRole.ADMIN);
    private final User technician = user(2L, UserRole.TECHNICIAN);
    private final User requester = user(3L, UserRole.REQUESTER);

    private static final String VALID_TICKET = """
            {"title": "Impressora offline", "description": "Nao imprime desde ontem", "priority": 2}
            """;

    @Nested
    class Authentication {

        @Test
        void requestWithoutToken_returns401WithStandardErrorBody() throws Exception {
            mockMvc.perform(get("/api/tickets"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.path").value("/api/tickets"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verifyNoInteractions(ticketService);
        }

        @Test
        void requestWithInvalidToken_returns401() throws Exception {
            when(jwtService.validateAndParse("expired")).thenThrow(new JwtService.InvalidTokenException("Token expired"));

            mockMvc.perform(get("/api/tickets").header(HttpHeaders.AUTHORIZATION, "Bearer expired"))
                    .andExpect(status().isUnauthorized());

            verifyNoInteractions(ticketService);
        }

        @Test
        void tokenIssuedBeforeRevocation_returns401() throws Exception {
            var request = get("/api/tickets").with(authenticatedAs(requester));
            requester.revokeTokens();

            mockMvc.perform(request)
                    .andExpect(status().isUnauthorized());

            verifyNoInteractions(ticketService);
        }

        @Test
        void tokenOfInactiveUser_returns401() throws Exception {
            requester.setActive(false);

            mockMvc.perform(get("/api/tickets").with(authenticatedAs(requester)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class RoleAuthorization {

        @Test
        void requesterDeletingTicket_returns403NotServerError() throws Exception {
            mockMvc.perform(delete("/api/tickets/{id}", 10L).with(authenticatedAs(requester)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));

            verifyNoInteractions(ticketService);
        }

        @Test
        void technicianDeletingTicket_returns403() throws Exception {
            mockMvc.perform(delete("/api/tickets/{id}", 10L).with(authenticatedAs(technician)))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(ticketService);
        }

        @Test
        void adminDeletingTicket_returns204WithoutBody() throws Exception {
            mockMvc.perform(delete("/api/tickets/{id}", 10L).with(authenticatedAs(admin)))
                    .andExpect(status().isNoContent());

            verify(ticketService).deleteTicket(10L, admin);
        }

        @Test
        void requesterUpdatingTicket_returns403() throws Exception {
            mockMvc.perform(patch("/api/tickets/{id}", 10L).with(authenticatedAs(requester))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": 2}"))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(ticketService);
        }

        @Test
        void serviceLevelForbidden_isMappedTo403() throws Exception {
            when(ticketService.getTicket(10L, requester))
                    .thenThrow(new ForbiddenOperationException("You can only access your own tickets."));

            mockMvc.perform(get("/api/tickets/{id}", 10L).with(authenticatedAs(requester)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("You can only access your own tickets."));
        }
    }

    @Nested
    class CreateTicket {

        @Test
        void validRequest_returns201WithCreatedTicket() throws Exception {
            when(ticketService.createTicket(eq(requester), any(TicketCreateRequest.class))).thenReturn(ticketResponse(20L));

            mockMvc.perform(post("/api/tickets").with(authenticatedAs(requester))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_TICKET))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(20))
                    .andExpect(jsonPath("$.requester.id").value(3));

            verify(ticketService).createTicket(requester, new TicketCreateRequest(
                    "Impressora offline", "Nao imprime desde ontem", 2, null, null));
        }

        @Test
        void blankTitle_returns400BeforeReachingService() throws Exception {
            mockMvc.perform(post("/api/tickets").with(authenticatedAs(requester))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\": \" \", \"description\": \"Nao imprime\", \"priority\": 2}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("title")));

            verifyNoInteractions(ticketService);
        }

        @Test
        void unknownPriorityCode_returns400NotServerError() throws Exception {
            mockMvc.perform(post("/api/tickets").with(authenticatedAs(requester))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\": \"Impressora\", \"description\": \"Nao imprime\", \"priority\": 9}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("priority")));

            verifyNoInteractions(ticketService);
        }

        @Test
        void malformedJson_returns400() throws Exception {
            mockMvc.perform(post("/api/tickets").with(authenticatedAs(requester))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\": "))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Malformed request."));

            verifyNoInteractions(ticketService);
        }
    }

    @Nested
    class ErrorMapping {

        @Test
        void missingTicket_returns404() throws Exception {
            when(ticketService.getTicket(99L, technician)).thenThrow(new ResourceNotFoundException("Ticket not found: 99"));

            mockMvc.perform(get("/api/tickets/{id}", 99L).with(authenticatedAs(technician)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Ticket not found: 99"))
                    .andExpect(jsonPath("$.path").value("/api/tickets/99"));
        }

        @Test
        void nonNumericId_returns400() throws Exception {
            mockMvc.perform(get("/api/tickets/abc").with(authenticatedAs(technician)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Malformed request."));
        }

        @Test
        void unsupportedHttpMethod_returns405() throws Exception {
            mockMvc.perform(put("/api/tickets/{id}", 10L).with(authenticatedAs(admin))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        void invalidStatusOnUpdate_returns400() throws Exception {
            mockMvc.perform(patch("/api/tickets/{id}", 10L).with(authenticatedAs(technician))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": 7}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("status")));

            verifyNoInteractions(ticketService);
        }

        @Test
        void unexpectedException_returns500WithoutLeakingDetails() throws Exception {
            when(ticketService.listVisibleTickets(technician))
                    .thenThrow(new IllegalStateException("could not execute statement; SQL [select ...]"));

            mockMvc.perform(get("/api/tickets").with(authenticatedAs(technician)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Unexpected error."));
        }
    }

    @Test
    void technicianUpdatingTicket_returns200WithUpdatedTicket() throws Exception {
        TicketUpdateRequest request = new TicketUpdateRequest(2, null, null);
        when(ticketService.updateTicket(10L, technician, request)).thenReturn(ticketResponse(10L));

        mockMvc.perform(patch("/api/tickets/{id}", 10L).with(authenticatedAs(technician))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void listTickets_returns200WithVisibleTickets() throws Exception {
        when(ticketService.listVisibleTickets(requester)).thenReturn(List.of(ticketResponse(1L), ticketResponse(2L)));

        mockMvc.perform(get("/api/tickets").with(authenticatedAs(requester)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void addComment_returns201() throws Exception {
        when(ticketService.addComment(eq(10L), eq(requester), any())).thenReturn(new CommentResponse(
                5L, 10L, new UserSummaryResponse(3L, "Requester", "r@helpdesk.com", 3), "Ainda sem solucao", LocalDateTime.now()));

        mockMvc.perform(post("/api/tickets/{id}/comments", 10L).with(authenticatedAs(requester))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\": \"Ainda sem solucao\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    private TicketResponse ticketResponse(Long id) {
        UserSummaryResponse requesterSummary = new UserSummaryResponse(3L, "Requester", "r@helpdesk.com", 3);
        return new TicketResponse(id, "Impressora offline", "Nao imprime desde ontem", 0, 2,
                requesterSummary, null, LocalDateTime.now(), LocalDateTime.now(), null);
    }
}
