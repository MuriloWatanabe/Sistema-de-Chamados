package br.com.helpdesk.controllers;

import br.com.helpdesk.dtos.DashboardStatsResponse;
import br.com.helpdesk.services.TicketService;
import br.com.helpdesk.security.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Indicadores de chamados")
public class DashboardController {
    private final TicketService ticketService;
    private final CurrentUserService currentUserService;

    public DashboardController(TicketService ticketService, CurrentUserService currentUserService) {
        this.ticketService = ticketService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Contadores por status",
            description = "Considera apenas os chamados visiveis ao usuario: todos para tecnicos ou perfis "
                    + "superiores, somente os proprios para solicitantes.")
    @ApiResponse(responseCode = "200", description = "Indicadores calculados")
    public DashboardStatsResponse stats(Authentication authentication) {
        return ticketService.getDashboardStats(currentUserService.requireUser(authentication));
    }
}
