package br.com.helpdesk.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado do login")
public record LoginResponse(
        @Schema(description = "JWT a enviar no header Authorization das proximas requisicoes") String token,
        @Schema(example = "Bearer") String tokenType,
        UserResponse user
) {
}
