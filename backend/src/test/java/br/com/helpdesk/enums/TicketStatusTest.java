package br.com.helpdesk.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketStatusTest {

    @Test
    void open_hasExpectedCodeAndLabel() {
        assertThat(TicketStatus.OPEN.getCode()).isEqualTo(0);
        assertThat(TicketStatus.OPEN.getLabel()).isEqualTo("Aberto");
    }

    @Test
    void inProgress_hasExpectedCodeAndLabel() {
        assertThat(TicketStatus.IN_PROGRESS.getCode()).isEqualTo(1);
        assertThat(TicketStatus.IN_PROGRESS.getLabel()).isEqualTo("Em andamento");
    }

    @Test
    void resolved_hasExpectedCodeAndLabel() {
        assertThat(TicketStatus.RESOLVED.getCode()).isEqualTo(2);
        assertThat(TicketStatus.RESOLVED.getLabel()).isEqualTo("Resolvido");
    }

    @Test
    void closed_hasExpectedCodeAndLabel() {
        assertThat(TicketStatus.CLOSED.getCode()).isEqualTo(3);
        assertThat(TicketStatus.CLOSED.getLabel()).isEqualTo("Fechado");
    }

    @Test
    void fromCode_withNull_returnsOpen() {
        assertThat(TicketStatus.fromCode(null)).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void fromCode_withZero_returnsOpen() {
        assertThat(TicketStatus.fromCode(0)).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void fromCode_withOne_returnsInProgress() {
        assertThat(TicketStatus.fromCode(1)).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    void fromCode_withTwo_returnsResolved() {
        assertThat(TicketStatus.fromCode(2)).isEqualTo(TicketStatus.RESOLVED);
    }

    @Test
    void fromCode_withThree_returnsClosed() {
        assertThat(TicketStatus.fromCode(3)).isEqualTo(TicketStatus.CLOSED);
    }

    @Test
    void fromCode_withUnknownCode_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> TicketStatus.fromCode(99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void isClosed_onClosedStatus_returnsTrue() {
        assertThat(TicketStatus.CLOSED.isClosed()).isTrue();
    }

    @Test
    void isClosed_onOpenStatus_returnsFalse() {
        assertThat(TicketStatus.OPEN.isClosed()).isFalse();
    }

    @Test
    void isClosed_onInProgressStatus_returnsFalse() {
        assertThat(TicketStatus.IN_PROGRESS.isClosed()).isFalse();
    }

    @Test
    void isClosed_onResolvedStatus_returnsFalse() {
        assertThat(TicketStatus.RESOLVED.isClosed()).isFalse();
    }

    @Test
    void values_containsExactlyFourConstants() {
        assertThat(TicketStatus.values()).hasSize(4);
    }

    @Test
    void allCodes_areUnique() {
        TicketStatus[] values = TicketStatus.values();
        long distinctCodes = java.util.Arrays.stream(values)
                .mapToInt(TicketStatus::getCode)
                .distinct()
                .count();
        assertThat(distinctCodes).isEqualTo(values.length);
    }
}
