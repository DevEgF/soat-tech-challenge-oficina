# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

All Gradle commands must be run from the `oficina/` subdirectory:

```bash
cd oficina

./gradlew bootRun              # Run locally (H2 in-memory, no Flyway)
./gradlew test                 # Run all tests
./gradlew check                # Run tests + enforce 80% JaCoCo coverage
./gradlew bootJar              # Build JAR for Docker
./gradlew jacocoTestReport     # Generate HTML coverage report (build/reports/jacoco/test/html/)

# Run a single test class
./gradlew test --tests "com.soat.tech.challenge.oficina.domain.OrdemServicoTest"
```

Docker deployment (from repo root):
```bash
cp .env.example .env  # Edit with Postgres credentials and JWT secret
docker compose up --build
```

## Architecture

Hexagonal (Ports & Adapters) + DDD, with strict dependency direction:
**Presentation → Application → Domain ← Infrastructure**

```
presentation/   REST controllers (role-scoped: admin, attendant, technician, warehouse, public)
application/    Use case orchestration (ApplicationService classes), DTOs, extension-function mappers
domain/         Aggregates, value objects, domain exceptions, repository port interfaces
infrastructure/ JPA entities, repository adapters, security (JWT/Spring Security), Flyway migrations
```

**Key design decisions:**
- **Domain ports** (interfaces in `domain/port/`) are implemented by **repository adapters** in `infrastructure/`. Application services depend only on ports, never JPA directly.
- **`OrdemServico`** is the core aggregate root with an **8-state machine**. State transitions are enforced via explicit methods (e.g., `iniciarDiagnostico()`, `submeterPlano()`). Invalid transitions throw `InvalidStatusTransitionException`.
- **Profiles:** Default profile uses H2 + Hibernate DDL auto (no Flyway); `docker` profile uses PostgreSQL + Flyway (`src/main/resources/db/migration/`); `test` profile uses H2 with `create-drop`.
- **Security:** Stateless JWT (HS256). Four in-memory users map to Spring Security scopes: `SCOPE_ADMIN`, `SCOPE_ATTENDANT`, `SCOPE_TECHNICIAN`, `SCOPE_WAREHOUSE`. Login via `POST /api/public/auth/login`.

## Work Order Swimlane (Business Process)

```
RECEBIDA
  ↓ tecnico: iniciar-diagnostico
EM_DIAGNOSTICO
  ↓ tecnico: submeter-plano (reserves parts)
AGUARDANDO_APROVACAO_INTERNA
  ↓ admin: aprovar-interno          ↓ admin: reprovar-interno → CANCELADA
AGUARDANDO_APROVACAO (customer quote sent by attendant)
  ↓ cliente: aprovar-orcamento      ↓ cliente: reprovar-orcamento → CANCELADA
EM_EXECUCAO (after warehouse confirms part exit)
  ↓ tecnico: concluir-servicos
FINALIZADA
  ↓ atendente: registrar-entrega
ENTREGUE
```
`CANCELADA` is reachable from any rejection (internal or customer).

Also: `atendente: voltar-diagnostico` sends `AGUARDANDO_APROVACAO` back to `EM_DIAGNOSTICO`.

## Layer-by-Layer Reference

### Domain (`domain/`)

**Aggregates / Entities:**
| Class | Role |
|---|---|
| `OrdemServico` | Core aggregate root; 8-state machine; tracks timestamps per transition; `serviceLines`/`partLines` mutable collections; `totalCents` auto-calculated |
| `Cliente` | Customer identified by `DocumentoFiscal` (CPF or CNPJ) |
| `Veiculo` | Vehicle identified by `PlacaVeiculo` (old or Mercosul format); belongs to a `Cliente` |
| `Peca` | Part/supply with stock; `withAdjustedStock(delta)` for safe mutation; `replenishmentPoint` triggers low-stock alerts |
| `ServicoCatalogo` | Catalog entry with price and estimated time |
| `ReservaPecaOs` | Stock reservation per work order (separate aggregate; status: PENDENTE → CONFIRMADA or CANCELADA) |
| `LinhaServicoOrdem` | Service line item inside an OS |
| `LinhaPecaOrdem` | Part line item inside an OS |

