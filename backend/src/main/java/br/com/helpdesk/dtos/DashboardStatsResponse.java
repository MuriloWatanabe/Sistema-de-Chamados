package br.com.helpdesk.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Contadores dos chamados visiveis ao usuario")
public record DashboardStatsResponse(
        @Schema(example = "5") long total,
        @Schema(example = "2") long open,
        @Schema(example = "1") long inProgress,
        @Schema(example = "1") long resolved,
        @Schema(example = "1") long closed,
        @Schema(description = "Chamados com prioridade Urgente que ainda nao foram fechados", example = "1") long urgent
) {
}
