# Agenda Facil API

[Portugues](#portugues) | [English](#english)

## Portugues

API REST para agendamento de servicos, criada como projeto de portfolio Java com Spring Boot.

O projeto simula um fluxo real de agenda: um administrador cadastra servicos e profissionais, define disponibilidade, e clientes autenticados conseguem criar agendamentos sem conflito de horario.

### Destaques Tecnicos

- Autenticacao JWT com Spring Security
- Controle de acesso por perfil: `CLIENT`, `PROFESSIONAL` e `ADMIN`
- Regra de negocio para evitar conflito de horarios
- Versionamento de banco com Flyway
- Documentacao interativa com Swagger/OpenAPI
- PostgreSQL como banco principal
- Teste unitario para regra de conflito de agendamento
- Arquitetura em camadas: controller, service, repository, domain e dto

### Funcionalidades

- Cadastro e login com JWT
- Cadastro de servicos
- Cadastro de profissionais
- Cadastro de disponibilidade por profissional
- Criacao, listagem e cancelamento de agendamentos
- Bloqueio de dois agendamentos no mesmo horario com o mesmo profissional
- Swagger/OpenAPI com autenticacao Bearer JWT

### Stack

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- JWT
- JUnit 5
- Mockito
- Swagger/OpenAPI

### Como Rodar

Crie o banco no PostgreSQL:

```sql
CREATE DATABASE agenda_facil;
CREATE USER agenda WITH PASSWORD 'agenda';
GRANT ALL PRIVILEGES ON DATABASE agenda_facil TO agenda;
\c agenda_facil
GRANT ALL ON SCHEMA public TO agenda;
```

Rode a aplicacao:

```bash
mvn spring-boot:run
```

Se a porta `8080` estiver ocupada:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

Acesse:

- API: `http://localhost:8081`
- Swagger: `http://localhost:8081/swagger-ui.html`

### Variaveis Principais

Os valores padrao estao em `src/main/resources/application.yml`.

```text
DB_URL=jdbc:postgresql://localhost:5432/agenda_facil
DB_USER=agenda
DB_PASSWORD=agenda
JWT_SECRET=change-this-secret-change-this-secret-change-this-secret
JWT_EXPIRATION_MINUTES=120
```

### Testes

```bash
mvn test
```

### Fluxo Para Testar No Swagger

#### 1. Criar Admin

`POST /auth/register`

```json
{
  "name": "Admin",
  "email": "admin@email.com",
  "password": "123456",
  "role": "ADMIN"
}
```

Copie o token retornado e clique em `Authorize` no Swagger. Informe o token como Bearer JWT.

#### 2. Criar Servico

`POST /services`

```json
{
  "name": "Consulta inicial",
  "description": "Atendimento de avaliacao",
  "durationMinutes": 30,
  "price": 120
}
```

Guarde o `id` retornado. Ele sera usado como `serviceId`.

#### 3. Criar Usuario Profissional

`POST /auth/register`

```json
{
  "name": "Dr. Silva",
  "email": "profissional@email.com",
  "password": "123456",
  "role": "PROFESSIONAL"
}
```

Guarde o `userId` retornado.

#### 4. Cadastrar Profissional

Autenticado como admin, use `POST /professionals`.

```json
{
  "userId": 2,
  "specialty": "Clinico geral"
}
```

Troque `2` pelo `userId` real. Guarde o `id` retornado. Ele sera usado como `professionalId`.

#### 5. Criar Disponibilidade

`POST /professionals/availability`

```json
{
  "professionalId": 1,
  "dayOfWeek": "MONDAY",
  "startsAt": "09:00:00",
  "endsAt": "18:00:00"
}
```

#### 6. Criar Cliente

`POST /auth/register`

```json
{
  "name": "Cliente Teste",
  "email": "cliente@email.com",
  "password": "123456",
  "role": "CLIENT"
}
```

Copie o token do cliente e atualize o `Authorize` do Swagger com ele.

#### 7. Criar Agendamento

`POST /appointments`

Use uma data futura que caia no mesmo dia da disponibilidade. Exemplo de segunda-feira:

```json
{
  "professionalId": 1,
  "serviceId": 1,
  "startsAt": "2026-07-13T10:00:00"
}
```

Se repetir o mesmo horario para o mesmo profissional, a API deve bloquear com:

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "Profissional ja possui agendamento neste horario"
}
```

### Principais Endpoints

| Metodo | Endpoint | Descricao |
| --- | --- | --- |
| `POST` | `/auth/register` | Cria usuario e retorna JWT |
| `POST` | `/auth/login` | Autentica usuario |
| `GET` | `/services` | Lista servicos |
| `POST` | `/services` | Cria servico, apenas admin |
| `GET` | `/professionals` | Lista profissionais |
| `POST` | `/professionals` | Cria profissional, apenas admin |
| `POST` | `/professionals/availability` | Cria disponibilidade |
| `GET` | `/professionals/{id}/availability` | Lista disponibilidade |
| `POST` | `/appointments` | Cria agendamento, apenas cliente |
| `GET` | `/appointments/me` | Lista meus agendamentos |
| `PATCH` | `/appointments/{id}/cancel` | Cancela agendamento |

### Regras De Negocio

- Apenas usuarios `CLIENT` podem criar agendamentos.
- Um profissional nao pode ter dois agendamentos conflitantes.
- Agendamentos so podem ser criados para datas futuras.
- O horario solicitado precisa estar dentro da disponibilidade do profissional.
- Cancelamento altera o status para `CANCELLED`; o registro nao e apagado.
- Clientes visualizam seus proprios agendamentos.
- Profissionais visualizam sua agenda.
- Admin pode cancelar qualquer agendamento.

### Melhorias Futuras

- Envio de e-mail de confirmacao
- Reagendamento
- Filtros por periodo
- Testes de integracao com Testcontainers
- Frontend em React ou Angular

## English

REST API for appointment scheduling, built as a Java portfolio project with Spring Boot.

The project simulates a real scheduling workflow: an administrator creates services and professionals, defines availability, and authenticated clients can book appointments without time conflicts.

### Technical Highlights

- JWT authentication with Spring Security
- Role-based access control: `CLIENT`, `PROFESSIONAL` and `ADMIN`
- Business rule to prevent appointment time conflicts
- Database versioning with Flyway
- Interactive documentation with Swagger/OpenAPI
- PostgreSQL as the main database
- Unit test for appointment conflict validation
- Layered architecture: controller, service, repository, domain and dto

### Features

- User registration and login with JWT
- Service management
- Professional management
- Professional availability management
- Appointment creation, listing and cancellation
- Prevention of overlapping appointments for the same professional
- Swagger/OpenAPI with Bearer JWT authentication

### Stack

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- JWT
- JUnit 5
- Mockito
- Swagger/OpenAPI

### How To Run

Create the PostgreSQL database:

```sql
CREATE DATABASE agenda_facil;
CREATE USER agenda WITH PASSWORD 'agenda';
GRANT ALL PRIVILEGES ON DATABASE agenda_facil TO agenda;
\c agenda_facil
GRANT ALL ON SCHEMA public TO agenda;
```

Run the application:

```bash
mvn spring-boot:run
```

If port `8080` is already in use:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

Open:

- API: `http://localhost:8081`
- Swagger: `http://localhost:8081/swagger-ui.html`

