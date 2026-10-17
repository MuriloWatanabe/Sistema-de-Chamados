package br.com.helpdesk.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Usuario")
public record UserResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Maria Santos") String name,
        @Schema(example = "maria@helpdesk.com") String email,
        Boolean active,
        @Schema(description = "Perfil: 0=Administrador, 1=Supervisor, 2=Tecnico, 3=Solicitante", example = "3") Integer role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
