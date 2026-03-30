# Oficina

Monólito **Kotlin** com **Spring Boot** (JPA, Flyway, Spring Security OAuth2 Resource Server + JWT emitido pela aplicação), organizado em **arquitetura em camadas** alinhada ao Tech Challenge SOAT (MVP oficina mecânica).

## Stack

- Kotlin 2.3, Java 17, Gradle
- Spring Boot (web, data-jpa, validation, flyway, security, oauth2-resource-server, session-jdbc, actuator)
- OpenAPI / Swagger UI (springdoc)
- Banco local (padrão): H2 em memória + schema criado/atualizado pelo Hibernate (`ddl-auto=update`; Flyway **desligado** neste perfil)
- Banco em Docker: **PostgreSQL** + **Flyway** (`validate` + scripts em `oficina/src/main/resources/db/migration`)

## Linguagem ubíqua (resumo)

| Termo | Significado |
| ----- | ----------- |
| **Ordem de serviço (OS)** | Agregado com status: Recebida → Em diagnóstico → Aguardando aprovação → Em execução → Finalizada → Entregue. |
| **Documento (CPF/CNPJ)** | Identificação do cliente; validação de dígitos no domínio. |
| **Placa** | Identificação do veículo (padrão antigo ou Mercosul). |
| **Serviço (catálogo)** | Serviço cadastrado com preço e tempo estimado. |
| **Peça / insumo** | Item com estoque; baixa na **aprovação do orçamento** (início da execução). |
| **Código de acompanhamento** | UUID público da OS para consulta pelo cliente. |

Documentação DDD (Event Storming, diagramas) deve ser mantida no **Miro** (ou equivalente), conforme enunciado da disciplina.

## Estrutura de camadas

| Camada | Pacote | Papel |
| ------ | ------ | ----- |
| **Domain** | `...domain` | Modelo, value objects, exceções de domínio, portas. |
| **Application** | `...application` | Casos de uso, DTOs de API internos, orquestração. |
| **Infrastructure** | `...infrastructure` | JPA, Flyway, adapters, JWT, Jackson, OpenAPI. |
| **Presentation** | `...presentation` | Controllers REST (`presentation` porque `interface` é palavra reservada). |

## API REST (visão geral)

- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **Login (público):** `POST /api/public/auth/login` — corpo `{"username":"admin","password":"..."}` → `accessToken` JWT.
- **Administrativo (JWT):** prefixo `/api/admin/**` — header `Authorization: Bearer <token>`.
- **Cliente (público):** `GET /api/public/os/acompanhar?documento=<cpf/cnpj>&codigo=<uuid>` — documento com ou sem máscara.

Recursos admin: clientes, veículos, serviços de catálogo, peças, ordens de serviço (criação + ações de transição de status), métricas de tempo médio de execução por serviço (OS finalizadas ou entregues).

## Escolha do banco de dados

**PostgreSQL** em Docker: relacional, ACID, aderente a JPA, integridade entre clientes, veículos, itens de OS e estoque. Flyway versiona o schema em `db/migration/V1__init.sql`.

**H2** no perfil padrão local: desenvolvimento sem instalar Postgres; perfil `test` desliga Flyway e usa `ddl-auto=create-drop` para testes automatizados.

## Como executar

### Local (Gradle)

Na pasta [`oficina/`](oficina/):

```bash
./gradlew bootRun
```

Propriedades úteis (ver [`oficina/src/main/resources/application.properties`](oficina/src/main/resources/application.properties)):

- `app.jwt.secret` / `APP_JWT_SECRET`
- `app.security.admin.username` / `APP_SECURITY_ADMIN_USERNAME`
- `app.security.admin.password` / `APP_SECURITY_ADMIN_PASSWORD`

### Docker Compose (aplicação + PostgreSQL)

Na **raiz** do repositório:

```bash
cp .env.example .env
```

Edite `.env` (Postgres, datasource e **JWT/admin**). O Compose define **URL e usuário** padrão para o JDBC (`jdbc:postgresql://db:5432/oficina` / `oficina`) se não estiverem no `.env`; a **senha** (`SPRING_DATASOURCE_PASSWORD`) continua obrigatória via `.env` (igual à `POSTGRES_PASSWORD`). No perfil **docker**, **Spring Session está desligado** (`store-type=none`), pois a API usa JWT stateless e a sessão JDBC gerava conflito de DataSource quando variáveis vinham vazias.

Exemplo:

```dotenv
POSTGRES_DB=oficina
POSTGRES_USER=oficina
POSTGRES_PASSWORD=change-me-in-local-env

SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/oficina
SPRING_DATASOURCE_USERNAME=oficina
SPRING_DATASOURCE_PASSWORD=change-me-in-local-env

APP_JWT_SECRET=change-me-long-random-secret-for-hs256
APP_SECURITY_ADMIN_USERNAME=admin
APP_SECURITY_ADMIN_PASSWORD=change-me-in-local-env
```

```bash
docker compose up --build
```

- API: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`

## Testes e cobertura

```bash
cd oficina
./gradlew check
```

- Testes unitários e de integração (incluindo fluxo principal da OS com MockMvc + segurança).
- **JaCoCo:** `check` executa `jacocoTestCoverageVerification` com **mínimo de 80% de linhas** nos pacotes `domain` e `application` (classes de DTO em `application.api.dto` excluídas do cálculo por serem apenas estruturas de dados).
- Relatório HTML: `oficina/build/reports/jacoco/test/html/index.html`.

## Relatório de vulnerabilidades

Modelo e instruções de scan: [docs/relatorio-vulnerabilidades.md](docs/relatorio-vulnerabilidades.md). Inclua a saída das ferramentas no PDF de entrega da disciplina.

## Entrega (checklist institucional)

- Vídeo (até 15 min), documentação DDD no Miro, repositório privado com acesso **soat-architecture**, PDF de entrega com links e relatório de vulnerabilidades (preencher o modelo acima).
