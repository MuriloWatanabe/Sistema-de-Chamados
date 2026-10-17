package br.com.helpdesk.controllers;

import br.com.helpdesk.dtos.CommentCreateRequest;
import br.com.helpdesk.dtos.CommentResponse;
import br.com.helpdesk.dtos.TicketCreateRequest;
import br.com.helpdesk.dtos.TicketResponse;
import br.com.helpdesk.dtos.TicketUpdateRequest;
import br.com.helpdesk.services.TicketService;
import br.com.helpdesk.security.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Abertura, acompanhamento e atendimento de chamados")
public class TicketController {
    private final TicketService ticketService;
    private final CurrentUserService currentUserService;

    public TicketController(TicketService ticketService, CurrentUserService currentUserService) {
        this.ticketService = ticketService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar chamados visiveis",
            description = "Tecnicos, supervisores e administradores veem todos os chamados; "
                    + "solicitantes veem apenas os que abriram. Ordenado do mais recente para o mais antigo.")
    @ApiResponse(responseCode = "200", description = "Lista de chamados")
    public List<TicketResponse> listTickets(Authentication authentication) {
        return ticketService.listVisibleTickets(currentUserService.requireUser(authentication));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Buscar chamado por ID")
    @ApiResponse(responseCode = "200", description = "Chamado encontrado")
    @ApiResponse(responseCode = "403", description = "Solicitante tentando acessar chamado de outro usuario")
    @ApiResponse(responseCode = "404", description = "Chamado inexistente")
    public TicketResponse getTicket(
            @Parameter(description = "ID do chamado", example = "1") @PathVariable Long id,
            Authentication authentication
    ) {
        return ticketService.getTicket(id, currentUserService.requireUser(authentication));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Abrir chamado",
            description = "Cria o chamado com status Aberto, ou Em andamento quando ja e atribuido a um tecnico. "
                    + "Apenas tecnicos ou perfis superiores podem abrir em nome de outro usuario (requesterId).")
    @ApiResponse(responseCode = "201", description = "Chamado criado")
    @ApiResponse(responseCode = "400", description = "Dados invalidos ou responsavel informado nao e tecnico")
    @ApiResponse(responseCode = "403", description = "Solicitante tentando abrir chamado em nome de outro usuario")
    @ApiResponse(responseCode = "404", description = "Solicitante ou responsavel inexistente")
    public TicketResponse createTicket(@Valid @RequestBody TicketCreateRequest request, Authentication authentication) {
        return ticketService.createTicket(currentUserService.requireUser(authentication), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir chamado",
            description = "Remove o chamado e seus comentarios. Somente administradores.")
    @ApiResponse(responseCode = "204", description = "Chamado excluido")
    @ApiResponse(responseCode = "404", description = "Chamado inexistente")
    public void deleteTicket(
            @Parameter(description = "ID do chamado", example = "1") @PathVariable Long id,
            Authentication authentication
    ) {
        ticketService.deleteTicket(id, currentUserService.requireUser(authentication));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'SUPERVISOR', 'ADMIN')")
    @Operation(summary = "Atualizar status, prioridade ou responsavel",
            description = "Apenas os campos informados sao alterados. Atribuir um tecnico a um chamado Aberto "
                    + "o move para Em andamento; mudar o status para Fechado registra a data de encerramento.")
    @ApiResponse(responseCode = "200", description = "Chamado atualizado")
    @ApiResponse(responseCode = "400", description = "Codigo invalido ou responsavel informado nao e tecnico")
    @ApiResponse(responseCode = "404", description = "Chamado ou responsavel inexistente")
    public TicketResponse updateTicket(
            @Parameter(description = "ID do chamado", example = "1") @PathVariable Long id,
            @Valid @RequestBody TicketUpdateRequest request,
            Authentication authentication
    ) {
        return ticketService.updateTicket(id, currentUserService.requireUser(authentication), request);
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar comentarios do chamado", description = "Ordenados do mais antigo para o mais recente.")
    @ApiResponse(responseCode = "200", description = "Lista de comentarios")
    @ApiResponse(responseCode = "403", description = "Solicitante tentando acessar chamado de outro usuario")
    @ApiResponse(responseCode = "404", description = "Chamado inexistente")
    public List<CommentResponse> listComments(
            @Parameter(description = "ID do chamado", example = "1") @PathVariable Long id,
            Authentication authentication
    ) {
        return ticketService.listComments(id, currentUserService.requireUser(authentication));
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Adicionar comentario ao chamado")
    @ApiResponse(responseCode = "201", description = "Comentario criado")
    @ApiResponse(responseCode = "400", description = "Comentario vazio")
    @ApiResponse(responseCode = "403", description = "Solicitante tentando comentar chamado de outro usuario")
    @ApiResponse(responseCode = "404", description = "Chamado inexistente")
    public CommentResponse addComment(
            @Parameter(description = "ID do chamado", example = "1") @PathVariable Long id,
            @Valid @RequestBody CommentCreateRequest request,
            Authentication authentication
    ) {
        return ticketService.addComment(id, currentUserService.requireUser(authentication), request);
    }
}
