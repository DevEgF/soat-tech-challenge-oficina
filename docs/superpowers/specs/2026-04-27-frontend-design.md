# Frontend Design — Sistema de Oficina Mecânica

**Data:** 2026-04-27  
**Escopo:** Criação da pasta `frontend/` na raiz do repositório com aplicação React + Vite consumindo a API REST já existente (Spring Boot / Kotlin).

---

## 1. Visão Geral

Single-page application que expõe dois domínios de acesso dentro do mesmo projeto Vite:

- **Público** (`/public/*`) — sem autenticação; portal do cliente para acompanhar OS e aprovar/reprovar orçamento.
- **Interno** (`/internal/*`) — protegido por JWT; sistema operacional para as equipes da oficina (Admin, Atendente, Técnico, Almoxarife).

A API backend já está pronta e rodando em `http://localhost:8080` (configurável via variável de ambiente `VITE_API_BASE_URL`).

---

## 2. Stack

| Camada | Biblioteca |
|---|---|
| Build | Vite 5 + React 18 + TypeScript |
| Roteamento | React Router v6 |
| Estado de servidor | TanStack Query v5 |
| Estado global (auth) | Zustand |
| Componentes UI | shadcn/ui + Tailwind CSS |
| Formulários | react-hook-form + zod |
| HTTP | Axios |

---

## 3. Tema Visual — Oficina Brand

Cores Tailwind customizadas:

