package br.com.helpdesk.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Schema(description = "Corpo padrao de todas as respostas de erro da API")
public record ApiErrorResponse(
        LocalDateTime timestamp,
        @Schema(description = "Codigo HTTP", example = "404") int status,
        @Schema(description = "Descricao padrao do codigo HTTP", example = "Not Found") String error,
        @Schema(description = "Detalhe do erro, exibivel ao usuario", example = "Ticket not found: 42") String message,
        @Schema(description = "Rota chamada", example = "/api/tickets/42") String path
) {
    public static ApiErrorResponse of(HttpStatus status, String message, String path) {
        return new ApiErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path);
    }
}
