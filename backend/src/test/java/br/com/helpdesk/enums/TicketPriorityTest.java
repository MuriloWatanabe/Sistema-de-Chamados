package br.com.helpdesk.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketPriorityTest {

    @Test
    void low_hasExpectedCodeAndLabel() {
        assertThat(TicketPriority.LOW.getCode()).isEqualTo(0);
        assertThat(TicketPriority.LOW.getLabel()).isEqualTo("Baixa");
    }

    @Test
    void medium_hasExpectedCodeAndLabel() {
        assertThat(TicketPriority.MEDIUM.getCode()).isEqualTo(1);
        assertThat(TicketPriority.MEDIUM.getLabel()).isEqualTo("Media");
    }

    @Test
    void high_hasExpectedCodeAndLabel() {
        assertThat(TicketPriority.HIGH.getCode()).isEqualTo(2);
        assertThat(TicketPriority.HIGH.getLabel()).isEqualTo("Alta");
    }

    @Test
    void urgent_hasExpectedCodeAndLabel() {
        assertThat(TicketPriority.URGENT.getCode()).isEqualTo(3);
        assertThat(TicketPriority.URGENT.getLabel()).isEqualTo("Urgente");
    }

    @Test
    void fromCode_withNull_returnsMedium() {
        assertThat(TicketPriority.fromCode(null)).isEqualTo(TicketPriority.MEDIUM);
    }

    @Test
    void fromCode_withZero_returnsLow() {
        assertThat(TicketPriority.fromCode(0)).isEqualTo(TicketPriority.LOW);
    }

    @Test
    void fromCode_withOne_returnsMedium() {
        assertThat(TicketPriority.fromCode(1)).isEqualTo(TicketPriority.MEDIUM);
    }

    @Test
    void fromCode_withTwo_returnsHigh() {
        assertThat(TicketPriority.fromCode(2)).isEqualTo(TicketPriority.HIGH);
    }

    @Test
    void fromCode_withThree_returnsUrgent() {
        assertThat(TicketPriority.fromCode(3)).isEqualTo(TicketPriority.URGENT);
    }

    @Test
    void fromCode_withUnknownCode_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> TicketPriority.fromCode(99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void values_containsExactlyFourConstants() {
        assertThat(TicketPriority.values()).hasSize(4);
    }

    @Test
    void allCodes_areUnique() {
        TicketPriority[] values = TicketPriority.values();
        long distinctCodes = java.util.Arrays.stream(values)
                .mapToInt(TicketPriority::getCode)
                .distinct()
                .count();
        assertThat(distinctCodes).isEqualTo(values.length);
    }
}
