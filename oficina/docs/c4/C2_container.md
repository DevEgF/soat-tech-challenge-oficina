# C2 - Container

## Visão Geral

O diagrama C2 (Container) mostra a estrutura interna da aplicação e seus componentes principais, incluindo como os dados fluem entre eles.

## Diagrama

```mermaid
graph TB
    subgraph "Cliente"
        CLIENT["🌐 Cliente HTTP<br/>(Web Browser/<br/>Mobile App)"]
    end
    
    subgraph "API Oficina"
        API_LAYER["🎯 API REST Layer<br/>(Spring Boot)<br/>Port: 8080"]
        SECURITY["🔐 Security Module<br/>(Spring Security<br/>+ OAuth2 + JWT)"]
        VALIDATION["✅ Validation<br/>(Bean Validation<br/>+ Custom Validators)"]
    end
    
    subgraph "Business Logic"
        APP_SERVICES["⚙️ Application Services<br/>(Use Cases)<br/>- Workflow Orquestration<br/>- DTO Mapping<br/>- Business Rules"]
        DOMAIN_MODEL["📦 Domain Model<br/>(Entidades)<br/>- Customer<br/>- Vehicle<br/>- WorkOrder<br/>- Part<br/>- CatalogService"]
    end
    
    subgraph "Data & Infrastructure"
        REPOSITORIES["💾 Repositories<br/>(JPA/Hibernate)<br/>- CustomerRepository<br/>- VehicleRepository<br/>- WorkOrderRepository<br/>- PartRepository<br/>- CatalogRepository"]
        JPA["🗂️ JPA Entities<br/>(Database Mapping)<br/>- CustomerEntity<br/>- VehicleEntity<br/>- WorkOrderEntity<br/>- PartEntity<br/>- ServiceEntity")
        MIGRATIONS["🔄 Migrations<br/>(Flyway)<br/>SQL Schema Versioning")
    end
    
    subgraph "Persistence"
        DB["🗄️ PostgreSQL<br/>(Primary Data Store)<br/>- Customers<br/>- Vehicles<br/>- WorkOrders<br/>- Parts<br/>- Services<br/>- Users/Audit")
    end
    
    subgraph "External Services"
        EMAIL["📧 Email Service<br/>(Notifications)<br/>Order Updates<br/>Confirmations")
        MONITORING["📊 Monitoring<br/>(Spring Actuator)<br/>Health Checks<br/>Metrics")
    end
    
    subgraph "Documentation"
        SWAGGER["📚 OpenAPI/Swagger<br/>(API Documentation)<br/>Interactive API Docs")
    end
    
    CLIENT -->|"HTTP Request<br/>(JSON)"| API_LAYER
    API_LAYER -->|"Route"| SECURITY
    SECURITY -->|"Validate JWT<br/>Check Permissions"| VALIDATION
    VALIDATION -->|"Forward Valid<br/>Requests"| APP_SERVICES
    
    APP_SERVICES -->|"Apply Business Rules<br/>Validate Domain"| DOMAIN_MODEL
    APP_SERVICES -->|"Read/Write"| REPOSITORIES
    
    REPOSITORIES -->|"Map Entity"| JPA
    JPA -->|"SQL Queries<br/>(Hibernate)"| DB
    REPOSITORIES -->|"Check Schema"| MIGRATIONS
    MIGRATIONS -->|"Ensure Schema<br/>Consistency"| DB
    
    APP_SERVICES -->|"Queue Notifications"| EMAIL
    
    API_LAYER -->|"Expose Metrics"| MONITORING
    API_LAYER -->|"Generate Docs"| SWAGGER
    
    CLIENT -->|"Access Docs"| SWAGGER
    CLIENT -->|"Check Health"| MONITORING
    
    style API_LAYER fill:#4A90E2,color:#fff
    style SECURITY fill:#E24A4A,color:#fff
    style VALIDATION fill:#E29C4A,color:#fff
    style APP_SERVICES fill:#7E57C2,color:#fff
    style DOMAIN_MODEL fill:#5C7CBA,color:#fff
    style REPOSITORIES fill:#26A69A,color:#fff
    style DB fill:#2E3B3A,color:#fff
```

## Componentes Principais

### 🎯 API REST Layer (Spring Boot)
**Framework**: Spring Boot 4.1.0 com Kotlin

**Responsabilidades**:
- Expor endpoints REST para todas as operações
- Mapear requisições HTTP para objetos Java
- Serializar/desserializar JSON
- Retornar respostas HTTP apropriadas

**Endpoints Principais**:
```
POST   /auth/login                    → Autenticar usuário
GET    /work-orders                   → Listar ordens
POST   /work-orders                   → Criar ordem
PUT    /work-orders/{id}              → Atualizar ordem
GET    /customers                     → Listar clientes
POST   /customers                     → Criar cliente
GET    /parts                         → Listar peças
GET    /metrics                       → Métricas do sistema
```

### 🔐 Security Module
**Componentes**:
- Spring Security
- OAuth2 Resource Server
- JWT (Nimbus JOSE JWT)

**Responsabilidades**:
- Validar tokens JWT
- Extrair claims (usuário, papéis)
- Aplicar RBAC (Role-Based Access Control)
- Proteger endpoints sensíveis

**Fluxo de Autenticação**:
```
1. Cliente envia credenciais
2. Sistema gera JWT (assinado)
3. Cliente inclui JWT em requisições subsequentes
4. Security Module valida JWT
5. Autorização baseada em roles
```

### ✅ Validation Module
**Tecnologias**:
- Bean Validation (JSR-380)
- Custom Validators

**Responsabilidades**:
- Validar entrada do usuário
- Garantir integridade de dados
- Retornar erros descritivos