**Value Objects:**
| Class | Validation |
|---|---|
| `DocumentoFiscal` | Inline value class; validates CPF (11 digits, Mod-11 check) or CNPJ (14 digits, weighted check) |
| `PlacaVeiculo` | Inline value class; validates legacy (ABC-1234) and Mercosul (ABC1D23) formats |

**Status Enums:**
- `StatusOrdemServico`: 8 values (RECEBIDA, EM_DIAGNOSTICO, AGUARDANDO_APROVACAO_INTERNA, AGUARDANDO_APROVACAO, EM_EXECUCAO, FINALIZADA, ENTREGUE, CANCELADA)
- `StatusReservaPecaOs`: 3 values (PENDENTE, CONFIRMADA, CANCELADA)

**Domain Exceptions:**
- `DomainException` (base)
- `InvalidTaxDocumentException` → HTTP 400
- `InvalidLicensePlateException` → HTTP 400
- `InvalidStatusTransitionException` (carries `currentStatus` + `action`) → HTTP 409
- `InsufficientStockException` → HTTP 409

**Repository Ports (interfaces in `domain/port/`):**
`ClienteRepository`, `VeiculoRepository`, `PecaRepository`, `ServicoCatalogoRepository`, `OrdemServicoRepository`, `ReservaPecaOsRepository`, `MetricasServicoPort`

---

### Application (`application/`)

**7 ApplicationService classes** (all `@Service @Transactional`):

| Service | Key Use Cases |
|---|---|
| `OrdemServicoApplicationService` | create, list, get, track (public, masks tax ID), startDiagnosis, submitPlan (triggers reservations), approveInternal, rejectInternal, sendQuoteToCustomer, returnToDiagnosis, approveCustomerQuote, rejectCustomerQuote, completeServices, registerDelivery |
| `ClienteApplicationService` | CRUD + unique document validation |
| `VeiculoApplicationService` | CRUD + unique plate + customer ownership |
| `PecaApplicationService` | CRUD + recordGoodsReceipt (stock entry) + unique code |
| `ServicoCatalogoApplicationService` | CRUD |
| `WarehouseApplicationService` | listPendingReservations, confirmStockExit (PENDENTE→CONFIRMADA + deducts stock), listLowStockAlerts |
| `MetricasApplicationService` | averageTimeByService (in-memory aggregation of FINALIZADA/ENTREGUE orders) |

**Application exception:** `NotFoundException` → HTTP 404

**DTOs (24 classes in `application/api/dto/`):**
Request: `LoginRequest`, `ClienteRequest`, `VeiculoRequest`, `ServicoCatalogoRequest`, `PecaRequest`, `EntradaMercadoriaRequest`, `CriarOrdemServicoRequest`, `OrdemServicoLinhaServicoRequest`, `OrdemServicoLinhaPecaRequest`
Response: `LoginResponse`, `ClienteResponse`, `VeiculoResponse`, `ServicoCatalogoResponse`, `PecaResponse`, `OrdemServicoResponse`, `OrdemServicoLinhaServicoResponse`, `AcompanhamentoOsResponse`, `TempoMedioServicoResponse`, `ReservaPecaOsResponse`, `EstoqueAlertaResponse`

**Mappers:** Extension functions in `Mappers.kt` (e.g., `Cliente.toResponse()`, `OrdemServico.toResponse(serviceName, partName)`).

---

### Infrastructure (`infrastructure/`)

**JPA Entities (8):** `ClienteEntity`, `VeiculoEntity`, `ServicoCatalogoEntity`, `PecaEntity`, `OrdemServicoEntity`, `OrdemServicoLinhaServicoEntity`, `OrdemServicoLinhaPecaEntity`, `ReservaPecaOsEntity`

**Repository Adapters (7 `@Component`):** One per domain port; encapsulate all JPA ↔ domain mapping via `DomainEntityMapper.kt` extension functions.

