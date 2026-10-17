package br.com.helpdesk.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para abrir um chamado")
public record TicketCreateRequest(
        @Schema(description = "Resumo do problema", example = "Impressora offline no setor financeiro")
        @NotBlank @Size(max = 200) String title,

        @Schema(description = "Descricao detalhada do problema",
                example = "A impressora HP LaserJet do financeiro nao imprime desde ontem.")
        @NotBlank @Size(max = 5000) String description,

        @Schema(description = "Prioridade: 0=Baixa, 1=Media, 2=Alta, 3=Urgente", example = "2")
        @NotNull @Min(0) @Max(3) Integer priority,

        @Schema(description = "ID do solicitante. Opcional; se omitido, o chamado e aberto em nome de quem chama. "
                + "Apenas tecnicos ou perfis superiores podem informar outro usuario.", nullable = true)
        Long requesterId,

        @Schema(description = "ID do tecnico responsavel (opcional). Deve ter o perfil Tecnico.", nullable = true)
        Long assignedToId
) {
}
