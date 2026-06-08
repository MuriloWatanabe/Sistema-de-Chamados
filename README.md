# Sistema de Chamados (Helpdesk)

Sistema de gerenciamento de chamados de suporte com controle de acesso baseado em perfis, desenvolvido com Spring Boot no backend e Angular no frontend.

---

## Domínio do Problema

Empresas frequentemente enfrentam dificuldades no gerenciamento de chamados técnicos internos, como problemas de rede, computadores, sistemas e acessos.

Quando não há um sistema estruturado, podem ocorrer:

- Perda de solicitações
- Falta de priorização adequada
- Atraso no atendimento
- Ausência de histórico de ocorrências

O sistema proposto centraliza e organiza os chamados técnicos internos, proporcionando controle, rastreabilidade e melhor gestão do suporte, incluindo acompanhamento completo do ciclo de vida do chamado.

---

## Tecnologias

### Backend
| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.14 |
| Spring Security | - |
| Spring Data JPA | - |
| JWT | - |
| PostgreSQL Driver | - |
| Maven | - |

### Frontend
| Tecnologia | Versão |
|---|---|
| Angular | 19.2 |
| Angular Material | - |
| RxJS | 7.8 |
| JWT Interceptor | - |
| TypeScript | 5.7 |

### Banco de Dados
- PostgreSQL 16+

---

## Arquitetura

O projeto segue arquitetura em camadas:

```
Controller → Service → Repository → Database
```

Exemplo de fluxo:

```
TicketController
      ↓
TicketService
      ↓
TicketRepository
      ↓
   PostgreSQL
```

---

## Estrutura do Projeto

```
Sistema-de-Chamados/
├── backend/
│   └── src/main/java/
│       ├── br/com/sistemadechamados/
│       │   └── SistemaDeChamadosApplication.java
│       ├── config/
│       ├── security/
│       ├── controllers/
│       ├── services/
│       ├── repositories/
│       ├── entities/
│       ├── dtos/
│       ├── exceptions/
│       ├── auditing/
│       └── utils/
│
└── frontend/
    └── src/app/
        ├── core/
        │   ├── auth/
        │   ├── interceptors/
        │   └── guards/
        ├── shared/
        │   └── components/
        ├── modules/
        │   ├── dashboard/
        │   ├── users/
        │   ├── tickets/
        │   ├── comments/
        │   └── audit/
        └── layouts/
```

---

## Modelo de Dados

### Usuário (`users`)
| Campo | Tipo |
|---|---|
| id | BIGSERIAL |
| name | VARCHAR(150) |
| email | VARCHAR(150) |
| password | VARCHAR(255) |
| created_at | TIMESTAMP |
| updated_at | TIMESTAMP |
| active | BOOLEAN |
| role | INTEGER |

### Chamado (`tickets`)
| Campo | Tipo |
|---|---|
| id | BIGSERIAL |
| title | VARCHAR(200) |
| description | TEXT |
| status | INTEGER |
| priority | INTEGER |
| requester_id | BIGSERIAL (FK users) |
| assigned_to_id | BIGSERIAL (FK users) |
| created_at | TIMESTAMP |
| updated_at | TIMESTAMP |
| closed_at | TIMESTAMP |

### Comentários (`comments`)
| Campo | Tipo |
|---|---|
| id | BIGSERIAL |
| ticket_id | BIGSERIAL (FK tickets) |
| user_id | BIGSERIAL (FK users) |
| comment | TEXT |
| created_at | TIMESTAMP |

### Auditoria (`audit_logs`)
| Campo | Tipo |
|---|---|
| id | BIGSERIAL |
| user_id | BIGSERIAL (FK users) |
| action | VARCHAR(100) |
| entity_type | INTEGER |
| entity_id | BIGSERIAL |
| old_value | JSONB |
| new_value | JSONB |
| created_at | TIMESTAMP |

---

## Perfis de Acesso

| Perfil | Descrição |
|---|---|
| `ROLE_REQUESTER` | Usuário comum que abre e acompanha seus próprios chamados |
| `ROLE_TECHNICIAN` | Técnico responsável por atender e resolver chamados |
| `ROLE_SUPERVISOR` | Supervisor responsável pelo gerenciamento da equipe e monitoramento |
| `ROLE_ADMIN` | Administrador com acesso completo ao sistema |

---

## Matriz de Permissões

| Funcionalidade | Solicitante | Técnico | Supervisor | Administrador |
|---|:---:|:---:|:---:|:---:|
| Criar usuário | | | | X |
| Editar usuário | | | X | X |
| Excluir usuário | | | | X |
| Consultar usuários | | X | X | X |
| Abrir chamado | X | X | X | X |
| Consultar próprios chamados | X | X | X | X |
| Consultar todos os chamados | | X | X | X |
| Alterar status do chamado | | X | X | X |
| Alterar prioridade | | X | X | X |
| Atribuir responsável | | X | X | X |
| Encerrar chamado | | X | X | X |
| Reabrir chamado | | X | X | X |
| Adicionar comentário | X | X | X | X |
| Consultar logs de auditoria | | | X | X |
| Alterar permissões de usuários | | | | X |

---

## Segurança

- Autenticação via **JWT Bearer Token**
  ```
  Authorization: Bearer eyJhb...
  ```
- Senhas armazenadas com **BCryptPasswordEncoder**
- Controle de acesso com **Spring Security** e anotações `@PreAuthorize`
  ```java
  @PreAuthorize("hasRole('ADMIN')")
  ```

---

## Pré-requisitos

- Java 21+
- Maven 3.9+
- Node.js 20+
- Angular CLI 19+
- PostgreSQL 16+

---

## Como Executar

### Backend

```bash
cd backend
mvn spring-boot:run
```

O servidor sobe em `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm start
```

A aplicação sobe em `http://localhost:4200`.

---

## Configuração do Banco de Dados

Crie o banco de dados no PostgreSQL e configure as credenciais em `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sistema_chamados
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update
```