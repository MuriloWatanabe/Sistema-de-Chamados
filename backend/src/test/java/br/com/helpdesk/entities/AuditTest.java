package br.com.helpdesk.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AuditTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Admin");
        user.setEmail("admin@helpdesk.com");
        user.setPassword("hashed_password");
        user.setRole(1);
    }

    @Test
    void noArgsConstructor_createsInstanceWithNullFields() {
        Audit audit = new Audit();

        assertThat(audit.getId()).isNull();
        assertThat(audit.getUser()).isNull();
        assertThat(audit.getAction()).isNull();
        assertThat(audit.getEntityType()).isNull();
        assertThat(audit.getEntityId()).isNull();
        assertThat(audit.getOldValue()).isNull();
        assertThat(audit.getNewValue()).isNull();
        assertThat(audit.getCreatedAt()).isNull();
    }

    @Test
    void setId_andGetId_returnExpectedValue() {
        Audit audit = new Audit();
        audit.setId(42L);

        assertThat(audit.getId()).isEqualTo(42L);
    }

    @Test
    void setUser_andGetUser_returnExpectedUser() {
        Audit audit = new Audit();
        audit.setUser(user);

        assertThat(audit.getUser()).isSameAs(user);
        assertThat(audit.getUser().getId()).isEqualTo(1L);
        assertThat(audit.getUser().getName()).isEqualTo("Admin");
    }

    @Test
    void setAction_andGetAction_returnExpectedValue() {
        Audit audit = new Audit();
        audit.setAction("TICKET_CREATED");

        assertThat(audit.getAction()).isEqualTo("TICKET_CREATED");
    }

    @Test
    void setEntityType_andGetEntityType_returnExpectedValue() {
        Audit audit = new Audit();
        audit.setEntityType(1);

        assertThat(audit.getEntityType()).isEqualTo(1);
    }

    @Test
    void setEntityId_andGetEntityId_returnExpectedValue() {
        Audit audit = new Audit();
        audit.setEntityId(99L);

        assertThat(audit.getEntityId()).isEqualTo(99L);
    }

    @Test
    void setOldValue_andGetOldValue_returnExpectedJsonNode() throws Exception {
        Audit audit = new Audit();
        JsonNode oldValue = OBJECT_MAPPER.readTree("{\"status\": \"OPEN\"}");
        audit.setOldValue(oldValue);

        assertThat(audit.getOldValue()).isNotNull();
        assertThat(audit.getOldValue().get("status").asText()).isEqualTo("OPEN");
    }

    @Test
    void setNewValue_andGetNewValue_returnExpectedJsonNode() throws Exception {
        Audit audit = new Audit();
        JsonNode newValue = OBJECT_MAPPER.readTree("{\"status\": \"CLOSED\"}");
        audit.setNewValue(newValue);

        assertThat(audit.getNewValue()).isNotNull();
        assertThat(audit.getNewValue().get("status").asText()).isEqualTo("CLOSED");
    }

    @Test
    void setOldValueAndNewValue_allowNullValues() {
        Audit audit = new Audit();
        audit.setOldValue(null);
        audit.setNewValue(null);

        assertThat(audit.getOldValue()).isNull();
        assertThat(audit.getNewValue()).isNull();
    }

    @Test
    void setCreatedAt_andGetCreatedAt_returnExpectedTimestamp() {
        Audit audit = new Audit();
        LocalDateTime now = LocalDateTime.now();
        audit.setCreatedAt(now);

        assertThat(audit.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void allFields_setAndGet_workTogether() throws Exception {
        Audit audit = new Audit();
        LocalDateTime now = LocalDateTime.now();
        JsonNode oldValue = OBJECT_MAPPER.readTree("{\"priority\": 1}");
        JsonNode newValue = OBJECT_MAPPER.readTree("{\"priority\": 3}");

        audit.setId(10L);
        audit.setUser(user);
        audit.setAction("TICKET_UPDATED");
        audit.setEntityType(2);
        audit.setEntityId(55L);
        audit.setOldValue(oldValue);
        audit.setNewValue(newValue);
        audit.setCreatedAt(now);

        assertThat(audit.getId()).isEqualTo(10L);
        assertThat(audit.getUser()).isSameAs(user);
        assertThat(audit.getAction()).isEqualTo("TICKET_UPDATED");
        assertThat(audit.getEntityType()).isEqualTo(2);
        assertThat(audit.getEntityId()).isEqualTo(55L);
        assertThat(audit.getOldValue().get("priority").asInt()).isEqualTo(1);
        assertThat(audit.getNewValue().get("priority").asInt()).isEqualTo(3);
        assertThat(audit.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void setUser_withNull_allowsNullUser() {
        Audit audit = new Audit();
        audit.setUser(null);

        assertThat(audit.getUser()).isNull();
    }

    @Test
    void setAction_withDifferentValues_updatesCorrectly() {
        Audit audit = new Audit();

        audit.setAction("COMMENT_ADDED");
        assertThat(audit.getAction()).isEqualTo("COMMENT_ADDED");

        audit.setAction("TICKET_CLOSED");
        assertThat(audit.getAction()).isEqualTo("TICKET_CLOSED");
    }
}
