# 📑 Índice Completo - Documentação C4 Oficina Backend

## Documentação Principal

### 🎯 [QUICKSTART.md](./QUICKSTART.md) - **COMECE AQUI!**
**Guia rápido** para navegar toda a documentação.
- Roteiros de aprendizado personalizados
- Resumo da arquitetura
- Endpoints principais
- Setup inicial

### 📖 [README.md](./README.md)
**Visão geral do projeto** com tecnologias e padrões.
- Stack tecnológico completo
- Estrutura em camadas
- Fluxo genérico de requisições
- Recursos de segurança

---

## Modelos C4

### C1: System Context
📄 **[C1_system_context.md](./C1_system_context.md)**

Mostra o sistema em relação aos seus usuários e sistemas externos.

**Contém**:
- 5 atores principais (Cliente, Atendente, Técnico, Admin)
- 1 sistema externo (Email)
- Fluxos de interação de alto nível
- Limitações e cenários de uso

**Para quem**: Arquitetos, Product Managers, Stakeholders

**Diagramas**: System Context (Mermaid)

---

### C2: Container
📄 **[C2_container.md](./C2_container.md)**

Arquitetura interna com 8 containers principais.

**Contém**:
- API REST Layer (Spring Boot)
- Security Module (JWT + OAuth2)
- Validation Module
- Application Services
- Domain Model
- Repositories (JPA)
- Database (PostgreSQL)
- Email Service

**Para quem**: Arquitetos, Tech Leads, Desenvolvedores Backend

**Diagramas**: Container Architecture (Mermaid)

**Principais Fluxos**:
1. Criar Ordem de Trabalho
2. Consultar Ordens
3. Atualizar Status

---

### C3: Component
📄 **[C3_component.md](./C3_component.md)**

Detalhe fino com 30+ componentes internos.

**Contém**:
- 15 Controllers REST
- 7 Application Services
- 8 Domain Models
- 6 Repositories
- Configurações de Infrastructure

**Para quem**: Desenvolvedores Backend, Code Reviewers, Arquitetos

**Diagramas**: Component Architecture (Mermaid) + Fluxo detalhado

**Controllers Listados**:
- AuthController
- AdminCustomerController
- AdminVehicleController
- AdminWorkOrderController
- TechnicianWorkOrderController
- AttendantWorkOrderController
- PublicWorkOrderController
- AdminPartController
- TechnicianPartController
- AdminCatalogServiceController
- TechnicianCatalogServiceController
- WarehouseController
- AdminMetricsController
- RestExceptionHandler

---

## Especificações Técnicas

### 🗄️ Database Schema
📄 **[DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md)**

Schema relacional completo do PostgreSQL.

**Contém**:
- 9 tabelas com DDL completo
- Entity-Relationship Diagram (ER)
- Relacionamentos e cascatas
- Índices estratégicos
- Constraints e unicidade
- Exemplos de queries comuns

**Tabelas**:
1. `users` - Usuários do sistema
2. `customers` - Dados de clientes
3. `vehicles` - Veículos em manutenção
4. `work_orders` - Ordens de trabalho
5. `work_order_parts` - Peças utilizadas
6. `parts` - Estoque de peças
7. `catalog_services` - Catálogo de serviços
8. `work_order_services` - Serviços aplicados
9. `audit_logs` - Auditoria de operações

**Para quem**: DBAs, Desenvolvedores Backend, Arquitetos

**Diagramas**: ER Diagram (Mermaid)

---

### 🔄 Fluxos de Negócio
📄 **[FLOWS.md](./FLOWS.md)**

Diagramas de sequência para 8 fluxos principais.

**Fluxos Documentados**:
1. ✅ Autenticação e Geração de Token
2. ✅ Criar Ordem de Trabalho
3. ✅ Técnico Atualiza Status
4. ✅ Técnico Registra Peça Utilizada
5. ✅ Concluir Ordem de Trabalho
6. ✅ Cliente Consulta Status
7. ✅ Admin Gera Relatórios
8. ✅ Sistema de Baixo Estoque

**Inclui**:
- Sequence Diagrams (Mermaid)
- State Machine (transições de ordem)
- Validações em cada etapa
- Padrão de paginação
- Timing e performance

