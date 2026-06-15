package br.com.helpdesk.dtos;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record AuditResponse(
        Long id,
        UserSummaryResponse user,
        String action,
        Integer entityType,
        Long entityId,
        JsonNode oldValue,
        JsonNode newValue,
        LocalDateTime createdAt
) {
}