- **Primária:** `orange-600` (#ea580c) — botões, badges ativos, indicador de sidebar
- **Superfície:** `orange-50` (#fff7ed) — fundo de páginas e cards
- **Borda:** `orange-200` (#fed7aa)
- **Sidebar:** `stone-900` (#1c1917)
- **Texto primário:** `stone-900` (#1c1917)
- **Texto secundário:** `stone-500` (#78716c)

---

## 4. Estrutura de Pastas

```
frontend/
├── src/
│   ├── api/
│   │   ├── client.ts             # axios instance + interceptor JWT + 401 redirect
│   │   ├── auth.ts               # login
│   │   ├── workOrders.ts         # queries e mutations de OS
│   │   ├── customers.ts
│   │   ├── vehicles.ts
│   │   ├── parts.ts
│   │   ├── catalogServices.ts
│   │   └── warehouse.ts
│   ├── auth/
│   │   ├── store.ts              # Zustand: token, username, scope, logout
│   │   └── ProtectedRoute.tsx    # redirect /login se sem token
│   ├── components/
│   │   ├── AppLayout.tsx         # sidebar expandida + outlet
│   │   ├── StatusBadge.tsx       # badge colorido por WorkOrderStatus
│   │   ├── ConfirmDialog.tsx     # diálogo de confirmação reutilizável
│   │   └── ErrorBoundary.tsx
│   ├── pages/
│   │   ├── public/
│   │   │   ├── TrackOrderPage.tsx     # /public/acompanhar
│   │   │   └── QuoteResultPage.tsx    # /public/resultado
│   │   └── internal/
│   │       ├── LoginPage.tsx
│   │       ├── WorkOrdersPage.tsx         # listagem com filtro de status
│   │       ├── WorkOrderDetailPage.tsx    # detalhe + ações contextuais
│   │       ├── CustomersPage.tsx
│   │       ├── VehiclesPage.tsx
│   │       ├── PartsPage.tsx
│   │       ├── CatalogServicesPage.tsx
│   │       ├── WarehousePage.tsx
│   │       └── MetricsPage.tsx
│   ├── lib/
│   │   ├── utils.ts       # shadcn cn(), formatCurrency, formatStatus
│   │   └── constants.ts   # mapa de status → label/cor
│   └── router.tsx
├── index.html
├── vite.config.ts
├── tailwind.config.ts
└── package.json
```

---

## 5. Roteamento

```
/                          → redirect /public/acompanhar
/login                     → LoginPage (sem guard)
/public/acompanhar         → TrackOrderPage (sem guard)
/public/resultado          → QuoteResultPage (sem guard)
/internal                  → ProtectedRoute > AppLayout
  /internal/ordens-servico              → WorkOrdersPage
  /internal/ordens-servico/:id         → WorkOrderDetailPage
  /internal/clientes                   → CustomersPage
  /internal/veiculos                   → VehiclesPage
  /internal/pecas                      → PartsPage
  /internal/servicos-catalogo          → CatalogServicesPage
  /internal/almoxarifado               → WarehousePage
  /internal/metricas                   → MetricsPage
```

`ProtectedRoute` lê o token do Zustand store; se ausente, redireciona para `/login` preservando `?from=` para redirect pós-login.

---

## 6. Autenticação

- `POST /api/public/auth/login` com `{ username, password }` retorna `{ accessToken, expiresInSeconds }`.
- Token armazenado no Zustand (persistido em `localStorage` via `zustand/middleware/persist`).
- Axios interceptor injeta `Authorization: Bearer <token>` em toda requisição para `/api/admin/*`, `/api/attendant/*`, `/api/technician/*`, `/api/warehouse/*`.
- Interceptor de resposta: 401 → limpa store + redirect `/login`; 403 → toast "Sem permissão".

**Usuários/perfis do backend:**

| Username | Senha | Scope |
|---|---|---|
| admin | admin | SCOPE_ADMIN |
| atendente | atendente | SCOPE_ATTENDANT |
| tecnico | tecnico | SCOPE_TECHNICIAN |
| almoxarife | almoxarife | SCOPE_WAREHOUSE |

---

## 7. Telas e Funcionalidades

### 7.1 Portal Público — `/public/acompanhar`

- Formulário: CPF/CNPJ + código de rastreio.
- Chama `GET /api/public/os/acompanhar?documento=&codigo=`.
- Exibe: placa, status, total formatado em R$.
- Se status = `AWAITING_CLIENT_APPROVAL`: mostra botões **Aprovar** e **Reprovar** que chamam os endpoints correspondentes e redirecionam para `/public/resultado`.

### 7.2 Login — `/login`

- Formulário username + senha com validação zod.
- Chama `POST /api/public/auth/login`, persiste token no Zustand, redireciona para `/internal/ordens-servico`.

### 7.3 Listagem de OS — `/internal/ordens-servico`

- Tabela com colunas: código, status (badge), veículo, cliente, total, link "Ver".
- Filtro por status (select múltiplo).
- Botão "Nova OS" (visível para `SCOPE_ATTENDANT` e `SCOPE_ADMIN`) abre drawer com formulário de criação.
- Formulário de criação: dados do cliente (CPF/CNPJ, nome, email, telefone), dados do veículo (placa, marca, modelo, ano), seleção de serviços do catálogo (multiselect com quantidade).
- Chama `POST /api/attendant/ordens-servico`.

### 7.4 Detalhe da OS — `/internal/ordens-servico/:id`

- Cards com info de cliente e veículo.
- Abas: **Serviços** / **Peças** / **Progresso** (stepper visual mostrando todos os status do ciclo de vida com o status atual destacado — derivado do campo `status` da OS, sem endpoint de histórico no backend).
- Totais breakdown (serviços + peças + total).
- **Painel de ações contextuais** — renderizado condicionalmente por `scope` + `status`:

| Status | ATTENDANT | TECHNICIAN | ADMIN | WAREHOUSE |
|---|---|---|---|---|
| RECEIVED | — | Iniciar Diagnóstico | — | — |
| IN_DIAGNOSIS | — | Submeter Plano | — | — |
| AWAITING_INTERNAL_APPROVAL | — | — | Aprovar / Reprovar | — |
| AWAITING_CLIENT_APPROVAL | Enviar Orçamento / Voltar Diagnóstico | — | — | — |
| AWAITING_PARTS_RELEASE | — | — | — | (na página Almoxarifado) |
| IN_EXECUTION | — | Concluir Serviços | — | — |
| FINALIZED | Registrar Entrega | — | — | — |
| DELIVERED / CANCELED | — | — | — | — |

### 7.5 CRUD Clientes — `/internal/clientes`

- Tabela: nome, CPF/CNPJ, email, telefone.
- Modal criar/editar com validação de CPF/CNPJ.
- Botão deletar com confirmação.
- APIs: `GET/POST/PUT/DELETE /api/admin/clientes`.

### 7.6 CRUD Veículos — `/internal/veiculos`

- Tabela: placa, marca, modelo, ano, cliente (nome).
- Modal criar/editar com seleção de cliente existente.
- APIs: `GET/POST/PUT/DELETE /api/admin/veiculos`.

### 7.7 CRUD Peças — `/internal/pecas`

- Tabela: código, nome, preço, estoque, ponto de reposição.
- Modal criar/editar.
- Botão "Registrar Entrada" por peça → modal com quantidade + referência.
- Seção de alertas de estoque baixo (lista peças abaixo do ponto de reposição).
- APIs: `GET/POST/PUT/DELETE /api/admin/pecas` + `POST /{id}/entrada-mercadoria`.

### 7.8 CRUD Serviços do Catálogo — `/internal/servicos-catalogo`

- Tabela: nome, descrição, preço, tempo estimado.
- Modal criar/editar.
- APIs: `GET/POST/PUT/DELETE /api/admin/servicos-catalogo`.

### 7.9 Almoxarifado — `/internal/almoxarifado`

- **Limitação do backend:** não existe endpoint para listar todas as OS pelo scope WAREHOUSE. A página expõe um campo de busca por ID da OS (UUID); o atendente informa o ID verbalmente ou por nota interna.
- Após inserir o ID, exibe a lista de peças reservadas + quantidade (`GET /api/warehouse/ordens-servico/{id}/reservas-pendentes`).
- Botão "Confirmar Saída" por OS → `POST /api/warehouse/ordens-servico/{id}/confirmar-saida`.
- Seção inferior: alertas de estoque baixo (`GET /api/warehouse/alertas-estoque-baixo`).

### 7.10 Métricas — `/internal/metricas`

- Tabela + gráfico de barras horizontais com tempo médio de execução por serviço.
- API: `GET /api/admin/metricas/tempo-medio-execucao-servicos`.
- Gráfico usando a biblioteca `recharts`.

---

## 8. Tratamento de Erros

- **401** → logout automático + redirect `/login`
- **403** → toast "Você não tem permissão para essa ação"
- **400/422** → mensagem inline no campo do formulário (via `setError` do react-hook-form)
- **500 / falha de rede** → toast "Erro ao processar. Tente novamente."
- `<ErrorBoundary>` na raiz para erros inesperados de renderização

---

## 9. Sidebar — Menu e Perfis

Menu lateral único para todos os perfis (decisão do usuário). Ações dentro das telas são condicionais pelo `scope`. Itens do menu:

1. Ordens de Serviço
2. Clientes
3. Veículos
4. Peças
5. Serviços Catálogo
6. Almoxarifado
7. Métricas

Footer da sidebar: nome do usuário logado + scope + link "Sair".

---

## 10. Fora do Escopo

- Testes unitários/integração do frontend (MVP)
- Internacionalização (i18n)
- Modo offline / PWA
- Notificações em tempo real (WebSocket)
- Paginação server-side nas tabelas (todas carregam a lista completa)
