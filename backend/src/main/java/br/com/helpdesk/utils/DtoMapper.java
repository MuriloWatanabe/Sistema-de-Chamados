package br.com.helpdesk.utils;

import br.com.helpdesk.dtos.AuditResponse;
import br.com.helpdesk.dtos.CommentResponse;
import br.com.helpdesk.dtos.TicketResponse;
import br.com.helpdesk.dtos.UserResponse;
import br.com.helpdesk.dtos.UserSummaryResponse;
import br.com.helpdesk.entities.Audit;
import br.com.helpdesk.entities.Comment;
import br.com.helpdesk.entities.Ticket;
import br.com.helpdesk.entities.User;

public final class DtoMapper {
    private DtoMapper() {
    }

    public static UserSummaryResponse toUserSummary(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getActive(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public static TicketResponse toTicketResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                toUserSummary(ticket.getRequester()),
                toUserSummary(ticket.getAssignedTo()),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getClosedAt()
        );
    }

    public static CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTicket().getId(),
                toUserSummary(comment.getUser()),
                comment.getComment(),
                comment.getCreatedAt()
        );
    }

    public static AuditResponse toAuditResponse(Audit audit) {
        return new AuditResponse(
                audit.getId(),
                toUserSummary(audit.getUser()),
                audit.getAction(),
                audit.getEntityType(),
                audit.getEntityId(),
                audit.getOldValue(),
                audit.getNewValue(),
                audit.getCreatedAt()
        );
    }
}
