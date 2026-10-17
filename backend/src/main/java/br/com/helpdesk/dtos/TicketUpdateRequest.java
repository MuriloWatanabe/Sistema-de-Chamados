package br.com.helpdesk.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Atualizacao parcial de um chamado; campos nulos ou omitidos nao sao alterados")
public record TicketUpdateRequest(
        @Schema(description = "Status: 0=Aberto, 1=Em andamento, 2=Resolvido, 3=Fechado", example = "2", nullable = true)
        @Min(0) @Max(3) Integer status,

        @Schema(description = "Prioridade: 0=Baixa, 1=Media, 2=Alta, 3=Urgente", example = "3", nullable = true)
        @Min(0) @Max(3) Integer priority,

        @Schema(description = "ID do tecnico responsavel. Deve ter o perfil Tecnico.", example = "3", nullable = true)
        Long assignedToId
) {
}
