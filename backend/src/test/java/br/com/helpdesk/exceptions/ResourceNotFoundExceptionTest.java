package br.com.helpdesk.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor_setsMessageCorrectly() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Chamado não encontrado.");

        assertThat(ex.getMessage()).isEqualTo("Chamado não encontrado.");
    }

    @Test
    void isInstanceOfRuntimeException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("erro");

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void canBeThrownAndCaught() {
        assertThatThrownBy(() -> { throw new ResourceNotFoundException("usuário não encontrado"); })
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("usuário não encontrado");
    }
}
