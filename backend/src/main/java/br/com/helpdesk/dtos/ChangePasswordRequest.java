package br.com.helpdesk.dtos;

import br.com.helpdesk.security.PasswordPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Troca da propria senha")
public record ChangePasswordRequest(
        @Schema(description = "Senha atual", example = "admin123")
        @NotBlank @Size(max = 72) String currentPassword,

        @Schema(description = "Nova senha: 8 a 72 caracteres, sem espacos, com letras e numeros; diferente da atual",
                example = "NovaSenha2026")
        @NotBlank @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE) String newPassword
) {
}
