package br.com.helpdesk.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Credenciais de acesso")
public record LoginRequest(
        @Schema(example = "admin@helpdesk.com")
        @NotBlank @Email @Size(max = 150) String email,

        @Schema(example = "admin123")
        @NotBlank @Size(max = 72) String password
) {
}
