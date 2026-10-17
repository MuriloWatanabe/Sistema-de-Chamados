package br.com.helpdesk.dtos;

import br.com.helpdesk.security.PasswordPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criar ou atualizar um usuario")
public record UserUpsertRequest(
        @Schema(description = "Nome completo", example = "Maria Santos")
        @NotBlank @Size(max = 150) String name,

        @Schema(description = "E-mail de login; unico no sistema", example = "maria@helpdesk.com")
        @NotBlank @Email @Size(max = 150) String email,

        @Schema(description = "Senha em texto puro (gravada como hash): 8 a 72 caracteres, sem espacos, com letras e numeros. "
                + "Obrigatoria na criacao; na atualizacao, se omitida, mantem a atual.", example = "Senha123", nullable = true)
        @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE) String password,

        @Schema(description = "Perfil: 0=Administrador, 1=Supervisor, 2=Tecnico, 3=Solicitante. "
                + "Somente administradores podem alterar.", example = "3")
        @NotNull @Min(0) @Max(3) Integer role,

        @Schema(description = "Usuarios inativos nao conseguem fazer login", example = "true")
        @NotNull Boolean active
) {
}
