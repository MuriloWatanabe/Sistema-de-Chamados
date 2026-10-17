package br.com.helpdesk.repositories;

import br.com.helpdesk.entities.Ticket;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Override
    @EntityGraph(attributePaths = {"requester", "assignedTo"})
    List<Ticket> findAll();

    @EntityGraph(attributePaths = {"requester", "assignedTo"})
    List<Ticket> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"requester", "assignedTo"})
    List<Ticket> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    @Query("select t.status as status, count(t) as total from Ticket t group by t.status")
    List<StatusCount> countGroupedByStatus();

    @Query("select t.status as status, count(t) as total from Ticket t where t.requester.id = :requesterId group by t.status")
    List<StatusCount> countGroupedByStatusForRequester(@Param("requesterId") Long requesterId);

    long countByPriorityAndStatusNot(Integer priority, Integer status);

    long countByRequesterIdAndPriorityAndStatusNot(Long requesterId, Integer priority, Integer status);

    boolean existsByRequesterId(Long requesterId);

    boolean existsByAssignedToId(Long assignedToId);

    @Override
    @EntityGraph(attributePaths = {"requester", "assignedTo"})
    Optional<Ticket> findById(Long id);

    interface StatusCount {
        Integer getStatus();

        Long getTotal();
    }
}
