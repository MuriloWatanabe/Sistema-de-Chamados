package br.com.helpdesk.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditEntityTypeTest {

    @Test
    void user_hasExpectedCodeAndLabel() {
        assertThat(AuditEntityType.USER.getCode()).isEqualTo(0);
        assertThat(AuditEntityType.USER.getLabel()).isEqualTo("USER");
    }

    @Test
    void ticket_hasExpectedCodeAndLabel() {
        assertThat(AuditEntityType.TICKET.getCode()).isEqualTo(1);
        assertThat(AuditEntityType.TICKET.getLabel()).isEqualTo("TICKET");
    }

    @Test
    void comment_hasExpectedCodeAndLabel() {
        assertThat(AuditEntityType.COMMENT.getCode()).isEqualTo(2);
        assertThat(AuditEntityType.COMMENT.getLabel()).isEqualTo("COMMENT");
    }

    @Test
    void auth_hasExpectedCodeAndLabel() {
        assertThat(AuditEntityType.AUTH.getCode()).isEqualTo(3);
        assertThat(AuditEntityType.AUTH.getLabel()).isEqualTo("AUTH");
    }

    @Test
    void fromCode_withNull_returnsAuth() {
        assertThat(AuditEntityType.fromCode(null)).isEqualTo(AuditEntityType.AUTH);
    }

    @Test
    void fromCode_withZero_returnsUser() {
        assertThat(AuditEntityType.fromCode(0)).isEqualTo(AuditEntityType.USER);
    }

    @Test
    void fromCode_withOne_returnsTicket() {
        assertThat(AuditEntityType.fromCode(1)).isEqualTo(AuditEntityType.TICKET);
    }

    @Test
    void fromCode_withTwo_returnsComment() {
        assertThat(AuditEntityType.fromCode(2)).isEqualTo(AuditEntityType.COMMENT);
    }

    @Test
    void fromCode_withThree_returnsAuth() {
        assertThat(AuditEntityType.fromCode(3)).isEqualTo(AuditEntityType.AUTH);
    }

    @Test
    void fromCode_withUnknownCode_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> AuditEntityType.fromCode(99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void values_containsExactlyFourConstants() {
        assertThat(AuditEntityType.values()).hasSize(4);
    }

    @Test
    void allCodes_areUnique() {
        AuditEntityType[] values = AuditEntityType.values();
        long distinctCodes = java.util.Arrays.stream(values)
                .mapToInt(AuditEntityType::getCode)
                .distinct()
                .count();
        assertThat(distinctCodes).isEqualTo(values.length);
    }
}
