package br.com.helpdesk.config;

import br.com.helpdesk.entities.Comment;
import br.com.helpdesk.entities.Ticket;
import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.TicketPriority;
import br.com.helpdesk.enums.TicketStatus;
import br.com.helpdesk.enums.UserRole;
import br.com.helpdesk.repositories.CommentRepository;
import br.com.helpdesk.repositories.TicketRepository;
import br.com.helpdesk.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {
    @Bean
    public CommandLineRunner seedDatabase(
            UserRepository userRepository,
            TicketRepository ticketRepository,
            CommentRepository commentRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }
            seed(userRepository, ticketRepository, commentRepository, passwordEncoder);
        };
    }

    @Transactional
    protected void seed(
            UserRepository userRepository,
            TicketRepository ticketRepository,
            CommentRepository commentRepository,
            PasswordEncoder passwordEncoder
    ) {
        User admin = createUser(userRepository, passwordEncoder, "Admin Sistema", "admin@helpdesk.com", "admin123", UserRole.ADMIN, true);
        User supervisor = createUser(userRepository, passwordEncoder, "Carlos Supervisor", "supervisor@helpdesk.com", "super123", UserRole.SUPERVISOR, true);
        User technician = createUser(userRepository, passwordEncoder, "Ana Tecnica", "tecnico@helpdesk.com", "tecnico123", UserRole.TECHNICIAN, true);
        User requester = createUser(userRepository, passwordEncoder, "Joao Solicitante", "solicitante@helpdesk.com", "solicitante123", UserRole.REQUESTER, true);
        User maria = createUser(userRepository, passwordEncoder, "Maria Santos", "maria@helpdesk.com", "maria123", UserRole.REQUESTER, true);

        Ticket ticket1 = createTicket(ticketRepository, "Computador nao liga", "Meu computador nao esta ligando desde ontem. Ja verifiquei o cabo de energia e esta tudo conectado corretamente.", TicketStatus.OPEN, TicketPriority.HIGH, requester, null);
        Ticket ticket2 = createTicket(ticketRepository, "Sem acesso ao sistema ERP", "Nao consigo fazer login no sistema ERP. Recebo a mensagem de usuario nao encontrado ao tentar entrar.", TicketStatus.IN_PROGRESS, TicketPriority.HIGH, requester, technician);
        Ticket ticket3 = createTicket(ticketRepository, "Impressora offline no setor financeiro", "A impressora HP LaserJet do setor financeiro esta offline e nao esta imprimindo nenhum documento.", TicketStatus.RESOLVED, TicketPriority.MEDIUM, maria, technician);
        Ticket ticket4 = createTicket(ticketRepository, "VPN nao conecta ao trabalhar remoto", "Estou tentando me conectar a VPN da empresa para trabalho remoto, mas recebo erro de timeout apos 30 segundos.", TicketStatus.OPEN, TicketPriority.URGENT, maria, null);
        Ticket ticket5 = createTicket(ticketRepository, "Atualizacao do Windows pendente", "Meu computador esta solicitando atualizacao do Windows mas nao sei se posso instalar durante o horario de trabalho.", TicketStatus.CLOSED, TicketPriority.LOW, requester, technician);

        ticket5.setClosedAt(LocalDateTime.now());
        ticketRepository.save(ticket5);

        commentRepository.saveAll(List.of(
                createComment(ticket2, technician, "Ja identifiquei o problema. O usuario foi bloqueado por tentativas incorretas de login. Vou desbloquear agora."),
                createComment(ticket2, requester, "Obrigado pela rapida resposta! Quando isso estara resolvido?"),
                createComment(ticket3, technician, "Impressora foi reiniciada e reconectada na rede. Teste de impressao realizado com sucesso."),
                createComment(ticket3, maria, "Perfeito! Ja esta funcionando. Muito obrigada!")
        ));
    }

    private User createUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String name,
            String email,
            String rawPassword,
            UserRole role,
            boolean active
    ) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role.getCode());
        user.setActive(active);
        return userRepository.save(user);
    }

    private Ticket createTicket(
            TicketRepository ticketRepository,
            String title,
            String description,
            TicketStatus status,
            TicketPriority priority,
            User requester,
            User assignedTo
    ) {
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setStatus(status.getCode());
        ticket.setPriority(priority.getCode());
        ticket.setRequester(requester);
        ticket.setAssignedTo(assignedTo);
        return ticketRepository.save(ticket);
    }

    private Comment createComment(Ticket ticket, User user, String text) {
        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setUser(user);
        comment.setComment(text);
        return comment;
    }
}
