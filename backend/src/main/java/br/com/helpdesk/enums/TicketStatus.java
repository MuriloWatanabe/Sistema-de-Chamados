package br.com.helpdesk.enums;

import java.util.Arrays;

public enum TicketStatus {
    OPEN(0, "Aberto"),
    IN_PROGRESS(1, "Em andamento"),
    RESOLVED(2, "Resolvido"),
    CLOSED(3, "Fechado");

    private final int code;
    private final String label;

    TicketStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static TicketStatus fromCode(Integer code) {
        if (code == null) {
            return OPEN;
        }
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown ticket status: " + code));
    }

    public boolean isClosed() {
        return this == CLOSED;
    }
}
