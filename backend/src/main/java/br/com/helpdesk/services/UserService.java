package br.com.helpdesk.services;

import br.com.helpdesk.dtos.UserResponse;
import br.com.helpdesk.dtos.UserUpsertRequest;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.AuditEntityType;
import br.com.helpdesk.enums.UserRole;
import br.com.helpdesk.exceptions.BusinessRuleException;
import br.com.helpdesk.exceptions.ForbiddenOperationException;
import br.com.helpdesk.exceptions.ResourceNotFoundException;
import br.com.helpdesk.repositories.AuditRepository;
import br.com.helpdesk.repositories.CommentRepository;
import br.com.helpdesk.repositories.TicketRepository;
import br.com.helpdesk.repositories.UserRepository;
import br.com.helpdesk.utils.DtoMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final AuditRepository auditRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(
            UserRepository userRepository,
            TicketRepository ticketRepository,
            CommentRepository commentRepository,
            AuditRepository auditRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.auditRepository = auditRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findByOrderByNameAsc().stream()
                .map(DtoMapper::toUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listTechnicians() {
        return userRepository.findByActiveTrueAndRoleOrderByNameAsc(UserRole.TECHNICIAN.getCode()).stream()
                .map(DtoMapper::toUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return DtoMapper.toUserResponse(findUser(id));
    }

    @Transactional(readOnly = true)
    public User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Transactional
    public UserResponse createUser(UserUpsertRequest request, User actor) {
        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessRuleException("Password is required when creating a user.");
        }
        assertCanAssignRole(actor, UserRole.fromCode(request.role()));
        validateUniqueEmail(request.email(), null);
        User user = new User();
        applyFields(user, request);
        User saved = userRepository.saveAndFlush(user);
        auditService.record(actor, "USER_CREATED", AuditEntityType.USER, saved.getId(), null, DtoMapper.toUserResponse(saved));
        return DtoMapper.toUserResponse(saved);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpsertRequest request, User actor) {
        User user = findUser(id);
        assertCanUpdate(actor, user, request);
        UserResponse before = DtoMapper.toUserResponse(user);
        validateUniqueEmail(request.email(), id);
        String previousPassword = user.getPassword();
        applyFields(user, request);

        boolean roleChanged = !Objects.equals(before.role(), user.getRole());
        boolean deactivated = Boolean.TRUE.equals(before.active()) && !Boolean.TRUE.equals(user.getActive());
        boolean passwordChanged = !Objects.equals(previousPassword, user.getPassword());
        if (roleChanged || deactivated || passwordChanged) {
            // Sessoes abertas com o perfil/senha antigos deixam de valer.
            user.revokeTokens();
        }

        User saved = userRepository.saveAndFlush(user);
        UserResponse after = DtoMapper.toUserResponse(saved);
        auditService.record(actor, "USER_UPDATED", AuditEntityType.USER, saved.getId(), before, after);
        if (roleChanged) {
            auditService.record(actor, "USER_ROLE_CHANGED", AuditEntityType.USER, saved.getId(),
                    Map.of("role", before.role()), Map.of("role", after.role()));
        }
        if (passwordChanged) {
            auditService.record(actor, "USER_PASSWORD_CHANGED", AuditEntityType.USER, saved.getId(), null, null);
        }
        return after;
    }

    @Transactional
    public void deleteUser(Long id, User actor) {
        if (actor != null && Objects.equals(actor.getId(), id)) {
            throw new BusinessRuleException("You cannot delete your own user.");
        }
        User user = findUser(id);
        if (ticketRepository.existsByRequesterId(id) || ticketRepository.existsByAssignedToId(id) || commentRepository.existsByUserId(id) || auditRepository.existsByUserId(id)) {
            throw new BusinessRuleException("User has linked records and cannot be deleted.");
        }
        userRepository.delete(user);
        auditService.record(actor, "USER_DELETED", AuditEntityType.USER, id, DtoMapper.toUserResponse(user), null);
    }

    private void applyFields(User user, UserUpsertRequest request) {
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setActive(request.active() != null ? request.active() : true);
        user.setRole(UserRole.fromCode(request.role()).getCode());

        String rawPassword = request.password();
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
    }

    private void assertCanUpdate(User actor, User target, UserUpsertRequest request) {
        if (actor == null) {
            throw new ForbiddenOperationException("Authentication required.");
        }
        UserRole actorRole = UserRole.fromCode(actor.getRole());
        UserRole currentRole = UserRole.fromCode(target.getRole());
        UserRole requestedRole = UserRole.fromCode(request.role());

        if (actorRole != UserRole.ADMIN && currentRole == UserRole.ADMIN) {
            throw new ForbiddenOperationException("Only administrators can edit administrator accounts.");
        }
        if (requestedRole != currentRole) {
            assertCanAssignRole(actor, requestedRole);
        }
        if (Objects.equals(actor.getId(), target.getId())) {
            if (requestedRole != currentRole) {
                throw new BusinessRuleException("You cannot change your own role.");
            }
            if (!Boolean.TRUE.equals(request.active())) {
                throw new BusinessRuleException("You cannot deactivate your own user.");
            }
        }
    }

    // Matriz de permissoes: somente o administrador altera permissoes (perfis) de usuarios.
    private void assertCanAssignRole(User actor, UserRole role) {
        if (actor == null || UserRole.fromCode(actor.getRole()) != UserRole.ADMIN) {
            throw new ForbiddenOperationException("Only administrators can change user roles.");
        }
    }

    private void validateUniqueEmail(String email, Long currentId) {
        boolean exists = currentId == null
                ? userRepository.existsByEmailIgnoreCase(email)
                : userRepository.existsByEmailIgnoreCaseAndIdNot(email, currentId);
        if (exists) {
            throw new BusinessRuleException("Email already registered: " + email);
        }
    }
}
