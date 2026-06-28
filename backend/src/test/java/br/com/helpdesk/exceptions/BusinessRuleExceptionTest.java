package br.com.helpdesk.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BusinessRuleExceptionTest {

    @Test
    void constructor_setsMessageCorrectly() {
        BusinessRuleException ex = new BusinessRuleException("Regra de negócio violada.");

        assertThat(ex.getMessage()).isEqualTo("Regra de negócio violada.");
    }

    @Test
    void isInstanceOfRuntimeException() {
        BusinessRuleException ex = new BusinessRuleException("erro");

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void canBeThrownAndCaught() {
        assertThatThrownBy(() -> { throw new BusinessRuleException("chamado já fechado"); })
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("chamado já fechado");
    }
}
