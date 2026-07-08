# Roadmap

[Portugues](#portugues) | [English](#english)

## Portugues

Este roadmap lista melhorias planejadas para evoluir a Agenda Facil API como um projeto de portfolio Java/Spring Boot.

### Proximas Melhorias

#### 1. Testes de integracao com Testcontainers

Adicionar testes de integracao usando PostgreSQL real em ambiente isolado.

Objetivos:

- Subir PostgreSQL automaticamente durante os testes.
- Validar migrations do Flyway.
- Testar fluxos completos da API com banco real.

#### 2. Reagendamento de consultas

Criar endpoint para alterar data e horario de um agendamento existente.

Objetivos:

- Validar conflito de horario no novo periodo.
- Impedir reagendamento para datas passadas.
- Manter historico do agendamento.

#### 3. Notificacao por e-mail

Enviar e-mail de confirmacao quando um agendamento for criado ou cancelado.

Objetivos:

- Criar servico de notificacao.
- Usar templates simples de e-mail.
- Permitir configuracao via variaveis de ambiente.

#### 4. Filtros por periodo

Status: implementado.

Adicionar filtros de data para listagem de agendamentos.

Objetivos:

- Filtrar por data inicial e final.
- Permitir consultas por profissional.
- Melhorar uso em dashboards ou telas de agenda.

#### 5. Prints do Swagger no README

Adicionar imagens do Swagger para demonstrar endpoints, autorizacao JWT e fluxo de agendamento.

Objetivos:

- Melhorar apresentacao visual do projeto no GitHub.
- Facilitar entendimento rapido por recrutadores e revisores tecnicos.

#### 6. Frontend

Criar uma interface web simples para consumir a API.

Objetivos:

- Login e cadastro.
- Tela de servicos e profissionais.
- Fluxo de criacao de agendamento.
- Listagem de agendamentos do usuario.

## English

This roadmap lists planned improvements to evolve Agenda Facil API as a Java/Spring Boot portfolio project.

### Next Improvements

#### 1. Integration tests with Testcontainers

Add integration tests using a real PostgreSQL database in an isolated environment.

Goals:

- Start PostgreSQL automatically during tests.
- Validate Flyway migrations.
- Test complete API flows with a real database.

#### 2. Appointment rescheduling

Create an endpoint to update the date and time of an existing appointment.

Goals:

- Validate time conflicts for the new period.
- Prevent rescheduling to past dates.
- Preserve appointment history.

#### 3. Email notification

Send confirmation emails when an appointment is created or cancelled.

Goals:

- Create a notification service.
- Use simple email templates.
- Allow configuration through environment variables.

#### 4. Date range filters

Status: implemented.

Add date filters for appointment listing.

Goals:

- Filter by start and end date.
- Support professional schedule queries.
- Improve usage in dashboards or scheduling screens.

#### 5. Swagger screenshots in README

Add Swagger images to demonstrate endpoints, JWT authorization and the appointment flow.

Goals:

- Improve the project presentation on GitHub.
- Make the project easier to understand for recruiters and technical reviewers.

#### 6. Frontend

Create a simple web interface to consume the API.

Goals:

- Login and registration.
- Services and professionals screen.
- Appointment creation flow.
- User appointment listing.
