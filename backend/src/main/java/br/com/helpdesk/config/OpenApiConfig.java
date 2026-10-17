package br.com.helpdesk.config;

import br.com.helpdesk.exceptions.ApiErrorResponse;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String BEARER_SCHEME = "bearerAuth";
    private static final String ERROR_SCHEMA = "ApiErrorResponse";

    @Bean
    public OpenAPI helpdeskOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Helpdesk API")
                        .version("v1")
                        .description("""
                                API do sistema de chamados.

                                Para chamar endpoints protegidos: faca login em `POST /api/auth/login`, \
                                copie o campo `token` da resposta e informe-o em **Authorize**.

                                Codigos usados nos campos numericos:
                                - **status**: 0=Aberto, 1=Em andamento, 2=Resolvido, 3=Fechado
                                - **priority**: 0=Baixa, 1=Media, 2=Alta, 3=Urgente
                                - **role**: 0=Administrador, 1=Supervisor, 2=Tecnico, 3=Solicitante
                                """))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }

    /**
     * Todo erro da API sai no formato {@link ApiErrorResponse}. Em vez de repetir o schema em cada
     * {@code @ApiResponse}, este customizer aplica o corpo de erro a toda resposta 4xx/5xx e adiciona
     * as respostas comuns (401/403 nas rotas protegidas e 500 em todas).
     */
    @Bean
    public OpenApiCustomizer standardErrorResponses() {
        return openApi -> {
            Components components = openApi.getComponents();
            if (components.getSchemas() == null || !components.getSchemas().containsKey(ERROR_SCHEMA)) {
                components.addSchemas(ERROR_SCHEMA, ModelConverters.getInstance(openApi.getSpecVersion() == SpecVersion.V31)
                        .resolveAsResolvedSchema(new AnnotatedType(ApiErrorResponse.class)).schema);
            }
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().values().forEach(path -> path.readOperations().forEach(this::applyErrorResponses));
        };
    }

    private void applyErrorResponses(Operation operation) {
        ApiResponses responses = operation.getResponses();
        boolean isPublic = operation.getSecurity() != null && operation.getSecurity().isEmpty();
        if (!isPublic) {
            responses.putIfAbsent("401", new ApiResponse().description("Token ausente, invalido ou expirado"));
            responses.putIfAbsent("403", new ApiResponse().description("Perfil sem permissao para esta operacao"));
        }
        responses.putIfAbsent("500", new ApiResponse().description("Erro inesperado no servidor"));
        responses.forEach((code, response) -> {
            if (code.startsWith("4") || code.startsWith("5")) {
                response.setContent(errorContent());
            }
        });
    }

    private Content errorContent() {
        return new Content().addMediaType(
                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + ERROR_SCHEMA)));
    }
}
