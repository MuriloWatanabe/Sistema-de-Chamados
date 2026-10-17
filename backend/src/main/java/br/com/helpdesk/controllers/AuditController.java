package br.com.helpdesk.controllers;

import br.com.helpdesk.dtos.AuditResponse;
import br.com.helpdesk.services.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audits")
@Tag(name = "Auditoria", description = "Log de acoes criticas do sistema")
public class AuditController {
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    @Operation(summary = "Listar registros de auditoria",
            description = "Do mais recente para o mais antigo. Somente supervisores e administradores.")
    @ApiResponse(responseCode = "200", description = "Registros de auditoria")
    public List<AuditResponse> listAudits() {
        return auditService.listAll();
    }
}
