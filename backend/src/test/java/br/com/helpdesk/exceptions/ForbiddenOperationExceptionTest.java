package br.com.helpdesk.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ForbiddenOperationExceptionTest {

    @Test
    void constructor_setsMessageCorrectly() {
        ForbiddenOperationException ex = new ForbiddenOperationException("Operação não permitida.");

        assertThat(ex.getMessage()).isEqualTo("Operação não permitida.");
    }

    @Test
    void isInstanceOfRuntimeException() {
        ForbiddenOperationException ex = new ForbiddenOperationException("erro");

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void canBeThrownAndCaught() {
        assertThatThrownBy(() -> { throw new ForbiddenOperationException("acesso negado"); })
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("acesso negado");
    }
}
