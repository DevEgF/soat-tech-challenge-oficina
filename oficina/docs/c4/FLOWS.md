# Fluxos de Negócio - Sequence Diagrams

Este documento descreve os principais fluxos de negócio do sistema através de diagramas de sequência.

## 1. Autenticação e Geração de Token

```mermaid
sequenceDiagram
    actor User
    participant API as API REST
    participant Auth as AuthController
    participant JWT as JwtIssuerService
    participant DB as PostgreSQL
    
    User->>API: POST /auth/login
    Note over User,API: { username, password }
    
    API->>Auth: Recebe requisição
    Auth->>DB: SELECT user BY username
    DB-->>Auth: UserEntity
    
    alt Credenciais válidas
        Auth->>JWT: issueToken(user)
        JWT-->>Auth: JWT Token assinado
        Auth-->>API: 200 OK
        API-->>User: { token, expiresIn }
    else Credenciais inválidas
        Auth-->>API: 401 Unauthorized
        API-->>User: Erro de autenticação
    end
```

**Descrição**:
1. Usuário envia credenciais
2. Sistema busca usuário no banco
3. Valida senha (bcrypt)
4. Gera JWT assinado
5. Retorna token ao cliente

**Duração**: ~200ms

---

## 2. Criar Ordem de Trabalho

```mermaid
sequenceDiagram
    actor Attendant as Atendente
    participant API as API REST
    participant Controller as AttendantWO<br/>Controller
    participant Service as WorkOrder<br/>ApplicationService
    participant Domain as WorkOrder<br/>Domain
    participant Repo as Repository
    participant DB as PostgreSQL
    participant Email as Email Service
    
    Attendant->>API: POST /work-orders
    Note over Attendant,API: { customerId, vehicleId,<br/>description }
    
    API->>Controller: Autentica e autoriza
    Note over API,Controller: Valida JWT<br/>Verifica ROLE_ATENDENTE
    
    Controller->>Service: create(dto)
    
    Service->>Service: Valida entradas
    Service->>Repo: findCustomer(customerId)
    Repo->>DB: SELECT customer
    DB-->>Repo: Customer
    Repo-->>Service: Customer
    
    alt Cliente existe
        Service->>Domain: Cria WorkOrder
        Note over Service,Domain: status=CREATED<br/>startDate=now()
        
        Service->>Repo: save(workOrder)
        Repo->>DB: INSERT work_order
        DB-->>Repo: ID gerado
        Repo-->>Service: WorkOrder salva
        
        Service->>Email: notifyCreated(workOrder)
        Email-->>Service: Email queued
        
        Service-->>Controller: WorkOrderDTO
        Controller-->>API: 201 Created
        API-->>Attendant: { id, status, ... }
    else Cliente não existe
        Service-->>Controller: NotFoundException
        Controller-->>API: 404 Not Found
        API-->>Attendant: Erro
    end
```

**Descrição**:
1. Atendente envia dados da ordem
2. Sistema valida permissões
3. Verifica se cliente existe
4. Cria ordem com status CREATED
5. Persiste em PostgreSQL
6. Envia notificação por email
7. Retorna ordem criada

**Estados Envolvidos**: CREATED

**Duração**: ~500ms

---

## 3. Técnico Atualiza Status de Ordem

```mermaid
sequenceDiagram
    actor Technician as Técnico
    participant API as API REST
    participant Controller as TechnicianWO<br/>Controller
    participant Service as WorkOrder<br/>ApplicationService
    participant Domain as WorkOrder
    participant Repo as Repository
    participant DB as PostgreSQL
    participant Email as Email Service
    
    Technician->>API: PUT /work-orders/{id}
    Note over Technician,API: { status: IN_PROGRESS }
    
    API->>Controller: Autentica e autoriza
    Note over API,Controller: Valida JWT<br/>Verifica ROLE_TECNICO
    
    Controller->>Service: updateStatus(orderId, status)
    
    Service->>Repo: findById(orderId)
    Repo->>DB: SELECT work_order
    DB-->>Repo: WorkOrderEntity
    Repo-->>Service: WorkOrder
    
    alt Ordem existe
        Service->>Domain: Valida transição
        Note over Service,Domain: CREATED → IN_PROGRESS<br/>é válida?
        
        alt Transição válida
            Service->>Domain: updateStatus(IN_PROGRESS)
            Note over Domain: Atualiza estado
            Domain-->>Service: WorkOrder atualizada
            
            Service->>Repo: save(workOrder)
            Repo->>DB: UPDATE work_order
            DB-->>Repo: OK
            Repo-->>Service: WorkOrder
            
            Service->>Email: notifyStatusChanged(workOrder)
            
            Service-->>Controller: WorkOrderDTO
            Controller-->>API: 200 OK
            API-->>Technician: { id, status: IN_PROGRESS, ... }
        else Transição inválida
            Service-->>Controller: ConflictException
            Controller-->>API: 409 Conflict
            API-->>Technician: Transição não permitida
        end
    else Ordem não existe
        Service-->>Controller: NotFoundException
        Controller-->>API: 404 Not Found
        API-->>Technician: Ordem não encontrada
    end
```

