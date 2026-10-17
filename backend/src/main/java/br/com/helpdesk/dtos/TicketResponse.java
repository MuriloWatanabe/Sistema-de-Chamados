package br.com.helpdesk.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Chamado")
public record TicketResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Impressora offline no setor financeiro") String title,
        String description,
        @Schema(description = "Status: 0=Aberto, 1=Em andamento, 2=Resolvido, 3=Fechado", example = "1") Integer status,
        @Schema(description = "Prioridade: 0=Baixa, 1=Media, 2=Alta, 3=Urgente", example = "2") Integer priority,
        @Schema(description = "Quem abriu o chamado") UserSummaryResponse requester,
        @Schema(description = "Tecnico responsavel; nulo se ainda nao atribuido", nullable = true) UserSummaryResponse assignedTo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        @Schema(description = "Preenchido quando o status passa para Fechado", nullable = true) LocalDateTime closedAt
) {
}
