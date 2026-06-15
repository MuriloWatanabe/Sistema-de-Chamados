package br.com.helpdesk.services;

import br.com.helpdesk.dtos.UserResponse;
import br.com.helpdesk.dtos.UserUpsertRequest;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.AuditEntityType;
import br.com.helpdesk.enums.UserRole;
import br.com.helpdesk.exceptions.BusinessRuleException;
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

@Service
public class UserService {
    private static final String DEFAULT_PASSWORD = "Helpdesk@123";

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
        validateUniqueEmail(request.email(), null);
        User user = new User();
        applyFields(user, request, true);
        User saved = userRepository.saveAndFlush(user);
        auditService.record(actor, "USER_CREATED", AuditEntityType.USER, saved.getId(), null, DtoMapper.toUserResponse(saved));
        return DtoMapper.toUserResponse(saved);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpsertRequest request, User actor) {
        User user = findUser(id);
        UserResponse before = DtoMapper.toUserResponse(user);
        validateUniqueEmail(request.email(), id);
        applyFields(user, request, false);
        User saved = userRepository.saveAndFlush(user);
        auditService.record(actor, "USER_UPDATED", AuditEntityType.USER, saved.getId(), before, DtoMapper.toUserResponse(saved));
        return DtoMapper.toUserResponse(saved);
    }

    @Transactional
    public void deleteUser(Long id, User actor) {
        User user = findUser(id);
        if (ticketRepository.existsByRequesterId(id) || ticketRepository.existsByAssignedToId(id) || commentRepository.existsByUserId(id) || auditRepository.existsByUserId(id)) {
            throw new BusinessRuleException("User has linked records and cannot be deleted.");
        }
        userRepository.delete(user);
        auditService.record(actor, "USER_DELETED", AuditEntityType.USER, id, DtoMapper.toUserResponse(user), null);
    }

    private void applyFields(User user, UserUpsertRequest request, boolean creating) {
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setActive(request.active() != null ? request.active() : true);
        user.setRole(UserRole.fromCode(request.role()).getCode());

        String rawPassword = request.password();
        if (creating) {
            if (rawPassword == null || rawPassword.isBlank()) {
                rawPassword = DEFAULT_PASSWORD;
            }
            user.setPassword(passwordEncoder.encode(rawPassword));
        } else if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
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