**Para quem**: Desenvolvedores, QA, Product Managers

**Diagramas**: Sequence Diagrams (8x) + State Machine

---

## 🗺️ Mapa Mental da Arquitetura

```
Oficina Backend
│
├── 📡 Presentation Layer (REST)
│   ├── AuthController
│   ├── CustomerController
│   ├── VehicleController
│   ├── WorkOrderController (3x por papel)
│   ├── PartController (2x)
│   ├── CatalogServiceController (2x)
│   ├── WarehouseController
│   ├── MetricsController
│   └── ExceptionHandler
│
├── ⚙️ Application Layer (Use Cases)
│   ├── CustomerApplicationService
│   ├── VehicleApplicationService
│   ├── WorkOrderApplicationService
│   ├── PartApplicationService
│   ├── CatalogServiceApplicationService
│   ├── WarehouseApplicationService
│   ├── MetricsApplicationService
│   └── ResendEmailService
│
├── 📦 Domain Layer (Core)
│   ├── Customer (entity)
│   ├── Vehicle (entity)
│   ├── WorkOrder (entity)
│   ├── Part (entity)
│   ├── CatalogService (entity)
│   ├── User (entity)
│   └── Ports (interfaces)
│
├── 🔒 Infrastructure Layer
│   ├── Repositories (JPA)
│   │   ├── CustomerRepository
│   │   ├── VehicleRepository
│   │   ├── WorkOrderRepository
│   │   ├── PartRepository
│   │   ├── CatalogServiceRepository
│   │   └── UserRepository
│   │
│   ├── JPA Entities
│   │   └── (Mapeamento de tabelas)
│   │
│   ├── Configurations
│   │   ├── SecurityConfiguration
│   │   ├── JwtConfiguration
│   │   ├── JacksonConfiguration
│   │   ├── ClockConfiguration
│   │   └── OpenApiConfiguration
│   │
│   └── Migrations
│       └── Flyway (versioned SQL)
│
└── 🗄️ PostgreSQL Database
    ├── users
    ├── customers
    ├── vehicles
    ├── work_orders
    ├── work_order_parts
    ├── parts
    ├── catalog_services
    ├── work_order_services
    └── audit_logs
```

---

## 👥 Papéis de Usuário

### 👤 Cliente
- Visualizar suas próprias ordens
- Consultar status
- Receber notificações

### 👥 Atendente
- Criar ordens de trabalho
- Registrar dados de clientes
- Consultar estoque de peças
- Visualizar catálogo

### 🔧 Técnico
- Visualizar ordens atribuídas
- Atualizar status
- Registrar peças utilizadas
- Atualizar catálogo

### 👨‍💼 Admin
- Gerenciar clientes
- Gerenciar veículos
- Gerenciar ordens (todas)
- Gerenciar estoque
- Gerar relatórios e métricas
- Gerenciar usuários

---

## 🔐 Segurança

### Autenticação
- **Tipo**: JWT + OAuth2
- **Validação**: HMAC assinado
- **Storage**: Bearer Token em Authorization header

### Autorização
- **Modelo**: RBAC (Role-Based Access Control)
- **Papéis**: ADMIN, TECNICO, ATENDENTE, CLIENTE
- **Annotations**: @PreAuthorize, @Secured

### Validação
- **Input**: Bean Validation (@Valid, @NotNull, etc)
- **Domain**: Regras de negócio
- **Error Handling**: RestExceptionHandler

---

## 📊 Métricas Principais

```
Ordens de Trabalho
├── Total de ordens
├── Ordens completadas
├── Taxa de conclusão (%)
├── Tempo médio de execução
└── Custo médio (ticket)

Estoque
├── Peças em falta
├── Valor total em estoque
├── Peças mais utilizadas
└── Taxa de rotatividade

Financeiro
├── Receita total
├── Receita por período
├── Receita por serviço
└── Margem média

Performance
├── Eficiência por técnico
├── Tempo médio de atendimento
├── Taxa de retrabalho
└── Satisfação (se disponível)
```

---

## 🔄 Transições de Estado (WorkOrder)

