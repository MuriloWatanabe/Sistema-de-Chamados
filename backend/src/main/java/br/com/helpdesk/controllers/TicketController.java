package br.com.helpdesk.controllers;

import br.com.helpdesk.dtos.CommentCreateRequest;
import br.com.helpdesk.dtos.CommentResponse;
import br.com.helpdesk.dtos.TicketCreateRequest;
import br.com.helpdesk.dtos.TicketResponse;
import br.com.helpdesk.dtos.TicketUpdateRequest;
import br.com.helpdesk.services.TicketService;
import br.com.helpdesk.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final CurrentUserService currentUserService;

    public TicketController(TicketService ticketService, CurrentUserService currentUserService) {
        this.ticketService = ticketService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<TicketResponse> listTickets(Authentication authentication) {
        return ticketService.listVisibleTickets(currentUserService.requireUser(authentication));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public TicketResponse getTicket(@PathVariable Long id, Authentication authentication) {
        return ticketService.getTicket(id, currentUserService.requireUser(authentication));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public TicketResponse createTicket(@Valid @RequestBody TicketCreateRequest request, Authentication authentication) {
        return ticketService.createTicket(currentUserService.requireUser(authentication), request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTicket(@PathVariable Long id, Authentication authentication) {
        ticketService.deleteTicket(id, currentUserService.requireUser(authentication));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'SUPERVISOR', 'ADMIN')")
    public TicketResponse updateTicket(@PathVariable Long id, @RequestBody TicketUpdateRequest request, Authentication authentication) {
        return ticketService.updateTicket(id, currentUserService.requireUser(authentication), request);
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public List<CommentResponse> listComments(@PathVariable Long id, Authentication authentication) {
        return ticketService.listComments(id, currentUserService.requireUser(authentication));
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public CommentResponse addComment(@PathVariable Long id, @Valid @RequestBody CommentCreateRequest request, Authentication authentication) {
        return ticketService.addComment(id, currentUserService.requireUser(authentication), request);
    }
}
