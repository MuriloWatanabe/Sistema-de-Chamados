package br.com.helpdesk.dtos;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String title,
        String description,
        Integer status,
        Integer priority,
        UserSummaryResponse requester,
        UserSummaryResponse assignedTo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime closedAt
) {
}
