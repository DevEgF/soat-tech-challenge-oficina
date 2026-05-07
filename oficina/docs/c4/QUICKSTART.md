# Quick Start - Guia Rápido da Documentação C4

Bem-vindo à documentação de arquitetura do **Oficina Backend**! Este guia ajudará você a navegar e entender a estrutura do sistema.

## 📚 Documentos Disponíveis

### 1. **[README.md](./README.md)** - Visão Geral Geral
- Tecnologias utilizadas
- Estrutura de camadas
- Padrões de arquitetura
- Fluxo genérico de uma requisição

**Tempo de leitura**: ~5 minutos
**Para**: Todos os níveis

### 2. **[C1_system_context.md](./C1_system_context.md)** - Contexto do Sistema
Entenda o sistema em relação aos seus usuários e sistemas externos.

**O que você aprenderá**:
- Quem usa o sistema (Cliente, Atendente, Técnico, Admin)
- Como o sistema interage com serviços externos (Email)
- Fluxos de negócio de alto nível

**Diagrama**: System Context (atores + sistema)

**Tempo de leitura**: ~10 minutos
**Para**: Product Managers, Stakeholders, Arquitetos

### 3. **[C2_container.md](./C2_container.md)** - Arquitetura de Contêineres
Entenda os componentes principais da aplicação e como se comunicam.

**O que você aprenderá**:
- Componentes principais (API, Security, Services, Database)
- Tecnologias usadas em cada camada
- Fluxos de dados entre componentes
- Dependências entre containers

**Diagramas**: Container architecture com 7 principais containers

**Tempo de leitura**: ~15 minutos
**Para**: Arquitetos, Tech Leads, Desenvolvedores Backend

### 4. **[C3_component.md](./C3_component.md)** - Componentes Internos
Detalhe fino da arquitetura interna com controladores, serviços e repositórios.

**O que você aprenderá**:
- Todos os controladores REST
- Todos os serviços de aplicação
- Modelos de domínio
- Repositórios e padrões de acesso a dados
- Fluxo detalhado de uma requisição

**Diagramas**: Component diagram com 30+ componentes

**Tempo de leitura**: ~20 minutos
**Para**: Desenvolvedores Backend, Code Reviewers

### 5. **[DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md)** - Schema do Banco de Dados
Entenda estrutura de tabelas, relacionamentos e índices.

**O que você aprenderá**:
- Todas as 8 tabelas principais
- Relacionamentos entre tabelas
- Constraints e índices
- Exemplos de queries comuns
- Integridade referencial

**Diagramas**: ER (Entity-Relationship) diagram

**Tempo de leitura**: ~15 minutos
**Para**: DBAs, Desenvolvedores Backend, Arquitetos de Dados

### 6. **[FLOWS.md](./FLOWS.md)** - Fluxos de Negócio
Entenda os principais fluxos através de diagramas de sequência.

**Fluxos Documentados**:
1. Autenticação e Geração de Token
2. Criar Ordem de Trabalho
3. Técnico Atualiza Status
4. Técnico Registra Peça Utilizada
5. Concluir Ordem de Trabalho
6. Cliente Consulta Status
7. Admin Gera Relatórios
8. Alertas de Baixo Estoque

**Tempo de leitura**: ~20 minutos
**Para**: Desenvolvedores, QA, Product Managers

---

## 🎯 Roteiros de Aprendizado

### Para Novo Dev Backend
```
1. README.md (5 min)
   ↓
2. C1_system_context.md (10 min)
   ↓
3. C2_container.md (15 min)
   ↓
4. C3_component.md (20 min)
   ↓
5. DATABASE_SCHEMA.md (15 min)
   ↓
6. FLOWS.md (20 min)
   ↓
7. Explorar código-fonte

Total: ~85 minutos de leitura
```

### Para Code Review
```
1. C3_component.md (20 min)
   ↓
2. FLOWS.md (20 min)
   ↓
3. Revisar código alterado
```

### Para Arquiteto de Sistema
```
1. README.md (5 min)
   ↓
2. C1_system_context.md (10 min)
   ↓
3. C2_container.md (15 min)
   ↓
4. DATABASE_SCHEMA.md (15 min)
   ↓
5. Avaliar decisões arquiteturais
```

### Para Gestor/Product Manager
```
1. README.md (5 min)
   ↓
2. C1_system_context.md (10 min)
   ↓
3. FLOWS.md (20 min)

Total: ~35 minutos
```

---

## 🏗️ Estrutura da Arquitetura (Resumo)

```
PRESENTATION LAYER (REST)
    ↓
APPLICATION LAYER (Business Logic)
    ↓
DOMAIN LAYER (Core Models)
    ↓
INFRASTRUCTURE LAYER (Repositories, Config)
    ↓
DATABASE (PostgreSQL)
```

### Camadas Explicadas

| Camada | Responsabilidade | Exemplos |
|--------|------------------|----------|
| **Presentation** | Expor APIs REST, Validar HTTP | Controllers (Auth, Customer, WorkOrder) |
| **Application** | Implementar casos de uso | ApplicationServices, DTOs |
| **Domain** | Lógica de negócio pura | Entidades, Regras de negócio |
| **Infrastructure** | Persistência e configurações | Repositories, JPA, Security Config |

---

## 🔐 Segurança em Resumo

```
Autenticação
├─ JWT Token
└─ OAuth2 Resource Server

Autorização
├─ Role-Based Access Control (RBAC)
└─ 4 papéis: ADMIN, TECNICO, ATENDENTE, CLIENTE

Validação
├─ Bean Validation (entradas)
└─ Business Rules (domínio)
```

