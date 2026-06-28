package br.com.helpdesk.enums;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRoleTest {

    @Test
    void admin_hasExpectedCodeAuthorityAndLabel() {
        assertThat(UserRole.ADMIN.getCode()).isEqualTo(0);
        assertThat(UserRole.ADMIN.getAuthority()).isEqualTo("ROLE_ADMIN");
        assertThat(UserRole.ADMIN.getLabel()).isEqualTo("Administrador");
    }

    @Test
    void supervisor_hasExpectedCodeAuthorityAndLabel() {
        assertThat(UserRole.SUPERVISOR.getCode()).isEqualTo(1);
        assertThat(UserRole.SUPERVISOR.getAuthority()).isEqualTo("ROLE_SUPERVISOR");
        assertThat(UserRole.SUPERVISOR.getLabel()).isEqualTo("Supervisor");
    }

    @Test
    void technician_hasExpectedCodeAuthorityAndLabel() {
        assertThat(UserRole.TECHNICIAN.getCode()).isEqualTo(2);
        assertThat(UserRole.TECHNICIAN.getAuthority()).isEqualTo("ROLE_TECHNICIAN");
        assertThat(UserRole.TECHNICIAN.getLabel()).isEqualTo("Tecnico");
    }

    @Test
    void requester_hasExpectedCodeAuthorityAndLabel() {
        assertThat(UserRole.REQUESTER.getCode()).isEqualTo(3);
        assertThat(UserRole.REQUESTER.getAuthority()).isEqualTo("ROLE_REQUESTER");
        assertThat(UserRole.REQUESTER.getLabel()).isEqualTo("Solicitante");
    }

    @Test
    void fromCode_withNull_returnsRequester() {
        assertThat(UserRole.fromCode(null)).isEqualTo(UserRole.REQUESTER);
    }

    @Test
    void fromCode_withZero_returnsAdmin() {
        assertThat(UserRole.fromCode(0)).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void fromCode_withOne_returnsSupervisor() {
        assertThat(UserRole.fromCode(1)).isEqualTo(UserRole.SUPERVISOR);
    }

    @Test
    void fromCode_withTwo_returnsTechnician() {
        assertThat(UserRole.fromCode(2)).isEqualTo(UserRole.TECHNICIAN);
    }

    @Test
    void fromCode_withThree_returnsRequester() {
        assertThat(UserRole.fromCode(3)).isEqualTo(UserRole.REQUESTER);
    }

    @Test
    void fromCode_withUnknownCode_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> UserRole.fromCode(99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void toGrantedAuthority_returnsCorrectAuthority() {
        GrantedAuthority authority = UserRole.ADMIN.toGrantedAuthority();

        assertThat(authority.getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void toGrantedAuthority_eachRole_returnsMatchingAuthority() {
        assertThat(UserRole.ADMIN.toGrantedAuthority().getAuthority()).isEqualTo("ROLE_ADMIN");
        assertThat(UserRole.SUPERVISOR.toGrantedAuthority().getAuthority()).isEqualTo("ROLE_SUPERVISOR");
        assertThat(UserRole.TECHNICIAN.toGrantedAuthority().getAuthority()).isEqualTo("ROLE_TECHNICIAN");
        assertThat(UserRole.REQUESTER.toGrantedAuthority().getAuthority()).isEqualTo("ROLE_REQUESTER");
    }

    @Test
    void hasAccessAtLeast_adminHasAccessToAllRoles() {
        assertThat(UserRole.ADMIN.hasAccessAtLeast(UserRole.ADMIN)).isTrue();
        assertThat(UserRole.ADMIN.hasAccessAtLeast(UserRole.SUPERVISOR)).isTrue();
        assertThat(UserRole.ADMIN.hasAccessAtLeast(UserRole.TECHNICIAN)).isTrue();
        assertThat(UserRole.ADMIN.hasAccessAtLeast(UserRole.REQUESTER)).isTrue();
    }

    @Test
    void hasAccessAtLeast_supervisorHasNoAccessToAdmin() {
        assertThat(UserRole.SUPERVISOR.hasAccessAtLeast(UserRole.ADMIN)).isFalse();
    }

    @Test
    void hasAccessAtLeast_supervisorHasAccessToSupervisorAndBelow() {
        assertThat(UserRole.SUPERVISOR.hasAccessAtLeast(UserRole.SUPERVISOR)).isTrue();
        assertThat(UserRole.SUPERVISOR.hasAccessAtLeast(UserRole.TECHNICIAN)).isTrue();
        assertThat(UserRole.SUPERVISOR.hasAccessAtLeast(UserRole.REQUESTER)).isTrue();
    }

    @Test
    void hasAccessAtLeast_technicianHasNoAccessAboveOwnRole() {
        assertThat(UserRole.TECHNICIAN.hasAccessAtLeast(UserRole.ADMIN)).isFalse();
        assertThat(UserRole.TECHNICIAN.hasAccessAtLeast(UserRole.SUPERVISOR)).isFalse();
    }

    @Test
    void hasAccessAtLeast_technicianHasAccessToTechnicianAndBelow() {
        assertThat(UserRole.TECHNICIAN.hasAccessAtLeast(UserRole.TECHNICIAN)).isTrue();
        assertThat(UserRole.TECHNICIAN.hasAccessAtLeast(UserRole.REQUESTER)).isTrue();
    }

    @Test
    void hasAccessAtLeast_requesterHasAccessOnlyToRequester() {
        assertThat(UserRole.REQUESTER.hasAccessAtLeast(UserRole.ADMIN)).isFalse();
        assertThat(UserRole.REQUESTER.hasAccessAtLeast(UserRole.SUPERVISOR)).isFalse();
        assertThat(UserRole.REQUESTER.hasAccessAtLeast(UserRole.TECHNICIAN)).isFalse();
        assertThat(UserRole.REQUESTER.hasAccessAtLeast(UserRole.REQUESTER)).isTrue();
    }

    @Test
    void orderedByPrivilege_returnsCorrectOrder() {
        List<UserRole> ordered = UserRole.orderedByPrivilege();

        assertThat(ordered).containsExactly(
                UserRole.ADMIN,
                UserRole.SUPERVISOR,
                UserRole.TECHNICIAN,
                UserRole.REQUESTER
        );
    }

    @Test
    void orderedByPrivilege_containsAllFourRoles() {
        assertThat(UserRole.orderedByPrivilege()).hasSize(4);
    }

    @Test
    void values_containsExactlyFourConstants() {
        assertThat(UserRole.values()).hasSize(4);
    }

    @Test
    void allCodes_areUnique() {
        UserRole[] values = UserRole.values();
        long distinctCodes = java.util.Arrays.stream(values)
                .mapToInt(UserRole::getCode)
                .distinct()
                .count();
        assertThat(distinctCodes).isEqualTo(values.length);
    }
}
