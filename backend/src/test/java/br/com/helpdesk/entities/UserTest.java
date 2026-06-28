package br.com.helpdesk.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void noArgsConstructor_createsInstanceWithNullFields() {
        User user = new User();

        assertThat(user.getId()).isNull();
        assertThat(user.getName()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getRole()).isNull();
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
    }

    @Test
    void noArgsConstructor_activeField_defaultsToTrue() {
        User user = new User();

        assertThat(user.getActive()).isTrue();
    }

    @Test
    void setId_andGetId_returnExpectedValue() {
        User user = new User();
        user.setId(1L);

        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    void setName_andGetName_returnExpectedValue() {
        User user = new User();
        user.setName("Maria Souza");

        assertThat(user.getName()).isEqualTo("Maria Souza");
    }

    @Test
    void setEmail_andGetEmail_returnExpectedValue() {
        User user = new User();
        user.setEmail("maria@helpdesk.com");

        assertThat(user.getEmail()).isEqualTo("maria@helpdesk.com");
    }

    @Test
    void setPassword_andGetPassword_returnExpectedValue() {
        User user = new User();
        user.setPassword("$2a$10$hashed_bcrypt_password");

        assertThat(user.getPassword()).isEqualTo("$2a$10$hashed_bcrypt_password");
    }

    @Test
    void setRole_andGetRole_returnExpectedValue() {
        User user = new User();
        user.setRole(0);

        assertThat(user.getRole()).isEqualTo(0);
    }

    @Test
    void setRole_withDifferentValues_updatesCorrectly() {
        User user = new User();

        user.setRole(0);
        assertThat(user.getRole()).isEqualTo(0);

        user.setRole(1);
        assertThat(user.getRole()).isEqualTo(1);
    }

    @Test
    void setActive_toFalse_updatesCorrectly() {
        User user = new User();
        user.setActive(false);

        assertThat(user.getActive()).isFalse();
    }

    @Test
    void setActive_toTrue_updatesCorrectly() {
        User user = new User();
        user.setActive(false);
        user.setActive(true);

        assertThat(user.getActive()).isTrue();
    }

    @Test
    void setCreatedAt_andGetCreatedAt_returnExpectedTimestamp() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);

        assertThat(user.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void setUpdatedAt_andGetUpdatedAt_returnExpectedTimestamp() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        user.setUpdatedAt(now);

        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void allFields_setAndGet_workTogether() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        user.setId(7L);
        user.setName("Carlos Pereira");
        user.setEmail("carlos@helpdesk.com");
        user.setPassword("hashed_password_xyz");
        user.setRole(1);
        user.setActive(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        assertThat(user.getId()).isEqualTo(7L);
        assertThat(user.getName()).isEqualTo("Carlos Pereira");
        assertThat(user.getEmail()).isEqualTo("carlos@helpdesk.com");
        assertThat(user.getPassword()).isEqualTo("hashed_password_xyz");
        assertThat(user.getRole()).isEqualTo(1);
        assertThat(user.getActive()).isTrue();
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void setName_withDifferentValues_updatesCorrectly() {
        User user = new User();

        user.setName("Nome Antigo");
        assertThat(user.getName()).isEqualTo("Nome Antigo");

        user.setName("Nome Novo");
        assertThat(user.getName()).isEqualTo("Nome Novo");
    }

    @Test
    void setEmail_withDifferentValues_updatesCorrectly() {
        User user = new User();

        user.setEmail("antigo@helpdesk.com");
        assertThat(user.getEmail()).isEqualTo("antigo@helpdesk.com");

        user.setEmail("novo@helpdesk.com");
        assertThat(user.getEmail()).isEqualTo("novo@helpdesk.com");
    }
}
