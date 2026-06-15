package br.com.helpdesk.repositories;

import br.com.helpdesk.entities.Ticket;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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

    boolean existsByRequesterId(Long requesterId);

    boolean existsByAssignedToId(Long assignedToId);

    @Override
    @EntityGraph(attributePaths = {"requester", "assignedTo"})
    Optional<Ticket> findById(Long id);
}
