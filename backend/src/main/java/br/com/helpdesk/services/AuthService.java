package br.com.helpdesk.services;

import br.com.helpdesk.dtos.ChangePasswordRequest;
import br.com.helpdesk.dtos.LoginRequest;
import br.com.helpdesk.dtos.LoginResponse;
import br.com.helpdesk.dtos.UserResponse;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.AuditEntityType;
import br.com.helpdesk.exceptions.BusinessRuleException;
import br.com.helpdesk.exceptions.InvalidCredentialsException;
import br.com.helpdesk.exceptions.TooManyRequestsException;
import br.com.helpdesk.repositories.UserRepository;
import br.com.helpdesk.security.JwtService;
import br.com.helpdesk.security.LoginAttemptService;
import br.com.helpdesk.utils.DtoMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private static final String INVALID_CREDENTIALS = "Invalid e-mail or password.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final LoginAttemptService loginAttemptService;
    private volatile String dummyHash;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuditService auditService,
            LoginAttemptService loginAttemptService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.loginAttemptService = loginAttemptService;
    }

    // noRollbackFor: a auditoria de tentativa invalida precisa ser gravada mesmo com a excecao.
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public LoginResponse login(LoginRequest request) {
        long lockedFor = loginAttemptService.remainingLockSeconds(request.email());
        if (lockedFor > 0) {
            throw new TooManyRequestsException("Too many failed login attempts. Try again later.", lockedFor);
        }

        Optional<User> found = userRepository.findByEmailIgnoreCase(request.email());
        // Sempre roda o BCrypt, mesmo sem usuario, para o tempo de resposta nao revelar quais e-mails existem.
        String hash = found.map(User::getPassword).orElseGet(this::dummyHash);
        boolean passwordMatches = passwordEncoder.matches(request.password(), hash);

        User user = found.orElse(null);
        if (user == null || !passwordMatches || !Boolean.TRUE.equals(user.getActive())) {
            loginAttemptService.registerFailure(request.email());
            if (user != null) {
                auditService.record(user, "AUTH_LOGIN_FAILED", AuditEntityType.AUTH, user.getId(), null,
                        Map.of("reason", passwordMatches ? "inactive_user" : "invalid_password"));
            }
            throw new InvalidCredentialsException(INVALID_CREDENTIALS);
        }

        loginAttemptService.registerSuccess(request.email());
        String token = jwtService.generateToken(user);
        auditService.record(user, "AUTH_LOGIN", AuditEntityType.AUTH, user.getId(), null, DtoMapper.toUserResponse(user));
        return new LoginResponse(token, "Bearer", DtoMapper.toUserResponse(user));
    }

    /** Encerra todas as sessoes do usuario: tokens emitidos antes deixam de ser aceitos. */
    @Transactional
    public void logout(User user) {
        user.revokeTokens();
        userRepository.saveAndFlush(user);
        auditService.record(user, "AUTH_LOGOUT", AuditEntityType.AUTH, user.getId(), null, null);
    }

    @Transactional
    public LoginResponse changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessRuleException("Current password is incorrect.");
        }
        if (request.currentPassword().equals(request.newPassword())) {
            throw new BusinessRuleException("New password must be different from the current password.");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.revokeTokens();
        User saved = userRepository.saveAndFlush(user);
        auditService.record(saved, "AUTH_PASSWORD_CHANGED", AuditEntityType.AUTH, saved.getId(), null, null);
        // Os tokens antigos foram revogados; devolve um novo para a sessao atual continuar.
        return new LoginResponse(jwtService.generateToken(saved), "Bearer", DtoMapper.toUserResponse(saved));
    }

    public UserResponse describe(User user) {
        return DtoMapper.toUserResponse(user);
    }

    private String dummyHash() {
        if (dummyHash == null) {
            dummyHash = passwordEncoder.encode("dummy-password-for-timing-protection");
        }
        return dummyHash;
    }
}