### Main Environment Variables

Default values are defined in `src/main/resources/application.yml`.

```text
DB_URL=jdbc:postgresql://localhost:5432/agenda_facil
DB_USER=agenda
DB_PASSWORD=agenda
JWT_SECRET=change-this-secret-change-this-secret-change-this-secret
JWT_EXPIRATION_MINUTES=120
```

### Tests

```bash
mvn test
```

### Swagger Test Flow

#### 1. Create Admin

`POST /auth/register`

```json
{
  "name": "Admin",
  "email": "admin@email.com",
  "password": "123456",
  "role": "ADMIN"
}
```

Copy the returned token and click `Authorize` in Swagger. Use it as a Bearer JWT token.

#### 2. Create Service

`POST /services`

```json
{
  "name": "Initial consultation",
  "description": "Evaluation appointment",
  "durationMinutes": 30,
  "price": 120
}
```

Save the returned `id`. It will be used as `serviceId`.

#### 3. Create Professional User

`POST /auth/register`

```json
{
  "name": "Dr. Silva",
  "email": "professional@email.com",
  "password": "123456",
  "role": "PROFESSIONAL"
}
```

Save the returned `userId`.

#### 4. Register Professional

Authenticated as admin, use `POST /professionals`.

```json
{
  "userId": 2,
  "specialty": "General practitioner"
}
```

Replace `2` with the actual `userId`. Save the returned `id`. It will be used as `professionalId`.

#### 5. Create Availability

`POST /professionals/availability`

```json
{
  "professionalId": 1,
  "dayOfWeek": "MONDAY",
  "startsAt": "09:00:00",
  "endsAt": "18:00:00"
}
```

#### 6. Create Client

`POST /auth/register`

```json
{
  "name": "Test Client",
  "email": "client@email.com",
  "password": "123456",
  "role": "CLIENT"
}
```

Copy the client token and update Swagger `Authorize` with it.

#### 7. Create Appointment

`POST /appointments`

Use a future date that matches the professional availability day. Monday example:

```json
{
  "professionalId": 1,
  "serviceId": 1,
  "startsAt": "2026-07-13T10:00:00"
}
```

If the same time is used again for the same professional, the API should reject it with:

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "Profissional ja possui agendamento neste horario"
}
```

### Main Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/auth/register` | Creates user and returns JWT |
| `POST` | `/auth/login` | Authenticates user |
| `GET` | `/services` | Lists services |
| `POST` | `/services` | Creates service, admin only |
| `GET` | `/professionals` | Lists professionals |
| `POST` | `/professionals` | Creates professional, admin only |
| `POST` | `/professionals/availability` | Creates availability |
| `GET` | `/professionals/{id}/availability` | Lists availability |
| `POST` | `/appointments` | Creates appointment, client only |
| `GET` | `/appointments/me` | Lists my appointments |
| `PATCH` | `/appointments/{id}/cancel` | Cancels appointment |

### Business Rules

- Only `CLIENT` users can create appointments.
- A professional cannot have overlapping scheduled appointments.
- Appointments can only be created for future dates.
- The requested time must be inside the professional availability window.
- Cancellation changes the status to `CANCELLED`; the record is not deleted.
- Clients can view their own appointments.
- Professionals can view their schedule.
- Admin users can cancel any appointment.

### Future Improvements

- Confirmation email
- Rescheduling
- Period filters
- Integration tests with Testcontainers
- Frontend with React or Angular
