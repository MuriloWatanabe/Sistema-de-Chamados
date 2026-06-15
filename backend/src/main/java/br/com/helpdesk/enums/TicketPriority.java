package br.com.helpdesk.enums;

import java.util.Arrays;

public enum TicketPriority {
    LOW(0, "Baixa"),
    MEDIUM(1, "Media"),
    HIGH(2, "Alta"),
    URGENT(3, "Urgente");

    private final int code;
    private final String label;

    TicketPriority(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static TicketPriority fromCode(Integer code) {
        if (code == null) {
            return MEDIUM;
        }
        return Arrays.stream(values())
                .filter(priority -> priority.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown ticket priority: " + code));
    }
}
