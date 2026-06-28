package br.com.helpdesk.exceptions;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorResponseTest {

    @Test
    void record_storesAllFieldsCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        ApiErrorResponse response = new ApiErrorResponse(now, 404, "Not Found", "Recurso não encontrado.", "/api/tickets/99");

        assertThat(response.timestamp()).isEqualTo(now);
        assertThat(response.status()).isEqualTo(404);
        assertThat(response.error()).isEqualTo("Not Found");
        assertThat(response.message()).isEqualTo("Recurso não encontrado.");
        assertThat(response.path()).isEqualTo("/api/tickets/99");
    }

    @Test
    void twoRecordsWithSameValues_areEqual() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);

        ApiErrorResponse r1 = new ApiErrorResponse(now, 400, "Bad Request", "Erro.", "/api/test");
        ApiErrorResponse r2 = new ApiErrorResponse(now, 400, "Bad Request", "Erro.", "/api/test");

        assertThat(r1).isEqualTo(r2);
    }

    @Test
    void twoRecordsWithDifferentValues_areNotEqual() {
        LocalDateTime now = LocalDateTime.now();

        ApiErrorResponse r1 = new ApiErrorResponse(now, 400, "Bad Request", "Erro A.", "/api/a");
        ApiErrorResponse r2 = new ApiErrorResponse(now, 500, "Internal Server Error", "Erro B.", "/api/b");

        assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    void record_statusField_storesIntegerValue() {
        ApiErrorResponse response = new ApiErrorResponse(LocalDateTime.now(), 500, "Internal Server Error", "Erro inesperado.", "/api/test");

        assertThat(response.status()).isEqualTo(500);
    }
}