---

## 📊 Principais Entidades

| Entidade | Propósito | Relacionamentos |
|----------|-----------|-----------------|
| **Customer** | Dados dos clientes | vehicles, work_orders |
| **Vehicle** | Veículos em manutenção | customer, work_orders |
| **WorkOrder** | Ordens de trabalho | customer, vehicle, parts, services |
| **Part** | Estoque de peças | work_orders (via work_order_parts) |
| **CatalogService** | Serviços oferecidos | work_orders (via work_order_services) |
| **User** | Usuários do sistema | roles, audit_logs |

---

## 📋 Endpoints Principais por Papel

### 👤 Cliente (Public)
```
GET  /work-orders/{id}              Visualizar sua ordem
```

### 👥 Atendente
```
POST   /work-orders                 Criar ordem de trabalho
GET    /parts                       Listar peças disponíveis
GET    /catalog-services            Listar serviços
```

### 🔧 Técnico
```
PUT    /work-orders/{id}            Atualizar status
PUT    /work-orders/{id}/parts      Registrar peça usada
PUT    /catalog-services/{id}       Atualizar serviço
```

### 👨‍💼 Admin (Full Access)
```
GET    /customers                   Listar clientes
POST   /customers                   Criar cliente
PUT    /customers/{id}              Atualizar cliente
GET    /vehicles                    Listar veículos
POST   /vehicles                    Criar veículo
GET    /work-orders                 Listar todas as ordens
GET    /parts                       Gerenciar estoque
GET    /catalog-services            Gerenciar catálogo
GET    /metrics                     Gerar relatórios
```

---

## 🔄 Estados de Ordem de Trabalho

```
CREATED → SCHEDULED → IN_PROGRESS → COMPLETED → CLOSED
  ↑                                      ↑
  └──────── CANCELLED (a qualquer tempo)
            REOPEN (de COMPLETED)
```

**Transições Válidas**:
- ✅ CREATED → SCHEDULED (por Admin)
- ✅ SCHEDULED → IN_PROGRESS (por Técnico)
- ✅ IN_PROGRESS → COMPLETED (por Técnico)
- ✅ COMPLETED → CLOSED (por Admin)
- ✅ Qualquer → CANCELLED (por Admin)
- ✅ COMPLETED → IN_PROGRESS (por Admin, reabrir)

---

## 🗄️ Banco de Dados em 30 Segundos

**Tipo**: PostgreSQL

**Tabelas Principais**:
- `users` - Usuários do sistema
- `customers` - Clientes
- `vehicles` - Veículos
- `work_orders` - Ordens de trabalho
- `work_order_parts` - Peças por ordem
- `parts` - Estoque
- `catalog_services` - Serviços disponíveis
- `work_order_services` - Serviços por ordem
- `audit_logs` - Auditoria

**Migrations**: Flyway (V001, V002, ...)

---

## 🚀 Começar a Desenvolver

### 1. Setup Local
```bash
# Clone o repositório
git clone <repo>

# Instale as dependências
gradle build

# Configure o banco de dados
docker-compose up -d postgres

# Execute migrations (automático)
gradle bootRun
```

### 2. Explorar a API
```bash
# Acesse a documentação Swagger
http://localhost:8080/swagger-ui.html

# Ou a especificação OpenAPI
http://localhost:8080/v3/api-docs
```

### 3. Autenticar
```bash
# Obtenha um token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}'

# Use o token nas requisições
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/customers
```

---

## 📝 Convenções de Código

### Naming
- **Controllers**: `{Entity}{RolePrefix}Controller` (ex: `AdminCustomerController`)
- **Services**: `{Entity}ApplicationService` (ex: `CustomerApplicationService`)
- **Repositories**: `{Entity}Repository` (ex: `CustomerRepository`)
- **Entities**: `{Entity}Entity` (ex: `CustomerEntity`)
- **DTOs**: `{Operation}{Entity}DTO` (ex: `CreateCustomerDTO`)

### Estrutura de Pacotes
```
com.soat.tech.challenge.oficina
├── presentation       # Controllers REST
├── application        # Services, DTOs
├── domain             # Models, Interfaces
└── infrastructure     # Repositories, Config
```

---

## 🧪 Testes

### Cobertura Obrigatória
- **Domain**: 80%
- **Application**: 80%

### Executar Testes
```bash
gradle test
gradle jacocoTestReport
```

---

## 📞 Suporte

Dúvidas sobre a arquitetura? Verifique:

1. **Índice do documento**: Use Ctrl+F para buscar
2. **Diagramas Mermaid**: Clique para interagir
3. **Código-fonte**: Sempre a fonte da verdade
4. **Git Blame**: Para entender decisões passadas

---

## ✅ Checklist de Onboarding

- [ ] Li README.md
- [ ] Li C1_system_context.md
- [ ] Entendi os papéis de usuário
- [ ] Li C2_container.md
- [ ] Entendi os componentes principais
- [ ] Li C3_component.md
- [ ] Entendi os controladores
- [ ] Li DATABASE_SCHEMA.md
- [ ] Entendi o schema do banco
- [ ] Li FLOWS.md
- [ ] Entendi os fluxos de negócio
- [ ] Explorei o código-fonte
- [ ] Rodei a aplicação localmente
- [ ] Testei alguns endpoints
- [ ] Estou pronto para contribuir!

---

**Última atualização**: Maio 2026

Bem-vindo ao projeto! 🚀
