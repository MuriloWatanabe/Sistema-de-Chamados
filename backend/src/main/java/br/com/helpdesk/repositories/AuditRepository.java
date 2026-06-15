package br.com.helpdesk.repositories;

import br.com.helpdesk.entities.Audit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditRepository extends JpaRepository<Audit, Long> {
    @EntityGraph(attributePaths = {"user"})
    List<Audit> findAllByOrderByCreatedAtDesc();

    boolean existsByUserId(Long userId);
}
