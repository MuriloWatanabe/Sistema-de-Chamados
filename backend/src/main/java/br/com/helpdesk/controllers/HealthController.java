package br.com.helpdesk.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Verificacao de disponibilidade")
public class HealthController {
    @GetMapping("/health")
    @SecurityRequirements
    @Operation(summary = "Health check", description = "Responde `{\"status\": \"UP\"}` quando a API esta no ar. Nao exige token.")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
