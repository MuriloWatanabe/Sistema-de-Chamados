package br.com.helpdesk.repositories;

import br.com.helpdesk.entities.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = {"ticket", "user"})
    List<Comment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    boolean existsByUserId(Long userId);
}