**Descrição**:
1. Técnico atualiza status
2. Sistema valida permissão
3. Busca ordem no banco
4. Valida transição de estado
5. Atualiza status
6. Notifica clientes interessados

**Estados Possíveis**:
- CREATED → SCHEDULED
- SCHEDULED → IN_PROGRESS
- IN_PROGRESS → COMPLETED
- COMPLETED → CLOSED
- Qualquer → CANCELLED

**Duração**: ~400ms

---

## 4. Técnico Registra Peça Utilizada

```mermaid
sequenceDiagram
    actor Technician as Técnico
    participant API as API REST
    participant WOController as TechnicianWO<br/>Controller
    participant WOService as WorkOrder<br/>ApplicationService
    participant PartService as Part<br/>ApplicationService
    participant WORepo as WO Repository
    participant PartRepo as Part Repository
    participant DB as PostgreSQL
    
    Technician->>API: PUT /work-orders/{id}/parts
    Note over Technician,API: { partId, quantity }
    
    API->>WOController: Autentica e autoriza
    
    WOController->>WOService: addPart(orderId, partId, qty)
    
    WOService->>WORepo: findById(orderId)
    Repo->>DB: SELECT work_order
    DB-->>WORepo: WorkOrder
    WORepo-->>WOService: WorkOrder
    
    WOService->>PartService: consume(partId, quantity)
    
    PartService->>PartRepo: findById(partId)
    PartRepo->>DB: SELECT part
    DB-->>PartRepo: Part
    PartRepo-->>PartService: Part
    
    alt Quantidade disponível
        PartService->>PartService: Deduz quantidade
        PartService->>PartRepo: save(part)
        PartRepo->>DB: UPDATE part quantity
        DB-->>PartRepo: OK
        PartRepo-->>PartService: Part
        
        PartService-->>WOService: Part atualizada
        
        WOService->>WOService: Cria WorkOrderPart
        WOService->>WORepo: save(workOrder)
        WORepo->>DB: INSERT work_order_parts
        DB-->>WORepo: OK
        WORepo-->>WOService: WorkOrder
        
        WOService-->>WOController: WorkOrderDTO
        WOController-->>API: 200 OK
        API-->>Technician: { id, parts, ... }
    else Quantidade insuficiente
        PartService-->>WOService: InsufficientStockException
        WOService-->>WOController: Erro de estoque
        WOController-->>API: 409 Conflict
        API-->>Technician: Peça insuficiente
    end
```

**Descrição**:
1. Técnico registra peça utilizada
2. Sistema valida disponibilidade
3. Deduz quantidade do estoque
4. Registra peça na ordem
5. Atualiza total de custos

**Validações**:
- Quantidade em estoque >= quantidade solicitada
- Ordem existe e está em estado apropriado

**Duração**: ~350ms

---

## 5. Concluir Ordem de Trabalho

