package br.com.helpdesk.dtos;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long ticketId,
        UserSummaryResponse user,
        String comment,
        LocalDateTime createdAt
) {
}
