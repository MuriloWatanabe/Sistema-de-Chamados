package br.com.helpdesk.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Novo comentario em um chamado")
public record CommentCreateRequest(
        @Schema(description = "Texto do comentario", example = "Reiniciei a impressora e o problema persiste.")
        @NotBlank @Size(max = 2000) String comment
) {
}