```mermaid
sequenceDiagram
    actor Technician as Técnico
    participant API as API REST
    participant Controller as TechnicianWO<br/>Controller
    participant Service as WorkOrder<br/>ApplicationService
    participant Metrics as Metrics<br/>ApplicationService
    participant Repo as Repository
    participant DB as PostgreSQL
    participant Email as Email Service
    
    Technician->>API: PUT /work-orders/{id}
    Note over Technician,API: { status: COMPLETED }
    
    API->>Controller: Autentica e autoriza
    
    Controller->>Service: updateStatus(orderId, COMPLETED)
    
    Service->>Repo: findById(orderId)
    Repo->>DB: SELECT work_order
    DB-->>Repo: WorkOrder
    Repo-->>Service: WorkOrder
    
    Service->>Service: Calcula total_cost
    Note over Service: SUM(parts) + SUM(services)
    
    Service->>Service: Define completed_date = now()
    
    Service->>Repo: save(workOrder)
    Repo->>DB: UPDATE work_order
    DB-->>Repo: OK
    Repo-->>Service: WorkOrder
    
    Service->>Metrics: recordCompletion(workOrder)
    Note over Service,Metrics: Atualiza métricas<br/>e estatísticas
    
    Service->>Email: notifyCompleted(workOrder)
    Note over Service,Email: Envia para cliente
    
    Service-->>Controller: WorkOrderDTO
    Controller-->>API: 200 OK
    API-->>Technician: { id, status: COMPLETED, ... }
```

**Descrição**:
1. Técnico marca ordem como completa
2. Sistema calcula custos totais
3. Define data de conclusão
4. Atualiza métricas
5. Notifica cliente

**Cálculos Automáticos**:
- Total = Σ(peças) + Σ(serviços)

**Duração**: ~600ms

---

## 6. Cliente Consulta Status de Ordem

```mermaid
sequenceDiagram
    actor Customer as Cliente
    participant API as API REST
    participant Controller as PublicWO<br/>Controller
    participant Service as WorkOrder<br/>ApplicationService
    participant Repo as Repository
    participant DB as PostgreSQL
    
    Customer->>API: GET /work-orders/{id}
    Note over Customer,API: Requer JWT<br/>Deve ser sua ordem
    
    API->>Controller: Autentica
    Note over API,Controller: Valida JWT<br/>Extrai usuário
    
    Controller->>Service: findById(orderId, userId)
    
    Service->>Repo: findById(orderId)
    Repo->>DB: SELECT work_order
    DB-->>Repo: WorkOrder
    Repo-->>Service: WorkOrder
    
    Service->>Service: Valida autorização
    Note over Service: workOrder.customerId<br/>== userId?
    
    alt Autorizado
        Service-->>Controller: WorkOrderDTO
        Controller-->>API: 200 OK
        API-->>Customer: {<br/>  id,<br/>  vehicle,<br/>  status,<br/>  startDate,<br/>  expectedEndDate,<br/>  parts,<br/>  totalCost<br/>}
    else Não autorizado
        Service-->>Controller: ForbiddenException
        Controller-->>API: 403 Forbidden
        API-->>Customer: Acesso negado
    end
```

**Descrição**:
1. Cliente solicita status de sua ordem
2. Sistema valida autorização
3. Retorna dados da ordem

**Segurança**:
- Cliente pode ver apenas suas próprias ordens
- Validação é feita em múltiplos níveis

**Duração**: ~200ms

---

## 7. Admin Gera Relatório de Métricas

```mermaid
sequenceDiagram
    actor Admin as Administrador
    participant API as API REST
    participant Controller as AdminMetrics<br/>Controller
    participant Service as Metrics<br/>ApplicationService
    participant Repo as Repository
    participant DB as PostgreSQL
    
    Admin->>API: GET /metrics?period=month
    Note over Admin,API: Requer ROLE_ADMIN
    
    API->>Controller: Autentica e autoriza
    Note over API,Controller: Valida JWT<br/>Verifica ROLE_ADMIN
    
    Controller->>Service: generateReport(filter)
    
    Service->>Repo: Múltiplas queries
    
    Repo->>DB: SELECT COUNT(*) FROM work_orders<br/>WHERE status = 'COMPLETED'
    DB-->>Repo: Total: 150
    Repo-->>Service: totalOrders: 150
    
    Repo->>DB: SELECT SUM(total_cost)<br/>FROM work_orders
    DB-->>Repo: 45000.00
    Repo-->>Service: totalRevenue: 45000.00
    
    Repo->>DB: SELECT COUNT(*) FROM parts<br/>WHERE quantity <= minimum_stock
    DB-->>Repo: 8
    Repo-->>Service: lowStockParts: 8
    
    Service->>Service: Calcula KPIs
    Note over Service: - Taxa conclusão<br/>- Ticket médio<br/>- Performance/técnico
    
    Service-->>Controller: MetricsDTO
    Controller-->>API: 200 OK
    API-->>Admin: {<br/>  totalOrders: 150,<br/>  completedOrders: 140,<br/>  completionRate: 93.3%,<br/>  totalRevenue: 45000.00,<br/>  averageTicket: 300.00,<br/>  lowStockParts: 8,<br/>  technicianMetrics: [...]<br/>}
```

