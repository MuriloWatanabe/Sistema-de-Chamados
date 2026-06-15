package br.com.helpdesk.repositories;

import br.com.helpdesk.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    List<User> findByActiveTrueAndRoleOrderByNameAsc(Integer role);

    List<User> findByOrderByNameAsc();
}
