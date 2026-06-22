# Backend

Backend do helpdesk em Spring Boot.

## Como subir com Docker

Pre-requisitos:
- `Docker`
- `Docker Compose`

Entre na pasta `backend` e execute:

```powershell
docker compose up --build
```

Isso vai subir:
- `postgres:16` em `localhost:5432`
- backend em `localhost:8080`

## O que esta configurado

- `backend/Dockerfile` compila o projeto com Maven dentro do container.
- `backend/docker-compose.yml` sobe o PostgreSQL e o backend na mesma rede.
- O backend usa as variaveis de ambiente passadas pelo `compose`.
- As tabelas sao criadas/atualizadas automaticamente pelo Hibernate na inicializacao.

## Seguranca

- O login gera um JWT assinado com HMAC SHA-256.
- O token expira por padrao em 120 minutos.
- O filtro de autenticacao valida assinatura, expiracao, usuario ativo e papel antes de liberar a request.
- As senhas sao salvas como hash bcrypt no campo `PASSWORD` da tabela `USERS`.
- O backend nunca grava senha em texto puro, nem no login nem no seed inicial.

## Acesso inicial

O backend sobe com dados iniciais para demonstracao:

- `admin@helpdesk.com` / `admin123`
- `supervisor@helpdesk.com` / `super123`
- `tecnico@helpdesk.com` / `tecnico123`
- `solicitante@helpdesk.com` / `solicitante123`

Enderecos principais:

- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/tickets`
- `GET /api/tickets/{id}`
- `POST /api/tickets`
- `DELETE /api/tickets/{id}`
- `PATCH /api/tickets/{id}`
- `GET /api/tickets/{id}/comments`
- `POST /api/tickets/{id}/comments`
- `GET /api/users`
- `GET /api/dashboard/stats`
- `GET /api/audits`

## Banco de dados

Configuracao usada no ambiente Docker:
- banco: `helpdesk`
- usuario: `postgres`
- senha: `postgres`

## Parar o ambiente

```powershell
docker compose down
```

Para remover tambem os dados do PostgreSQL:

```powershell
docker compose down -v
```

## Observacao

Voce nao precisa ter `Maven` instalado localmente para subir com Docker.
