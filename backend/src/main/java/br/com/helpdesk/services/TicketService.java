package br.com.helpdesk.services;

import br.com.helpdesk.dtos.CommentCreateRequest;
import br.com.helpdesk.dtos.CommentResponse;
import br.com.helpdesk.dtos.DashboardStatsResponse;
import br.com.helpdesk.dtos.TicketCreateRequest;
import br.com.helpdesk.dtos.TicketResponse;
import br.com.helpdesk.dtos.TicketUpdateRequest;
import br.com.helpdesk.entities.Comment;
import br.com.helpdesk.entities.Ticket;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.AuditEntityType;
import br.com.helpdesk.enums.TicketPriority;
import br.com.helpdesk.enums.TicketStatus;
import br.com.helpdesk.enums.UserRole;
import br.com.helpdesk.exceptions.BusinessRuleException;
import br.com.helpdesk.exceptions.ForbiddenOperationException;
import br.com.helpdesk.exceptions.ResourceNotFoundException;
import br.com.helpdesk.repositories.CommentRepository;
import br.com.helpdesk.repositories.TicketRepository;
import br.com.helpdesk.repositories.UserRepository;
import br.com.helpdesk.utils.DtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public TicketService(
            TicketRepository ticketRepository,
            CommentRepository commentRepository,
            UserRepository userRepository,
            AuditService auditService
    ) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listVisibleTickets(User actor) {
        return visibleTickets(actor).stream()
                .map(DtoMapper::toTicketResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats(User actor) {
        List<Ticket> visibleTickets = visibleTickets(actor);
        long total = visibleTickets.size();
        long open = visibleTickets.stream().filter(ticket -> ticket.getStatus() == TicketStatus.OPEN.getCode()).count();
        long inProgress = visibleTickets.stream().filter(ticket -> ticket.getStatus() == TicketStatus.IN_PROGRESS.getCode()).count();
        long resolved = visibleTickets.stream().filter(ticket -> ticket.getStatus() == TicketStatus.RESOLVED.getCode()).count();
        long closed = visibleTickets.stream().filter(ticket -> ticket.getStatus() == TicketStatus.CLOSED.getCode()).count();
        long urgent = visibleTickets.stream()
                .filter(ticket -> ticket.getPriority() == TicketPriority.URGENT.getCode())
                .filter(ticket -> ticket.getStatus() < TicketStatus.CLOSED.getCode())
                .count();
        return new DashboardStatsResponse(total, open, inProgress, resolved, closed, urgent);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicket(Long id, User actor) {
        Ticket ticket = loadTicket(id);
        assertCanViewTicket(actor, ticket);
        return DtoMapper.toTicketResponse(ticket);
    }

    @Transactional
    public TicketResponse createTicket(User actor, TicketCreateRequest request) {
        if (actor == null) {
            throw new ForbiddenOperationException("Authentication required.");
        }
        User requester = resolveRequester(actor, request.requesterId());
        User assignedTo = resolveAssignable(request.assignedToId());

        Ticket ticket = new Ticket();
        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description().trim());
        ticket.setPriority(TicketPriority.fromCode(request.priority()).getCode());
        ticket.setStatus(TicketStatus.OPEN.getCode());
        ticket.setRequester(requester);
        ticket.setAssignedTo(assignedTo);
        if (assignedTo != null) {
            ticket.setStatus(TicketStatus.IN_PROGRESS.getCode());
        }

        Ticket saved = ticketRepository.saveAndFlush(ticket);
        auditService.record(actor, "TICKET_CREATED", AuditEntityType.TICKET, saved.getId(), null, DtoMapper.toTicketResponse(saved));
        return DtoMapper.toTicketResponse(saved);
    }

    @Transactional
    public void deleteTicket(Long id, User actor) {
        assertCanDeleteTickets(actor);
        Ticket ticket = loadTicket(id);
        TicketResponse before = DtoMapper.toTicketResponse(ticket);
        int deletedComments = commentRepository.deleteByTicketId(id);
        ticketRepository.delete(ticket);
        auditService.record(
                actor,
                "TICKET_DELETED",
                AuditEntityType.TICKET,
                id,
                Map.of(
                        "ticket", before,
                        "deletedComments", deletedComments
                ),
                null
        );
    }

    @Transactional
    public TicketResponse updateTicket(Long id, User actor, TicketUpdateRequest request) {
        Ticket ticket = loadTicket(id);
        assertCanManageTickets(actor);
        TicketResponse before = DtoMapper.toTicketResponse(ticket);

        if (request.status() != null) {
            TicketStatus status = TicketStatus.fromCode(request.status());
            ticket.setStatus(status.getCode());
            ticket.setClosedAt(status.isClosed() ? LocalDateTime.now() : null);
        }

        if (request.priority() != null) {
            ticket.setPriority(TicketPriority.fromCode(request.priority()).getCode());
        }

        if (request.assignedToId() != null) {
            User assignedTo = resolveAssignable(request.assignedToId());
            ticket.setAssignedTo(assignedTo);
            if (assignedTo == null && ticket.getStatus() != TicketStatus.CLOSED.getCode()) {
                ticket.setStatus(TicketStatus.OPEN.getCode());
            } else if (assignedTo != null && ticket.getStatus() == TicketStatus.OPEN.getCode()) {
                ticket.setStatus(TicketStatus.IN_PROGRESS.getCode());
            }
        }

        Ticket saved = ticketRepository.saveAndFlush(ticket);
        TicketResponse after = DtoMapper.toTicketResponse(saved);

        if (!Objects.equals(before.status(), after.status())) {
            auditService.record(actor, "TICKET_STATUS_UPDATED", AuditEntityType.TICKET, saved.getId(), before, after);
        }
        if (!Objects.equals(before.priority(), after.priority())) {
            auditService.record(actor, "TICKET_PRIORITY_UPDATED", AuditEntityType.TICKET, saved.getId(), before, after);
        }
        Long beforeAssignedToId = before.assignedTo() == null ? null : before.assignedTo().id();
        Long afterAssignedToId = after.assignedTo() == null ? null : after.assignedTo().id();
        if (!Objects.equals(beforeAssignedToId, afterAssignedToId)) {
            auditService.record(actor, "TICKET_ASSIGNED", AuditEntityType.TICKET, saved.getId(), before, after);
        }

        return after;
    }

    @Transactional
    public TicketResponse updateTicketStatus(Long id, User actor, Integer statusCode) {
        Ticket ticket = loadTicket(id);
        assertCanManageTickets(actor);
        TicketResponse before = DtoMapper.toTicketResponse(ticket);

        TicketStatus status = TicketStatus.fromCode(statusCode);
        ticket.setStatus(status.getCode());
        ticket.setClosedAt(status.isClosed() ? LocalDateTime.now() : null);
        Ticket saved = ticketRepository.saveAndFlush(ticket);
        TicketResponse after = DtoMapper.toTicketResponse(saved);

        if (!Objects.equals(before.status(), after.status())) {
            auditService.record(actor, "TICKET_STATUS_UPDATED", AuditEntityType.TICKET, saved.getId(), before, after);
        }

        return after;
    }

    @Transactional
    public TicketResponse updateTicketPriority(Long id, User actor, Integer priorityCode) {
        Ticket ticket = loadTicket(id);
        assertCanManageTickets(actor);
        TicketResponse before = DtoMapper.toTicketResponse(ticket);

        ticket.setPriority(TicketPriority.fromCode(priorityCode).getCode());
        Ticket saved = ticketRepository.saveAndFlush(ticket);
        TicketResponse after = DtoMapper.toTicketResponse(saved);

        if (!Objects.equals(before.priority(), after.priority())) {
            auditService.record(actor, "TICKET_PRIORITY_UPDATED", AuditEntityType.TICKET, saved.getId(), before, after);
        }

        return after;
    }

    @Transactional
    public TicketResponse assignTicket(Long id, User actor, Long assignedToId) {
        Ticket ticket = loadTicket(id);
        assertCanManageTickets(actor);
        TicketResponse before = DtoMapper.toTicketResponse(ticket);

        User assignedTo = resolveAssignable(assignedToId);
        ticket.setAssignedTo(assignedTo);
        if (assignedTo == null && ticket.getStatus() != TicketStatus.CLOSED.getCode()) {
            ticket.setStatus(TicketStatus.OPEN.getCode());
        } else if (assignedTo != null && ticket.getStatus() == TicketStatus.OPEN.getCode()) {
            ticket.setStatus(TicketStatus.IN_PROGRESS.getCode());
        }

        Ticket saved = ticketRepository.saveAndFlush(ticket);
        TicketResponse after = DtoMapper.toTicketResponse(saved);
        Long beforeAssignedToId = before.assignedTo() == null ? null : before.assignedTo().id();
        Long afterAssignedToId = after.assignedTo() == null ? null : after.assignedTo().id();

        if (!Objects.equals(beforeAssignedToId, afterAssignedToId)) {
            auditService.record(actor, "TICKET_ASSIGNED", AuditEntityType.TICKET, saved.getId(), before, after);
        }

        return after;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(Long ticketId, User actor) {
        Ticket ticket = loadTicket(ticketId);
        assertCanViewTicket(actor, ticket);
        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(DtoMapper::toCommentResponse)
                .toList();
    }

    @Transactional
    public CommentResponse addComment(Long ticketId, User actor, CommentCreateRequest request) {
        Ticket ticket = loadTicket(ticketId);
        assertCanViewTicket(actor, ticket);

        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setUser(actor);
        comment.setComment(request.comment().trim());

        Comment saved = commentRepository.saveAndFlush(comment);
        auditService.record(actor, "COMMENT_ADDED", AuditEntityType.COMMENT, saved.getId(), null, DtoMapper.toCommentResponse(saved));
        return DtoMapper.toCommentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listAssignableTickets(User actor) {
        return listVisibleTickets(actor);
    }

    private List<Ticket> visibleTickets(User actor) {
        if (actor == null) {
            throw new ForbiddenOperationException("Authentication required.");
        }
        if (UserRole.fromCode(actor.getRole()).hasAccessAtLeast(UserRole.TECHNICIAN)) {
            return ticketRepository.findAllByOrderByCreatedAtDesc();
        }
        return ticketRepository.findByRequesterIdOrderByCreatedAtDesc(actor.getId());
    }

    private Ticket loadTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
    }

    private void assertCanViewTicket(User actor, Ticket ticket) {
        if (actor == null) {
            throw new ForbiddenOperationException("Authentication required.");
        }
        if (UserRole.fromCode(actor.getRole()).hasAccessAtLeast(UserRole.TECHNICIAN)) {
            return;
        }
        if (ticket.getRequester() == null || !ticket.getRequester().getId().equals(actor.getId())) {
            throw new ForbiddenOperationException("You can only access your own tickets.");
        }
    }

    private void assertCanManageTickets(User actor) {
        if (actor == null || !UserRole.fromCode(actor.getRole()).hasAccessAtLeast(UserRole.TECHNICIAN)) {
            throw new ForbiddenOperationException("You do not have permission to manage tickets.");
        }
    }

    private void assertCanDeleteTickets(User actor) {
        if (actor == null || !UserRole.fromCode(actor.getRole()).hasAccessAtLeast(UserRole.ADMIN)) {
            throw new ForbiddenOperationException("You do not have permission to delete tickets.");
        }
    }

    private User resolveRequester(User actor, Long requesterId) {
        if (requesterId == null || requesterId.equals(actor.getId())) {
            return actor;
        }
        if (!UserRole.fromCode(actor.getRole()).hasAccessAtLeast(UserRole.TECHNICIAN)) {
            throw new ForbiddenOperationException("You cannot create a ticket on behalf of another user.");
        }
        return findUser(requesterId);
    }

    private User resolveAssignable(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = findUser(userId);
        if (UserRole.fromCode(user.getRole()) != UserRole.TECHNICIAN) {
            throw new BusinessRuleException("Assigned user must have the Technician role.");
        }
        return user;
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
