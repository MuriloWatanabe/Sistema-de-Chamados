package br.com.helpdesk.controllers;

import br.com.helpdesk.dtos.AuditResponse;
import br.com.helpdesk.services.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audits")
public class AuditController {
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    public List<AuditResponse> listAudits() {
        return auditService.listAll();
    }
}