**Descrição**:
1. Admin solicita métricas
2. Sistema executa múltiplas queries
3. Calcula KPIs agregados
4. Retorna relatório completo

**Métricas Disponíveis**:
- Total de ordens
- Ordens completadas
- Taxa de conclusão (%)
- Receita total
- Ticket médio
- Peças em falta
- Performance por técnico
- Tendências temporais

**Duração**: ~1000ms (querys agregadas)

---

## 8. Sistema de Baixo Estoque

```mermaid
sequenceDiagram
    participant DB as PostgreSQL
    participant Scheduler as Job Scheduler
    participant Service as Warehouse<br/>ApplicationService
    participant Repo as Repository
    participant Email as Email Service
    participant Admin as Admin (notificado)
    
    Note over DB,Admin: A cada 6 horas
    
    Scheduler->>Service: checkLowStock()
    
    Service->>Repo: findLowStockParts()
    
    Repo->>DB: SELECT * FROM parts<br/>WHERE quantity <= minimum_stock
    DB-->>Repo: [ Part1, Part2, ... ]
    Repo-->>Service: Low stock parts
    
    loop Para cada peça em falta
        Service->>Email: notifyAdminLowStock(part)
        Email-->>Admin: Email com lista de peças
    end
    
    Service->>Service: Log execution
```

**Descrição**:
1. Job scheduler executa periodicamente
2. Busca peças com estoque baixo
3. Envia notificação ao admin
4. Log para auditoria

**Frequência**: A cada 6 horas (configurável)

**Duração**: ~500ms

---

## Estados de Transição (State Machine)

```mermaid
stateDiagram-v2
    [*] --> CREATED: OrderCreated
    
    CREATED --> SCHEDULED: Schedule
    CREATED --> CANCELLED: Cancel
    
    SCHEDULED --> IN_PROGRESS: Start Work
    SCHEDULED --> CANCELLED: Cancel
    
    IN_PROGRESS --> COMPLETED: Finish Work
    IN_PROGRESS --> CANCELLED: Cancel
    
    COMPLETED --> CLOSED: Archive
    COMPLETED --> IN_PROGRESS: Reopen
    
    CLOSED --> [*]
    CANCELLED --> [*]
```

**Transições Permitidas**:

| De | Para | Quem | Condição |
|----|------|------|----------|
| CREATED | SCHEDULED | Admin/Técnico | Sempre |
| CREATED | CANCELLED | Admin | Sempre |
| SCHEDULED | IN_PROGRESS | Técnico | Sempre |
| SCHEDULED | CANCELLED | Admin | Sempre |
| IN_PROGRESS | COMPLETED | Técnico | Sempre |
| IN_PROGRESS | CANCELLED | Admin | Sempre |
| COMPLETED | CLOSED | Admin | Sempre |
| COMPLETED | IN_PROGRESS | Admin | Se falha for detectada |

---

## Padrão de Paginação

```mermaid
sequenceDiagram
    actor User
    participant API as API REST
    participant Controller as Controller
    participant Service as Service
    participant DB as PostgreSQL
    
    User->>API: GET /work-orders?page=0&size=10&sort=created_at,desc
    
    API->>Controller: Lista com paginação
    
    Controller->>Service: findAll(pageable)
    
    Service->>DB: SELECT * FROM work_orders<br/>ORDER BY created_at DESC<br/>LIMIT 10 OFFSET 0
    DB-->>Service: Page<WorkOrder>
    
    Service-->>Controller: Page<WorkOrderDTO>
    
    Controller-->>API: {<br/>  content: [...],<br/>  pageable: {...},<br/>  totalElements: 250,<br/>  totalPages: 25,<br/>  number: 0,<br/>  size: 10,<br/>  hasNext: true<br/>}
    
    API-->>User: JSON com página
```

---

Próximo passo: Implementar os controllers, services e repositories conforme estes fluxos.
