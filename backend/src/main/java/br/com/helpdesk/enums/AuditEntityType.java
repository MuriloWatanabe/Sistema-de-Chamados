package br.com.helpdesk.enums;

import java.util.Arrays;

public enum AuditEntityType {
    USER(0, "USER"),
    TICKET(1, "TICKET"),
    COMMENT(2, "COMMENT"),
    AUTH(3, "AUTH");

    private final int code;
    private final String label;

    AuditEntityType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static AuditEntityType fromCode(Integer code) {
        if (code == null) {
            return AUTH;
        }
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown audit entity type: " + code));
    }
}
