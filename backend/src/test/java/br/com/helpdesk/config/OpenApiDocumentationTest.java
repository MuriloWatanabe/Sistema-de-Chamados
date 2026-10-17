package br.com.helpdesk.config;

import br.com.helpdesk.repositories.AuditRepository;
import br.com.helpdesk.repositories.CommentRepository;
import br.com.helpdesk.repositories.TicketRepository;
import br.com.helpdesk.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sobe o contexto web completo (seguranca + springdoc) sem banco: o JPA e desligado e os
 * repositorios sao mockados. Garante que a documentacao continua publica e que o contrato de
 * erros e autenticacao descrito no Swagger nao se perde em refatoracoes.
 */
@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration")
@AutoConfigureMockMvc
class OpenApiDocumentationTest {
    private static final String ERROR_REF = "#/components/schemas/ApiErrorResponse";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TicketRepository ticketRepository;

    @MockitoBean
    private CommentRepository commentRepository;

    @MockitoBean
    private AuditRepository auditRepository;

    @MockitoBean(name = "seedDatabase")
    private CommandLineRunner seedDatabase;

    @Test
    void apiDocs_isPublicAndDeclaresJwtBearerScheme() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Helpdesk API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.security[0].bearerAuth").exists());
    }

    @Test
    void swaggerUi_isPublicAndItsCspAllowsOwnScripts() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("script-src 'self'")));
    }

    @Test
    void apiResponses_keepStrictCsp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", startsWith("default-src 'none'")))
                .andExpect(header().string("Content-Security-Policy", not(containsString("script-src"))));
    }

    @Test
    void apiDocs_rejectWriteMethods() throws Exception {
        mockMvc.perform(post("/v3/api-docs"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void authEndpoints_documentLockoutLogoutAndPasswordChange() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.responses['429'].content['application/json'].schema.$ref").value(ERROR_REF))
                .andExpect(jsonPath("$.paths['/api/auth/logout'].post.responses['204']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/password'].put.responses['400']").exists())
                .andExpect(jsonPath("$.components.schemas.ChangePasswordRequest.properties.newPassword.pattern").exists());
    }

    @Test
    void protectedOperation_documentsAuthAndErrorResponsesWithApiErrorSchema() throws Exception {
        String delete = "$.paths['/api/tickets/{id}'].delete";
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath(delete + ".responses['204']").exists())
                .andExpect(jsonPath(delete + ".responses['401'].content['application/json'].schema.$ref").value(ERROR_REF))
                .andExpect(jsonPath(delete + ".responses['403'].content['application/json'].schema.$ref").value(ERROR_REF))
                .andExpect(jsonPath(delete + ".responses['404'].content['application/json'].schema.$ref").value(ERROR_REF))
                .andExpect(jsonPath(delete + ".responses['500'].content['application/json'].schema.$ref").value(ERROR_REF))
                .andExpect(jsonPath("$.components.schemas.ApiErrorResponse.type").value("object"));
    }

    @Test
    void createTicket_documents201AndValidationError() throws Exception {
        String post = "$.paths['/api/tickets'].post";
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath(post + ".responses['201'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/TicketResponse"))
                .andExpect(jsonPath(post + ".responses['400'].content['application/json'].schema.$ref").value(ERROR_REF))
                .andExpect(jsonPath(post + ".responses", not(hasKey("200"))));
    }

    @Test
    void login_isDocumentedAsPublic() throws Exception {
        String login = "$.paths['/api/auth/login'].post";
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath(login + ".security", empty()))
                .andExpect(jsonPath(login + ".responses['401'].description").value("E-mail ou senha incorretos, ou usuario inativo"))
                .andExpect(jsonPath(login + ".responses", not(hasKey("403"))));
    }

    @Test
    void requestSchemas_exposeValidationRulesAndCodeMeaning() throws Exception {
        String priority = "$.components.schemas.TicketCreateRequest.properties.priority";
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath(priority + ".minimum").value(0))
                .andExpect(jsonPath(priority + ".maximum").value(3))
                .andExpect(jsonPath(priority + ".description").value("Prioridade: 0=Baixa, 1=Media, 2=Alta, 3=Urgente"));
    }

    @Test
    void authenticationParameter_isNotExposedAsRequestParameter() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/api/tickets'].get.parameters").doesNotExist());
    }
}