### ⚙️ Application Services
**Padrão**: Service Layer / Use Cases

**Principais Services**:
- `CustomerApplicationService`: Gerenciamento de clientes
- `VehicleApplicationService`: Gerenciamento de veículos
- `WorkOrderApplicationService`: Orquestração de ordens de trabalho
- `PartApplicationService`: Gerenciamento de peças
- `CatalogServiceApplicationService`: Catálogo de serviços
- `MetricsApplicationService`: Geração de métricas
- `WarehouseApplicationService`: Gestão de estoque

**Responsabilidades**:
- Implementar casos de uso (aplicação)
- Orquestrar fluxos de negócio
- Mapear DTOs para entidades de domínio
- Chamar repositories para persistência

### 📦 Domain Model
**Entidades de Negócio**:
- `Customer`: Informações de clientes
- `Vehicle`: Veículos em manutenção
- `WorkOrder`: Ordens de trabalho
- `Part`: Peças/componentes em estoque
- `CatalogService`: Serviços oferecidos
- `User`: Informações de usuários (com roles)

**Responsabilidades**:
- Representar conceitos de negócio
- Encapsular regras de negócio
- Validar invariantes de domínio

### 💾 Repositories
**Padrão**: Repository Pattern (Data Access Objects)

**Implementação**: Spring Data JPA

**Repositórios**:
- `CustomerRepository`
- `VehicleRepository`
- `WorkOrderRepository`
- `PartRepository`
- `CatalogServiceRepository`
- `UserRepository`
- `AuditLogRepository`

**Responsabilidades**:
- Abstração de acesso a dados
- Consultas customizadas
- Transações de banco de dados

### 🗂️ JPA Entities
**Mapeamento**: ORM via Hibernate

**Características**:
- Anotações `@Entity`, `@Table`
- Relacionamentos (OneToMany, ManyToOne, etc.)
- Validação via Bean Validation
- Auditoria (createdAt, updatedAt)

### 🔄 Migrations
**Ferramenta**: Flyway

**Responsabilidades**:
- Versionamento de schema
- Rollback automático
- Consistência de estrutura
- Histórico de mudanças

**Localização**: `src/main/resources/db/migration/`

### 🗄️ PostgreSQL Database
**Versão**: Suportada via driver postgresql

**Características**:
- Transações ACID
- Índices para performance
- Constraints de integridade
- Backup e recovery

**Principais Tabelas**:
```sql
- customers
- vehicles
- work_orders
- work_order_parts
- parts
- catalog_services
- users
- audit_logs
```

### 📧 Email Service
**Responsabilidade**: Enviar notificações

**Eventos**:
- Ordem criada
- Ordem atualizada
- Ordem concluída
- Confirmação de agendamento

**Status**: Integração configurável

### 📊 Monitoring
**Spring Actuator Endpoints**:
- `/actuator/health`: Status da aplicação
- `/actuator/metrics`: Métricas em tempo real
- `/actuator/info`: Informações da aplicação

### 📚 OpenAPI/Swagger
**Documentação Interativa**:
- `/swagger-ui.html`: Interface gráfica
- `/v3/api-docs`: Especificação OpenAPI JSON

## Fluxos de Dados

### Fluxo 1: Criar Ordem de Trabalho
```
1. Cliente HTTP envia POST /work-orders
2. API REST recebe requisição
3. Security valida JWT
4. Validation verifica dados de entrada
5. WorkOrderApplicationService implementa caso de uso
6. Domain Model valida regras de negócio
7. Repository persiste em PostgreSQL
8. Email Service envia notificação
9. API retorna resposta HTTP 201 Created
```

### Fluxo 2: Consultar Ordens
```
1. Cliente HTTP envia GET /work-orders
2. API REST recebe requisição
3. Security valida JWT e verifica permissões
4. WorkOrderApplicationService busca dados
5. Repository executa SELECT no PostgreSQL
6. Dados são mapeados para DTO
7. JSON é serializado
8. API retorna resposta HTTP 200 OK
```

### Fluxo 3: Atualizar Status
```
1. Cliente HTTP envia PUT /work-orders/{id}
2. API REST recebe requisição
3. Security valida autenticação e autorização
4. Validation verifica novos dados
5. WorkOrderApplicationService atualiza estado
6. Domain Model valida transição de estado
7. Repository atualiza em PostgreSQL
8. Email Service notifica clientes interessados
9. API retorna resposta HTTP 200 OK
```

## Dependências Entre Containers

```
Cliente HTTP
    ↓
API REST Layer (obrigatório)
    ↓
Security Module (obrigatório)
    ↓
Validation Module (obrigatório)
    ↓
Application Services (obrigatório)
    ↓
Domain Model (obrigatório)
    ↓
Repositories (obrigatório)
    ↓
PostgreSQL Database (obrigatório)

    + Email Service (optional)
    + Monitoring (optional)
    + OpenAPI Documentation (optional)
```

## Configuração

### Arquivo: `application.yml`
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/oficina
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER}
          jwk-set-uri: ${JWT_JWK_SET_URI}
```

## Tecnologias por Container

| Container | Versão | Tecnologia |
|-----------|--------|-----------|
| API REST | 4.1.0 | Spring Boot |
| Security | 6.1.x | Spring Security + OAuth2 |
| Validation | 3.0.x | Jakarta Bean Validation |
| App Services | Custom | Kotlin |
| Domain | Custom | Kotlin |
| Repositories | 3.2.x | Spring Data JPA |
| JPA | 6.0.x | Hibernate |
| Migrations | Latest | Flyway |
| Database | 15+ | PostgreSQL |

---

**Próximo passo**: Ver [C3 - Component](./C3_component.md) para entender os componentes internos.
