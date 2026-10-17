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
import br.com.helpdesk.enums.TicketPriority;
import br.com.helpdesk.enums.TicketStatus;
import br.com.helpdesk.enums.UserRole;
import br.com.helpdesk.exceptions.BusinessRuleException;
import br.com.helpdesk.exceptions.ForbiddenOperationException;
import br.com.helpdesk.exceptions.ResourceNotFoundException;
import br.com.helpdesk.repositories.CommentRepository;
import br.com.helpdesk.repositories.TicketRepository;
import br.com.helpdesk.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TicketService ticketService;

    private User admin;
    private User technician;
    private User requester;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setName("Admin");
        admin.setEmail("admin@helpdesk.com");
        admin.setRole(UserRole.ADMIN.getCode());
        admin.setActive(true);

        technician = new User();
        technician.setId(2L);
        technician.setName("Technician");
        technician.setEmail("tech@helpdesk.com");
        technician.setRole(UserRole.TECHNICIAN.getCode());
        technician.setActive(true);

        requester = new User();
        requester.setId(3L);
        requester.setName("Requester");
        requester.setEmail("requester@helpdesk.com");
        requester.setRole(UserRole.REQUESTER.getCode());
        requester.setActive(true);

        ticket = new Ticket();
        ticket.setId(10L);
        ticket.setTitle("Test Ticket");
        ticket.setDescription("Description");
        ticket.setStatus(TicketStatus.OPEN.getCode());
        ticket.setPriority(TicketPriority.MEDIUM.getCode());
        ticket.setRequester(requester);
    }

    // --- listVisibleTickets ---

    @Test
    void listVisibleTickets_returnsAllTicketsForTechnician() {
        when(ticketRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(ticket));

        List<TicketResponse> result = ticketService.listVisibleTickets(technician);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(10L);
    }

    @Test
    void listVisibleTickets_returnsAllTicketsForAdmin() {
        when(ticketRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(ticket));

        List<TicketResponse> result = ticketService.listVisibleTickets(admin);

        assertThat(result).hasSize(1);
    }

    @Test
    void listVisibleTickets_returnsOnlyOwnTicketsForRequester() {
        when(ticketRepository.findByRequesterIdOrderByCreatedAtDesc(3L)).thenReturn(List.of(ticket));

        List<TicketResponse> result = ticketService.listVisibleTickets(requester);

        assertThat(result).hasSize(1);
        verify(ticketRepository).findByRequesterIdOrderByCreatedAtDesc(3L);
    }

    @Test
    void listVisibleTickets_throwsForbiddenWhenActorIsNull() {
        assertThatThrownBy(() -> ticketService.listVisibleTickets(null))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    // --- getDashboardStats ---

    @Test
    void getDashboardStats_countsTicketsByStatus() {
        when(ticketRepository.countGroupedByStatus()).thenReturn(List.of(
                statusCount(TicketStatus.OPEN, 3),
                statusCount(TicketStatus.IN_PROGRESS, 2),
                statusCount(TicketStatus.RESOLVED, 1),
                statusCount(TicketStatus.CLOSED, 4)
        ));

        DashboardStatsResponse stats = ticketService.getDashboardStats(technician);

        assertThat(stats.total()).isEqualTo(10);
        assertThat(stats.open()).isEqualTo(3);
        assertThat(stats.inProgress()).isEqualTo(2);
        assertThat(stats.resolved()).isEqualTo(1);
        assertThat(stats.closed()).isEqualTo(4);
        assertThat(stats.urgent()).isZero();
    }

    @Test
    void getDashboardStats_countsUrgentTicketsThatAreNotClosed() {
        when(ticketRepository.countGroupedByStatus()).thenReturn(List.of(statusCount(TicketStatus.OPEN, 1)));
        when(ticketRepository.countByPriorityAndStatusNot(TicketPriority.URGENT.getCode(), TicketStatus.CLOSED.getCode()))
                .thenReturn(1L);

        DashboardStatsResponse stats = ticketService.getDashboardStats(admin);

        assertThat(stats.urgent()).isEqualTo(1);
    }

    @Test
    void getDashboardStats_requesterOnlyCountsOwnTickets() {
        when(ticketRepository.countGroupedByStatusForRequester(3L)).thenReturn(List.of(statusCount(TicketStatus.OPEN, 2)));
        when(ticketRepository.countByRequesterIdAndPriorityAndStatusNot(3L, TicketPriority.URGENT.getCode(), TicketStatus.CLOSED.getCode()))
                .thenReturn(1L);

        DashboardStatsResponse stats = ticketService.getDashboardStats(requester);

        assertThat(stats.total()).isEqualTo(2);
        assertThat(stats.open()).isEqualTo(2);
        assertThat(stats.urgent()).isEqualTo(1);
        verify(ticketRepository, never()).countGroupedByStatus();
    }

    @Test
    void getDashboardStats_returnsZerosWhenNoTickets() {
        when(ticketRepository.countGroupedByStatusForRequester(3L)).thenReturn(List.of());

        DashboardStatsResponse stats = ticketService.getDashboardStats(requester);

        assertThat(stats.total()).isZero();
        assertThat(stats.open()).isZero();
        assertThat(stats.urgent()).isZero();
    }

    // --- getTicket ---

    @Test
    void getTicket_returnsTicketForTechnician() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        TicketResponse result = ticketService.getTicket(10L, technician);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.title()).isEqualTo("Test Ticket");
    }

    @Test
    void getTicket_returnsOwnTicketForRequester() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        TicketResponse result = ticketService.getTicket(10L, requester);

        assertThat(result.id()).isEqualTo(10L);
    }

    @Test
    void getTicket_throwsForbiddenWhenRequesterAccessesOtherUsersTicket() {
        User otherRequester = new User();
        otherRequester.setId(99L);
        otherRequester.setRole(UserRole.REQUESTER.getCode());
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.getTicket(10L, otherRequester))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void getTicket_throwsResourceNotFoundWhenTicketDoesNotExist() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicket(99L, technician))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- createTicket ---

    @Test
    void createTicket_createsTicketWithOpenStatusWhenNoAssignment() {
        TicketCreateRequest request = new TicketCreateRequest("New Ticket", "Details", TicketPriority.MEDIUM.getCode(), null, null);
        Ticket saved = buildTicket(20L, TicketStatus.OPEN, TicketPriority.MEDIUM);
        saved.setRequester(requester);
        when(ticketRepository.saveAndFlush(any(Ticket.class))).thenReturn(saved);

        TicketResponse result = ticketService.createTicket(requester, request);

        assertThat(result.id()).isEqualTo(20L);
        assertThat(result.status()).isEqualTo(TicketStatus.OPEN.getCode());
    }

    @Test
    void createTicket_setsInProgressStatusWhenAssignedTo() {
        TicketCreateRequest request = new TicketCreateRequest("New Ticket", "Details", TicketPriority.MEDIUM.getCode(), null, 2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(technician));
        Ticket saved = buildTicket(20L, TicketStatus.IN_PROGRESS, TicketPriority.MEDIUM);
        saved.setRequester(requester);
        saved.setAssignedTo(technician);
        when(ticketRepository.saveAndFlush(any(Ticket.class))).thenReturn(saved);

        TicketResponse result = ticketService.createTicket(requester, request);

        assertThat(result.status()).isEqualTo(TicketStatus.IN_PROGRESS.getCode());
    }

    @Test
    void createTicket_throwsForbiddenWhenActorIsNull() {
        TicketCreateRequest request = new TicketCreateRequest("Ticket", "Details", TicketPriority.LOW.getCode(), null, null);

        assertThatThrownBy(() -> ticketService.createTicket(null, request))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void createTicket_throwsForbiddenWhenRequesterCreatesForAnotherUser() {
        TicketCreateRequest request = new TicketCreateRequest("Ticket", "Details", TicketPriority.LOW.getCode(), 99L, null);

        assertThatThrownBy(() -> ticketService.createTicket(requester, request))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void createTicket_throwsBusinessRuleWhenAssignedUserIsNotTechnician() {
        TicketCreateRequest request = new TicketCreateRequest("Ticket", "Details", TicketPriority.LOW.getCode(), null, 3L);
        when(userRepository.findById(3L)).thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> ticketService.createTicket(admin, request))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void createTicket_recordsAuditAfterSave() {
        TicketCreateRequest request = new TicketCreateRequest("Ticket", "Details", TicketPriority.LOW.getCode(), null, null);
        Ticket saved = buildTicket(20L, TicketStatus.OPEN, TicketPriority.LOW);
        saved.setRequester(requester);
        when(ticketRepository.saveAndFlush(any(Ticket.class))).thenReturn(saved);

        ticketService.createTicket(requester, request);

        verify(auditService).record(eq(requester), eq("TICKET_CREATED"), any(), eq(20L), eq(null), any());
    }

    // --- deleteTicket ---

    @Test
    void deleteTicket_deletesTicketForAdmin() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(commentRepository.deleteByTicketId(10L)).thenReturn(0);

        ticketService.deleteTicket(10L, admin);

        verify(ticketRepository).delete(ticket);
    }

    @Test
    void deleteTicket_throwsForbiddenForNonAdmin() {
        assertThatThrownBy(() -> ticketService.deleteTicket(10L, technician))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(ticketRepository, never()).delete(any());
    }

    @Test
    void deleteTicket_throwsForbiddenWhenActorIsNull() {
        assertThatThrownBy(() -> ticketService.deleteTicket(10L, null))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void deleteTicket_deletesCommentsBeforeTicket() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(commentRepository.deleteByTicketId(10L)).thenReturn(3);

        ticketService.deleteTicket(10L, admin);

        verify(commentRepository).deleteByTicketId(10L);
        verify(ticketRepository).delete(ticket);
    }

    // --- updateTicket ---

    @Test
    void updateTicket_updatesStatusWhenProvided() {
        TicketUpdateRequest request = new TicketUpdateRequest(TicketStatus.RESOLVED.getCode(), null, null);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        Ticket saved = buildTicket(10L, TicketStatus.RESOLVED, TicketPriority.MEDIUM);
        saved.setRequester(requester);
        when(ticketRepository.saveAndFlush(any(Ticket.class))).thenReturn(saved);

        TicketResponse result = ticketService.updateTicket(10L, technician, request);

        assertThat(result.status()).isEqualTo(TicketStatus.RESOLVED.getCode());
    }

    @Test
    void updateTicket_updatesPriorityWhenProvided() {
        TicketUpdateRequest request = new TicketUpdateRequest(null, TicketPriority.URGENT.getCode(), null);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        Ticket saved = buildTicket(10L, TicketStatus.OPEN, TicketPriority.URGENT);
        saved.setRequester(requester);
        when(ticketRepository.saveAndFlush(any(Ticket.class))).thenReturn(saved);

        TicketResponse result = ticketService.updateTicket(10L, technician, request);

        assertThat(result.priority()).isEqualTo(TicketPriority.URGENT.getCode());
    }

    @Test
    void updateTicket_throwsForbiddenWhenActorIsRequester() {
        TicketUpdateRequest request = new TicketUpdateRequest(TicketStatus.RESOLVED.getCode(), null, null);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.updateTicket(10L, requester, request))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void updateTicket_recordsStatusAuditWhenStatusChanges() {
        TicketUpdateRequest request = new TicketUpdateRequest(TicketStatus.IN_PROGRESS.getCode(), null, null);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        Ticket saved = buildTicket(10L, TicketStatus.IN_PROGRESS, TicketPriority.MEDIUM);
        saved.setRequester(requester);
        when(ticketRepository.saveAndFlush(any(Ticket.class))).thenReturn(saved);

        ticketService.updateTicket(10L, technician, request);

        verify(auditService).record(eq(technician), eq("TICKET_STATUS_UPDATED"), any(), eq(10L), any(), any());
    }

    @Test
    void updateTicket_assigningTechnicianMovesOpenTicketToInProgress() {
        TicketUpdateRequest request = new TicketUpdateRequest(null, null, 2L);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(technician));
        when(ticketRepository.saveAndFlush(ticket)).thenReturn(ticket);

        TicketResponse result = ticketService.updateTicket(10L, admin, request);

        assertThat(result.status()).isEqualTo(TicketStatus.IN_PROGRESS.getCode());
        assertThat(result.assignedTo().id()).isEqualTo(2L);
        verify(auditService).record(eq(admin), eq("TICKET_ASSIGNED"), any(), eq(10L), any(), any());
        verify(auditService).record(eq(admin), eq("TICKET_STATUS_UPDATED"), any(), eq(10L), any(), any());
    }

    @Test
    void updateTicket_assigningKeepsStatusWhenTicketIsNotOpen() {
        ticket.setStatus(TicketStatus.RESOLVED.getCode());
        TicketUpdateRequest request = new TicketUpdateRequest(null, null, 2L);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(technician));
        when(ticketRepository.saveAndFlush(ticket)).thenReturn(ticket);

        TicketResponse result = ticketService.updateTicket(10L, admin, request);

        assertThat(result.status()).isEqualTo(TicketStatus.RESOLVED.getCode());
    }

    @Test
    void updateTicket_rejectsAssigningUserThatIsNotTechnician() {
        TicketUpdateRequest request = new TicketUpdateRequest(null, null, 3L);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(3L)).thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> ticketService.updateTicket(10L, admin, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Technician");

        verify(ticketRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateTicket_closingSetsClosedAtAndReopeningClearsIt() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.saveAndFlush(ticket)).thenReturn(ticket);

        TicketResponse closed = ticketService.updateTicket(10L, technician, new TicketUpdateRequest(TicketStatus.CLOSED.getCode(), null, null));
        assertThat(closed.closedAt()).isNotNull();

        TicketResponse reopened = ticketService.updateTicket(10L, technician, new TicketUpdateRequest(TicketStatus.OPEN.getCode(), null, null));
        assertThat(reopened.closedAt()).isNull();
    }

    @Test
    void updateTicket_doesNotRecordAuditWhenNothingChanges() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.saveAndFlush(ticket)).thenReturn(ticket);

        ticketService.updateTicket(10L, technician, new TicketUpdateRequest(null, ticket.getPriority(), null));

        verify(auditService, never()).record(any(), any(), any(), any(), any(), any());
    }

    // --- listComments ---

    @Test
    void listComments_returnsCommentsForTechnician() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setTicket(ticket);
        comment.setUser(technician);
        comment.setComment("Checking it.");
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(commentRepository.findByTicketIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(comment));

        List<CommentResponse> result = ticketService.listComments(10L, technician);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).comment()).isEqualTo("Checking it.");
    }

    @Test
    void listComments_throwsForbiddenWhenRequesterAccessesOtherTicket() {
        User otherRequester = new User();
        otherRequester.setId(99L);
        otherRequester.setRole(UserRole.REQUESTER.getCode());
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.listComments(10L, otherRequester))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    // --- addComment ---

    @Test
    void addComment_savesAndReturnsComment() {
        CommentCreateRequest request = new CommentCreateRequest("Issue resolved.");
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        Comment saved = new Comment();
        saved.setId(5L);
        saved.setTicket(ticket);
        saved.setUser(technician);
        saved.setComment("Issue resolved.");
        when(commentRepository.saveAndFlush(any(Comment.class))).thenReturn(saved);

        CommentResponse result = ticketService.addComment(10L, technician, request);

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.comment()).isEqualTo("Issue resolved.");
    }

    @Test
    void addComment_throwsForbiddenWhenRequesterAddsToOtherTicket() {
        User otherRequester = new User();
        otherRequester.setId(99L);
        otherRequester.setRole(UserRole.REQUESTER.getCode());
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.addComment(10L, otherRequester, new CommentCreateRequest("text")))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void addComment_recordsAuditAfterSave() {
        CommentCreateRequest request = new CommentCreateRequest("Note added.");
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        Comment saved = new Comment();
        saved.setId(5L);
        saved.setTicket(ticket);
        saved.setUser(technician);
        saved.setComment("Note added.");
        when(commentRepository.saveAndFlush(any(Comment.class))).thenReturn(saved);

        ticketService.addComment(10L, technician, request);

        verify(auditService).record(eq(technician), eq("COMMENT_ADDED"), any(), eq(5L), eq(null), any());
    }

    // --- helpers ---

    private TicketRepository.StatusCount statusCount(TicketStatus status, long total) {
        return new TicketRepository.StatusCount() {
            @Override
            public Integer getStatus() {
                return status.getCode();
            }

            @Override
            public Long getTotal() {
                return total;
            }
        };
    }

    private Ticket buildTicket(Long id, TicketStatus status, TicketPriority priority) {
        Ticket t = new Ticket();
        t.setId(id);
        t.setTitle("Ticket " + id);
        t.setDescription("Description");
        t.setStatus(status.getCode());
        t.setPriority(priority.getCode());
        t.setRequester(requester);
        return t;
    }
}