Notable adapter behaviors:
- `OrdemServicoRepositoryAdapter.save()` clears and rebuilds line entities on every save.
- `ReservaPecaOsRepositoryAdapter.replacePendingReservations()` deletes old PENDENTE and recreates on plan resubmission.
- `ReservaPecaOsRepositoryAdapter.confirmPendingForWorkOrder()` deducts stock and validates non-negative quantity.
- `MetricasServicoAdapter` performs in-memory grouping (acceptable for MVP scale).

**Flyway Migrations:**
- `V1__init.sql` — core tables (clientes, veiculos, servicos_catalogo, pecas, ordens_servico, linhas_servico, linhas_peca)
- `V2__swimlane_reservas.sql` — adds `ponto_reposicao` to pecas; adds 3 timestamp columns to ordens_servico; creates `reservas_peca_os` table

**Security:**
- `SecurityConfiguration`: 4 in-memory users (atendente/ATTENDANT, tecnico/TECHNICIAN, admin/ADMIN, almoxarife/WAREHOUSE); stateless sessions; OAuth2 JWT resource server + explicit `ProviderManager` for form login.
- `JwtConfiguration`: SHA-256 of `APP_JWT_SECRET` → HMAC-SHA256 (HS256) signing key.
- `JwtIssuerService`: Issues signed JWT; default expiry 60 min (configurable via `APP_JWT_EXPIRATION_MINUTES`).

**Other config classes:** `ClockConfiguration` (UTC Clock bean), `JacksonKotlinConfiguration`, `OpenApiConfiguration` (Swagger at `/swagger-ui.html`).

---

### Presentation (`presentation/`)

**Controllers (9) and their endpoints:**

| Controller | Base Path | Required Scope | Endpoints |
|---|---|---|---|
| `AuthController` | `/api/public/auth` | public | `POST /login` |
| `PublicOrdemServicoController` | `/api/public/os` | public | `GET /acompanhar`, `POST /aprovar-orcamento`, `POST /reprovar-orcamento` |
| `AdminClienteController` | `/api/admin/clientes` | SCOPE_ADMIN | `POST` (201), `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` (204) |
| `AdminVeiculoController` | `/api/admin/veiculos` | SCOPE_ADMIN | `POST` (201), `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` (204) |
| `AdminServicoCatalogoController` | `/api/admin/servicos-catalogo` | SCOPE_ADMIN | `POST` (201), `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` (204) |
| `AdminPecaController` | `/api/admin/pecas` | SCOPE_ADMIN | `POST` (201), `GET`, `GET /{id}`, `PUT /{id}`, `POST /{id}/entrada-mercadoria`, `DELETE /{id}` (204) |
| `AdminOrdemServicoController` | `/api/admin/ordens-servico` | SCOPE_ADMIN | `GET`, `GET /{id}`, `POST /{id}/aprovar-interno`, `POST /{id}/reprovar-interno` |
| `AdminMetricasController` | `/api/admin/metricas` | SCOPE_ADMIN | `GET /tempo-medio-execucao-servicos` |
| `AttendantOrdemServicoController` | `/api/attendant/ordens-servico` | SCOPE_ATTENDANT | `POST` (201), `GET`, `GET /{id}`, `POST /{id}/enviar-orcamento-cliente`, `POST /{id}/voltar-diagnostico`, `POST /{id}/registrar-entrega` |
| `TechnicianOrdemServicoController` | `/api/technician/ordens-servico` | SCOPE_TECHNICIAN | `GET`, `GET /{id}`, `POST /{id}/iniciar-diagnostico`, `POST /{id}/submeter-plano`, `POST /{id}/concluir-servicos` |
| `WarehouseController` | `/api/warehouse` | SCOPE_WAREHOUSE | `GET /ordens-servico/{id}/reservas-pendentes`, `POST /ordens-servico/{id}/confirmar-saida` (204), `GET /alertas-estoque-baixo` |

