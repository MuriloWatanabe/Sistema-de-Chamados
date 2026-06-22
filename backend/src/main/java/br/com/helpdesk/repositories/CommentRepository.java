package br.com.helpdesk.repositories;

import br.com.helpdesk.entities.Comment;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = {"ticket", "user"})
    List<Comment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    boolean existsByUserId(Long userId);

    @Modifying
    @Query("delete from Comment c where c.ticket.id = :ticketId")
    int deleteByTicketId(@Param("ticketId") Long ticketId);
}
