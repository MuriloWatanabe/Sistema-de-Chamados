package br.com.helpdesk.controllers;

import br.com.helpdesk.dtos.DashboardStatsResponse;
import br.com.helpdesk.services.TicketService;
import br.com.helpdesk.security.CurrentUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final TicketService ticketService;
    private final CurrentUserService currentUserService;

    public DashboardController(TicketService ticketService, CurrentUserService currentUserService) {
        this.ticketService = ticketService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public DashboardStatsResponse stats(Authentication authentication) {
        return ticketService.getDashboardStats(currentUserService.requireUser(authentication));
    }
}
