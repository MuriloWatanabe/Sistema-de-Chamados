# Sistema de Chamados (Helpdesk)

Sistema de chamados com backend Spring Boot + PostgreSQL e frontend Angular 19.

Este README descreve o que existe hoje no projeto, como o fluxo funciona na pratica e como subir o sistema localmente.

---

## Estado atual do projeto

- O backend esta real e persistente.
- O frontend esta hibrido: o login tenta o backend, mas as telas de dashboard, chamados e usuarios ainda usam dados em memoria via `MockDataService`.
- O token JWT e o usuario logado sao salvos no `localStorage` como `hd_user` e `hd_token`.
- O token ainda nao e anexado em outras requisicoes porque as telas fora do login ainda nao consomem a API real.

---

## Fluxo atual

1. A aplicacao abre em `/login`.
2. `AuthService` tenta autenticar em `POST /api/auth/login`.
3. Se o backend responder, o usuario e o token sao salvos e a navegacao vai para `/dashboard`.
4. Se o backend nao responder, o frontend cai no modo demo com credenciais locais.
5. `authGuard` so verifica se existe usuario logado.
6. `MainLayout` monta o menu conforme o papel do usuario.
7. As telas de dashboard, chamados e usuarios leem e alteram dados em memoria no frontend.
8. O backend, quando usado, persiste em PostgreSQL e grava auditoria.

Fluxo resumido da requisicao:

```text
Browser -> Angular login -> POST /api/auth/login -> JWT -> localStorage
Browser -> Angular telas -> MockDataService -> arrays em memoria
```

Fluxo real do backend:

```text
Request -> TokenAuthenticationFilter -> SecurityContext -> Controller -> Service -> Repository -> PostgreSQL
```

---

## Backend

### Stack

- Java 21
- Spring Boot 3.5.14
- Spring Security
- Spring Data JPA
- PostgreSQL 16+
- Maven 3.9+

### Estrutura de pacotes

- `config`
- `controllers`
- `dtos`
- `entities`
- `enums`
- `exceptions`
- `repositories`
- `security`
- `services`
- `utils`

### Componentes centrais

- `HelpdeskApplication`: ponto de entrada do Spring Boot.
- `SecurityConfig`: configura JWT, CORS, sessao stateless e regras de acesso.
- `TokenAuthenticationFilter`: valida o Bearer token em cada request autenticada.
- `CurrentUserService`: resolve o usuario autenticado a partir do `Authentication`.
- `DataInitializer`: cria dados iniciais quando o banco esta vazio.
- `RestExceptionHandler`: padroniza os erros da API.
- `DtoMapper`: converte entidades para respostas da API.

### Regras de autenticacao e seguranca

- A senha e armazenada com `BCryptPasswordEncoder`.
- O login so aceita usuario ativo.
- O JWT e assinado com HMAC SHA-256.
- O backend libera apenas:
  - `POST /api/auth/login`
  - `GET /api/health`
- O restante da API exige autenticacao.
- O acesso por papel usa `@PreAuthorize`.

### Papais de acesso

| Codigo | Papel | Uso principal |
|---|---|---|
| 0 | ADMIN | acesso total |
| 1 | SUPERVISOR | gestao de equipe e auditoria |
| 2 | TECHNICIAN | atendimento e manutencao de chamados |
| 3 | REQUESTER | abertura e acompanhamento de chamados proprios |

### Status de chamados

| Codigo | Status |
|---|---|
| 0 | OPEN |
| 1 | IN_PROGRESS |
| 2 | RESOLVED |
| 3 | CLOSED |

### Prioridade de chamados

| Codigo | Prioridade |
|---|---|
| 0 | LOW |
| 1 | MEDIUM |
| 2 | HIGH |
| 3 | URGENT |

### Entidades do banco

#### `USERS`

| Campo | Tipo |
|---|---|
| id | BIGSERIAL |
| name | VARCHAR(150) |
| email | VARCHAR(150) |
| password | VARCHAR(255) |
| active | BOOLEAN |
| role | INTEGER |
| created_at | TIMESTAMP |
| updated_at | TIMESTAMP |

#### `TICKETS`

| Campo | Tipo |
|---|---|
| id | BIGSERIAL |
| title | VARCHAR(200) |
| description | TEXT |
| status | INTEGER |
| priority | INTEGER |
| requester_id | FK users |
| assigned_to_id | FK users |
| created_at | TIMESTAMP |
| updated_at | TIMESTAMP |
| closed_at | TIMESTAMP |

#### `COMMENTS`

| Campo | Tipo |
|---|---|
| id | BIGSERIAL |
| ticket_id | FK tickets |
| user_id | FK users |
| comment_text | TEXT |
| created_at | TIMESTAMP |

#### `AUDITS`

| Campo | Tipo |
|---|---|
| id | BIGSERIAL |
| user_id | FK users |
| action | VARCHAR(100) |
| entity_type | INTEGER |
| entity_id | BIGSERIAL |
| old_value | JSONB |
| new_value | JSONB |
| created_at | TIMESTAMP |

### Regras de negocio do backend

- Solicitante ve apenas os proprios chamados.
- Tecnico, supervisor e admin veem todos os chamados.
- Um chamado pode ser aberto em nome de outro usuario somente por tecnico ou acima.
- O responsavel de um chamado precisa ter papel `TECHNICIAN`.
- Quando o status vira `CLOSED`, o campo `closedAt` e preenchido.
- Quando o responsavel e removido, o status volta para `OPEN` se o chamado nao estiver fechado.
- Um usuario nao pode ser excluido se tiver relacoes em tickets, comentarios ou auditoria.
- O login grava uma entrada de auditoria do tipo `AUTH`.