**HTTP status conventions:** 201 for resource creation; 204 for DELETE and no-body actions; 400 for validation/domain input errors; 404 for NotFoundException; 409 for `InvalidStatusTransitionException` / `InsufficientStockException`; 401 for auth failures.

**Exception handler:** `RestExceptionHandler` (`@RestControllerAdvice`) maps all exceptions to `ErrorBody(message)`.

---

## Test Coverage Rules

JaCoCo enforces **80% line coverage** on `domain` and `application` packages. The `check` task fails if coverage drops below this threshold. DTO classes (`application/api/dto/**`) are excluded from the calculation.

Integration tests use `@SpringBootTest` + `@ActiveProfiles("test")` with H2 (`ddl-auto=create-drop`) and MockMvc. JWT authorities are mocked via Spring Security Test DSL — no real tokens needed in tests.

**Test files (15 total — `oficina/src/test/`):**

Unit (11): `OrdemServicoTest`, `OrdemServicoApplicationServiceTest`, `ClienteApplicationServiceTest`, `VeiculoApplicationServiceTest`, `PecaApplicationServiceTest`, `ServicoCatalogoApplicationServiceTest`, `WarehouseApplicationServiceTest`, `MetricasApplicationServiceTest`, `MappersTest`, `DocumentoFiscalTest`, `PlacaVeiculoTest`

Integration (3): `OrdemServicoFlowIntegrationTest`, `SwimlaneFlowsIntegrationTest`, `CurlTestsDocumentedFlowIntegrationTest`

**Test stack:** JUnit 5 · MockK (`mockk:1.13.10`) · Spring Boot Test · Spring Security Test · H2

---

## Domain Terminology (Linguagem Ubíqua)

| Term | Meaning |
|---|---|
| **Ordem de Serviço (OS)** | Work order aggregate root |
| **Documento** | CPF (11 digits) or CNPJ (14 digits) with check-digit validation (`DocumentoFiscal`) |
| **Placa** | License plate — legacy (ABC-1234) or Mercosul (ABC1D23) format (`PlacaVeiculo`) |
| **Serviço / Peça** | Catalog service and part/supply with stock management |
| **ReservaPecaOs** | Stock reservation per work order created when technician submits plan |
| **Código de Acompanhamento** | Public UUID on the OS for unauthenticated customer tracking |
| **Ponto de Reposição** | Replenishment threshold for low-stock alerts |
| **Entrada de Mercadoria** | Goods receipt — increases part stock quantity |

---

## Tech Challenge Deliverables Checklist

This project is the MVP back-end for the SOAT Phase 1 Tech Challenge.

| Item | Status | Notes |
|---|---|---|
| Work order full swimlane | ✅ | 8-state machine (PDF requires 6; extra: AGUARDANDO_APROVACAO_INTERNA adds internal approval) |
| CRUD clientes, veículos, serviços, peças | ✅ | Under `/api/admin/**` |
| Stock control with replenishment alerts | ✅ | `WarehouseApplicationService.listLowStockAlerts()` |
| Customer public tracking | ✅ | `GET /api/public/os/acompanhar` (masks document) |
| JWT authentication | ✅ | HS256, stateless, 4 in-memory users |
| CPF/CNPJ + plate validation | ✅ | Domain value objects with check-digit algorithms |
| Average execution time metrics | ✅ | `GET /api/admin/metricas/tempo-medio-execucao-servicos` |
| Swagger/OpenAPI | ✅ | `/swagger-ui.html` |
| Dockerfile + docker-compose | ✅ | Root of repo |
| 80% test coverage on critical domains | ✅ | JaCoCo enforced via `./gradlew check` |
| README.md | ✅ | `README.md` at repo root |
| DDD documentation | ✅ | `oficina/docs/doc.md` + `event.png` + Excalidraw link |
| Vulnerability report template | ⚠️ | Template at `oficina/docs/relatorio-vulnerabilidades.md`; must be filled with actual scan results |
| Video (up to 15 min) | ⚠️ | Must be recorded separately |
| Repository access for `soat-architecture` | ⚠️ | Must be configured on GitHub |
