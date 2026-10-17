package br.com.helpdesk.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo de usuario embutido em chamados, comentarios e auditoria")
public record UserSummaryResponse(
        @Schema(example = "3") Long id,
        @Schema(example = "Ana Tecnica") String name,
        @Schema(example = "tecnico@helpdesk.com") String email,
        @Schema(description = "Perfil: 0=Administrador, 1=Supervisor, 2=Tecnico, 3=Solicitante", example = "2") Integer role
) {
}