### Endpoints da API

| Metodo | Endpoint | Acesso | Finalidade |
|---|---|---|---|
| POST | `/api/auth/login` | publico | autenticar e gerar JWT |
| GET | `/api/auth/me` | autenticado | retornar usuario atual |
| GET | `/api/health` | publico | healthcheck |
| GET | `/api/tickets` | autenticado | listar chamados visiveis |
| GET | `/api/tickets/{id}` | autenticado | detalhar chamado |
| POST | `/api/tickets` | autenticado | criar chamado |
| PATCH | `/api/tickets/{id}` | technician+ | atualizar status, prioridade ou responsavel |
| GET | `/api/tickets/{id}/comments` | autenticado | listar comentarios |
| POST | `/api/tickets/{id}/comments` | autenticado | adicionar comentario |
| GET | `/api/users` | technician+ | listar usuarios |
| GET | `/api/users/technicians` | technician+ | listar tecnicos ativos |
| GET | `/api/users/{id}` | technician+ | detalhar usuario |
| POST | `/api/users` | admin | criar usuario |
| PUT | `/api/users/{id}` | supervisor+ | atualizar usuario |
| DELETE | `/api/users/{id}` | admin | excluir usuario |
| GET | `/api/dashboard/stats` | autenticado | indicadores do dashboard |
| GET | `/api/audits` | supervisor+ | listar auditoria |

### Dados iniciais

Quando o banco esta vazio, o backend cria estes usuarios e exemplos:

- `admin@helpdesk.com` / `admin123`
- `supervisor@helpdesk.com` / `super123`
- `tecnico@helpdesk.com` / `tecnico123`
- `solicitante@helpdesk.com` / `solicitante123`
- `maria@helpdesk.com` / `maria123`

Tambem sao criados chamados e comentarios de exemplo para demonstracao.

### Configuracao do backend

Arquivo principal:

- `backend/src/main/resources/application.yml`

Variaveis aceitas:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_CORS_ALLOWED_ORIGINS`
- `APP_SECURITY_JWT_SECRET`
- `APP_SECURITY_JWT_EXPIRATION_MINUTES`
- `SERVER_PORT`

Defaults atuais:

- banco: `jdbc:postgresql://localhost:5432/helpdesk`
- usuario: `postgres`
- senha: `postgres`
- porta: `8080`
- CORS: `http://localhost:4200,http://127.0.0.1:4200`

---

## Frontend

### Stack

- Angular 19.2
- TypeScript 5.7
- RxJS 7.8

### Estrutura principal

- `core`
  - `guards`
  - `models`
  - `services`
- `layouts`
- `modules`
  - `auth`
  - `dashboard`
  - `tickets`
  - `users`

### Rotas

| Rota | Tela |
|---|---|
| `/login` | login |
| `/dashboard` | painel inicial |
| `/tickets` | lista de chamados |
| `/tickets/novo` | abertura de chamado |
| `/tickets/:id` | detalhe do chamado |
| `/usuarios` | lista de usuarios |

### Componentes e servicos centrais

- `AuthService`
  - tenta login real no backend
  - faz fallback para credenciais locais se a API falhar
  - guarda o usuario no `localStorage`
- `authGuard`
  - libera rotas apenas se existir usuario logado
- `MainLayoutComponent`
  - monta sidebar e cabecalho
  - esconde ou mostra opcoes por papel
- `MockDataService`
  - guarda usuarios, chamados e comentarios em memoria
  - cria, edita e exclui dados sem persistir no banco

### O que esta real e o que e mock no frontend

Real:

- login em `POST http://localhost:8080/api/auth/login`
- recuperacao do usuario autenticado no cache local

Mock:

- dashboard
- lista de chamados
- detalhe de chamado
- abertura de chamado
- listagem e edicao de usuarios

### Mapas de dominio no frontend

- `ROLES` espelha os codigos de papel do backend.
- `STATUS` espelha os codigos de status do chamado.
- `PRIORITY` espelha os codigos de prioridade.

---

## Como subir o projeto

### Dependencias

Modo recomendado:

- Docker Desktop
- Docker Compose
- Node.js 20+
- npm

Modo local para o backend:

- Java 21
- Maven 3.9+
- PostgreSQL 16+

Observacao:

- O Angular CLI nao precisa estar instalado globalmente.
- O frontend usa a URL fixa `http://localhost:8080/api`.

### Passo a passo recomendado

1. Suba o backend:

```powershell
cd backend
docker compose up -d --build
```

2. Suba o frontend:

```powershell
cd frontend
npm install
npm start
```

3. Abra a aplicacao:

- `http://localhost:4200`

4. Verifique os servicos:

- API: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

### Rodando o backend sem Docker

Se quiser rodar sem Docker, ajuste o banco no `application.yml` ou defina as variaveis abaixo:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/helpdesk"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
$env:SERVER_PORT="8080"
```

Depois execute:

```powershell
cd backend
mvn spring-boot:run
```

### Parar os servicos

Backend com Docker:

```powershell
cd backend
docker compose down
```

Frontend:

- pare o `npm start` com `Ctrl+C`

---

## Referencias do repositorio

- `backend/README.md`: instrucoes especificas do backend com Docker.
- `frontend/README.md`: README gerado pelo Angular.
- `docs/Matriz de Permissoes.pdf`: matriz de permissao do projeto.
- `docs/Planejamento Tecnico e Modelo de Dados.pdf`: planejamento e modelagem.

