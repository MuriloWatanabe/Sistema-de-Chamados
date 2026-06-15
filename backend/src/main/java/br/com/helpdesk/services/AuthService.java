package br.com.helpdesk.services;

import br.com.helpdesk.dtos.LoginRequest;
import br.com.helpdesk.dtos.LoginResponse;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.AuditEntityType;
import br.com.helpdesk.exceptions.InvalidCredentialsException;
import br.com.helpdesk.repositories.UserRepository;
import br.com.helpdesk.security.JwtService;
import br.com.helpdesk.utils.DtoMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .filter(found -> Boolean.TRUE.equals(found.getActive()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid e-mail or password."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid e-mail or password.");
        }

        String token = jwtService.generateToken(user);
        auditService.record(user, "AUTH_LOGIN", AuditEntityType.AUTH, user.getId(), null, DtoMapper.toUserResponse(user));
        return new LoginResponse(token, "Bearer", DtoMapper.toUserResponse(user));
    }
}
