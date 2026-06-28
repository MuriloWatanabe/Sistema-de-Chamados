package br.com.helpdesk.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvalidCredentialsExceptionTest {

    @Test
    void constructor_setsMessageCorrectly() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Email ou senha inválidos.");

        assertThat(ex.getMessage()).isEqualTo("Email ou senha inválidos.");
    }

    @Test
    void isInstanceOfRuntimeException() {
        InvalidCredentialsException ex = new InvalidCredentialsException("erro");

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void canBeThrownAndCaught() {
        assertThatThrownBy(() -> { throw new InvalidCredentialsException("credenciais inválidas"); })
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("credenciais inválidas");
    }
}
