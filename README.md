# Oficina

Monólito **Kotlin** com **Spring Boot** (JPA, Flyway, Spring Security OAuth2 Resource Server + JWT emitido pela aplicação), organizado em **arquitetura em camadas** alinhada ao Tech Challenge SOAT (MVP oficina mecânica).

## Stack

- Kotlin 2.3, Java 17, Gradle
- Spring Boot (web, data-jpa, validation, flyway, security, oauth2-resource-server, actuator)
- OpenAPI / Swagger UI (springdoc)
- Banco local (padrão): H2 em memória + schema criado/atualizado pelo Hibernate (`ddl-auto=update`; Flyway **desligado** neste perfil)
- Banco em Docker: **PostgreSQL** + **Flyway** (`validate` + scripts em `oficina/src/main/resources/db/migration`)

## Linguagem ubíqua (resumo)

| Termo | Significado |
| ----- | ----------- |
| **Ordem de serviço (OS)** | Recebida → Em diagnóstico → Aguardando aprovação interna → (reprovação interna → **Cancelada**) ou aprovação admin → atendente envia orçamento → Aguardando aprovação do cliente → Em execução → Finalizada → Entregue (ou cancelada pelo cliente). |
| **Documento (CPF/CNPJ)** | Identificação do cliente; validação de dígitos no domínio. |
| **Placa** | Identificação do veículo (padrão antigo ou Mercosul). |
| **Serviço (catálogo)** | Serviço cadastrado com preço e tempo estimado. |
| **Peça / insumo** | Estoque físico; **reserva** ao submeter o plano (técnico); **baixa** na confirmação de saída pelo almoxarife; **ponto de reposição** para alerta de estoque baixo. |
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
- **Login (público):** `POST /api/public/auth/login` — o JWT inclui escopos conforme o usuário (veja usuários demo abaixo).
- **Cliente (público, sem token):**
  - `GET /api/public/os/acompanhar?documento=&codigo=`
  - `POST /api/public/os/aprovar-orcamento?documento=&codigo=`
  - `POST /api/public/os/reprovar-orcamento?documento=&codigo=`

### Usuários in-memory (senha = username, exceto admin)

| Usuário      | Senha padrão | Escopo JWT   | Uso principal |
| ------------ | ------------ | ------------ | ------------- |
| `atendente`  | `atendente`  | `ATTENDANT`  | Criar OS, enviar orçamento ao cliente, entrega, voltar diagnóstico |
| `tecnico`    | `tecnico`    | `TECHNICIAN` | Diagnóstico, submeter plano (reserva), concluir serviços |
| `admin`      | `admin` (ou `APP_SECURITY_ADMIN_PASSWORD`) | `ADMIN` | Aprovar/reprovar plano interno, CRUD catálogo/peças/clientes/veículos, entrada de mercadoria, métricas |
| `almoxarife` | `almoxarife` | `WAREHOUSE`  | Listar reservas pendentes por OS, confirmar saída (baixa física), alertas de estoque baixo |

### Prefixos protegidos

- `/api/admin/**` — `SCOPE_ADMIN` (clientes, veículos, catálogo, peças + `POST .../pecas/{id}/entrada-mercadoria`, OS interno aprovar/reprovar, métricas).
- `/api/attendant/ordens-servico/**` — `SCOPE_ATTENDANT`.
- `/api/technician/ordens-servico/**` — `SCOPE_TECHNICIAN`.
- `/api/warehouse/**` — `SCOPE_WAREHOUSE` (reservas pendentes, confirmar saída, alertas).

Fluxo resumido: técnico `submeter-plano` → admin `aprovar-interno` → atendente `enviar-orcamento-cliente` → cliente aprova/reprova (público) → almoxarife `confirmar-saida` → técnico `concluir-servicos` → atendente `registrar-entrega`.

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
- `app.security.admin.password` / `APP_SECURITY_ADMIN_PASSWORD` (senha do usuário **admin**; demais usuários demo usam senha igual ao username)

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
# Senha do login "admin" (Postman/demo); altere em produção.
APP_SECURITY_ADMIN_PASSWORD=admin
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

Modelo e instruções de scan: [docs/relatorio-vulnerabilidades.md](oficina/docs/relatorio-vulnerabilidades.md). Inclua a saída das ferramentas no PDF de entrega da disciplina.

## Entrega (checklist institucional)

- Vídeo (até 15 min), documentação DDD no Miro, repositório privado com acesso **soat-architecture**, PDF de entrega com links e relatório de vulnerabilidades (preencher o modelo acima).