```
CREATED     → Estado inicial
  ↓
SCHEDULED   → Agendada
  ↓
IN_PROGRESS → Em execução
  ↓
COMPLETED   → Finalizada (aberta para reabrir)
  ↓
CLOSED      → Fechada (final)

CANCELLED   → Cancelada (a qualquer momento)
```

---

## 📚 Recursos Externos

### Documentação API
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Health & Metrics
- **Health Check**: `http://localhost:8080/actuator/health`
- **Métricas**: `http://localhost:8080/actuator/metrics`

### Base de Dados
- **Host**: localhost
- **Port**: 5432
- **Database**: oficina
- **User**: (variável de ambiente)

---

## 🚀 Roteiros de Estudo

### ⏱️ Rápido (15 min)
1. QUICKSTART.md
2. C1_system_context.md

### 📈 Intermediário (60 min)
1. README.md
2. C1_system_context.md
3. C2_container.md
4. FLOWS.md

### 🎓 Completo (120 min)
1. README.md
2. C1_system_context.md
3. C2_container.md
4. C3_component.md
5. DATABASE_SCHEMA.md
6. FLOWS.md
+ Explorar código-fonte

### 👨‍💻 Desenvolvedor Novo (90 min)
1. QUICKSTART.md
2. C2_container.md
3. C3_component.md
4. FLOWS.md (fluxos relevantes)
5. Código-fonte

### 🏗️ Arquiteto (120 min)
1. README.md
2. C1_system_context.md
3. C2_container.md
4. C3_component.md
5. DATABASE_SCHEMA.md

---

## 📋 Matriz de Acesso

| Documento | Dev Backend | Arquiteto | DBA | Product | QA |
|-----------|:----------:|:---------:|:---:|:-------:|:--:|
| README.md | ✅ | ✅ | ❌ | ✅ | ✅ |
| C1 | ❌ | ✅ | ❌ | ✅ | ✅ |
| C2 | ✅ | ✅ | ✅ | ❌ | ✅ |
| C3 | ✅ | ✅ | ❌ | ❌ | ✅ |
| DB Schema | ✅ | ✅ | ✅ | ❌ | ✅ |
| Flows | ✅ | ✅ | ❌ | ✅ | ✅ |
| Quickstart | ✅ | ✅ | ✅ | ❌ | ✅ |

---

## 🔍 Como Navegar

### Por Interesse
**"Quero entender o sistema como um todo"**
→ C1 + C2

**"Quero implementar um novo recurso"**
→ C3 + FLOWS + DATABASE_SCHEMA

**"Quero fazer code review"**
→ C3 + FLOWS

**"Quero gerenciar o projeto"**
→ C1 + FLOWS + README

**"Quero otimizar o banco"**
→ DATABASE_SCHEMA + FLOWS

### Por Papel
**Desenvolvedor Backend**
→ C2, C3, DATABASE_SCHEMA, FLOWS

**Arquiteto**
→ C1, C2, C3, DATABASE_SCHEMA

**Product Manager**
→ C1, FLOWS, QUICKSTART

**DBA**
→ DATABASE_SCHEMA, C2

**QA/Tester**
→ C1, C3, FLOWS

---

## ✅ Checklist de Completude

- [x] README - Visão geral
- [x] C1 - System Context
- [x] C2 - Container Architecture
- [x] C3 - Component Details
- [x] DATABASE_SCHEMA - ER Diagram + DDL
- [x] FLOWS - Sequence Diagrams
- [x] QUICKSTART - Guia rápido
- [x] INDEX - Este documento

---

## 📞 Suporte

**Dúvidas sobre arquitetura?**
1. Consulte QUICKSTART.md
2. Procure no documento relevante (Ctrl+F)
3. Veja o código-fonte correlato
4. Consulte git history (git blame)

**Encontrou erro?**
1. Abra uma issue
2. Sugira melhorias no PR

---

**Última atualização**: Maio 2026
**Versão**: 1.0
**Status**: ✅ Completo

---

🎓 **Bem-vindo à Oficina Backend!**

Comece pelo [QUICKSTART.md](./QUICKSTART.md) 🚀
