package br.com.helpdesk.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Registro de auditoria")
public record AuditResponse(
        Long id,
        @Schema(description = "Quem executou a acao") UserSummaryResponse user,
        @Schema(description = "Acao executada", example = "TICKET_STATUS_UPDATED") String action,
        @Schema(description = "Tipo da entidade afetada: 0=Usuario, 1=Chamado, 2=Comentario, 3=Autenticacao", example = "1")
        Integer entityType,
        @Schema(description = "ID da entidade afetada", example = "1") Long entityId,
        @Schema(description = "Estado anterior da entidade (JSON livre)", nullable = true) JsonNode oldValue,
        @Schema(description = "Estado posterior da entidade (JSON livre)", nullable = true) JsonNode newValue,
        LocalDateTime createdAt
) {
}
