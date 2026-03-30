# API Oficina — exemplos com `curl`

Base URL padrão local: `http://localhost:8080`. Troque `BASE` conforme o ambiente (Docker, etc.).

Rotas **admin** exigem header `Authorization: Bearer <token>` obtido no login.

Os corpos JSON usam os nomes de campos expostos pela API (em geral em português), alinhados aos `@JsonProperty` dos DTOs.

Credenciais admin padrão (sobrescrevíveis por `APP_SECURITY_ADMIN_USERNAME` / `APP_SECURITY_ADMIN_PASSWORD`):

- **username:** `admin`
- **password:** `admin`

---

## Variáveis úteis (Bash)

```bash
BASE=http://localhost:8080

TOKEN=$(curl -s -X POST "$BASE/api/public/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r .accessToken)

AUTH="Authorization: Bearer $TOKEN"
```

---

## Actuator

### Health

```bash
curl -s "$BASE/actuator/health"
```

---

## Autenticação (público)

### Login

```bash
curl -s -X POST "$BASE/api/public/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

Resposta: `accessToken`, `tokenType`, `expiresInSeconds`.

---

## Documentação OpenAPI (navegador)

- Swagger UI: `$BASE/swagger-ui.html`
- OpenAPI JSON: `$BASE/v3/api-docs`

---

## Clientes (`/api/admin/clientes`)

### Criar

```bash
curl -s -X POST "$BASE/api/admin/clientes" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
    "documento": "52998224725",
    "nome": "Maria Silva",
    "email": "maria@example.com",
    "telefone": "11999998888"
  }'
```

### Listar

```bash
curl -s "$BASE/api/admin/clientes" -H "$AUTH"
```

### Obter por ID

```bash
curl -s "$BASE/api/admin/clientes/UUID_DO_CLIENTE" -H "$AUTH"
```

### Atualizar

```bash
curl -s -X PUT "$BASE/api/admin/clientes/UUID_DO_CLIENTE" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
    "documento": "52998224725",
    "nome": "Maria Silva Atualizada",
    "email": "maria@example.com",
    "telefone": "11988887777"
  }'
```

### Excluir

```bash
curl -s -o /dev/null -w "%{http_code}\n" -X DELETE "$BASE/api/admin/clientes/UUID_DO_CLIENTE" -H "$AUTH"
```

(HTTP 204 sem corpo.)

---

## Veículos (`/api/admin/veiculos`)

### Criar

```bash
curl -s -X POST "$BASE/api/admin/veiculos" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
    "clienteId": "UUID_DO_CLIENTE",
    "placa": "ABC1D23",
    "marca": "VW",
    "modelo": "Gol",
    "ano": 2020
  }'
```

### Listar

```bash
curl -s "$BASE/api/admin/veiculos" -H "$AUTH"
```

### Obter por ID

```bash
curl -s "$BASE/api/admin/veiculos/UUID_DO_VEICULO" -H "$AUTH"
```

### Atualizar

```bash
curl -s -X PUT "$BASE/api/admin/veiculos/UUID_DO_VEICULO" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
    "clienteId": "UUID_DO_CLIENTE",
    "placa": "ABC1D23",
    "marca": "VW",
    "modelo": "Gol G5",
    "ano": 2012
  }'
```

### Excluir

```bash
curl -s -o /dev/null -w "%{http_code}\n" -X DELETE "$BASE/api/admin/veiculos/UUID_DO_VEICULO" -H "$AUTH"
```

---

## Serviços do catálogo (`/api/admin/servicos-catalogo`)

### Criar

```bash
curl -s -X POST "$BASE/api/admin/servicos-catalogo" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
    "nome": "Troca de oleo",
    "descricao": "Servico exemplo",
    "precoCentavos": 15000,
    "tempoEstimadoMinutos": 45
  }'
```

### Listar

```bash
curl -s "$BASE/api/admin/servicos-catalogo" -H "$AUTH"
```

### Obter por ID

```bash
curl -s "$BASE/api/admin/servicos-catalogo/UUID_DO_SERVICO" -H "$AUTH"
```

### Atualizar

```bash
curl -s -X PUT "$BASE/api/admin/servicos-catalogo/UUID_DO_SERVICO" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
    "nome": "Troca de oleo premium",
    "descricao": "Atualizado",
    "precoCentavos": 18000,
    "tempoEstimadoMinutos": 50
  }'
```

### Excluir

```bash
curl -s -o /dev/null -w "%{http_code}\n" -X DELETE "$BASE/api/admin/servicos-catalogo/UUID_DO_SERVICO" -H "$AUTH"
```

---

## Peças (`/api/admin/pecas`)

### Criar

```bash
curl -s -X POST "$BASE/api/admin/pecas" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
    "codigo": "FILTRO-01",
    "nome": "Filtro oleo",
    "precoCentavos": 3500,
    "quantidadeEstoque": 10
  }'
```

### Listar

```bash
curl -s "$BASE/api/admin/pecas" -H "$AUTH"
```

### Obter por ID

```bash
curl -s "$BASE/api/admin/pecas/UUID_DA_PECA" -H "$AUTH"
```

### Atualizar

```bash
curl -s -X PUT "$BASE/api/admin/pecas/UUID_DA_PECA" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
    "codigo": "FILTRO-01",
    "nome": "Filtro oleo ref.",
    "precoCentavos": 4000,
    "quantidadeEstoque": 20
  }'
```

### Excluir

```bash
curl -s -o /dev/null -w "%{http_code}\n" -X DELETE "$BASE/api/admin/pecas/UUID_DA_PECA" -H "$AUTH"
```

---

## Ordens de serviço — admin (`/api/admin/ordens-servico`)

Substitua `UUID_DO_SERVICO_CATALOGO` e `UUID_DA_PECA` pelos IDs reais (listagens acima).

### Criar

```bash
curl -s -X POST "$BASE/api/admin/ordens-servico" \
  -H "Content-Type: application/json" \
  -H "$AUTH" \
  -d '{
    "documentoCliente": "529.982.247-25",
    "nomeCliente": "Maria Teste",
    "emailCliente": "maria@example.com",
    "telefoneCliente": "11999998888",
    "placa": "ABC1D23",
    "marca": "VW",
    "modelo": "Gol",
    "anoVeiculo": 2020,
    "servicos": [
      { "servicoCatalogoId": "UUID_DO_SERVICO_CATALOGO", "quantidade": 1 }
    ],
    "pecas": [
      { "pecaId": "UUID_DA_PECA", "quantidade": 2 }
    ]
  }'
```

### Listar

```bash
curl -s "$BASE/api/admin/ordens-servico" -H "$AUTH"
```

### Obter por ID

```bash
curl -s "$BASE/api/admin/ordens-servico/UUID_DA_OS" -H "$AUTH"
```

### Iniciar diagnóstico

```bash
curl -s -X POST "$BASE/api/admin/ordens-servico/UUID_DA_OS/iniciar-diagnostico" \
  -H "$AUTH"
```

### Enviar orçamento

```bash
curl -s -X POST "$BASE/api/admin/ordens-servico/UUID_DA_OS/enviar-orcamento" \
  -H "$AUTH"
```

### Aprovar orçamento

```bash
curl -s -X POST "$BASE/api/admin/ordens-servico/UUID_DA_OS/aprovar-orcamento" \
  -H "$AUTH"
```

### Voltar para diagnóstico

(Válido a partir de `AGUARDANDO_APROVACAO`, antes de aprovar.)

```bash
curl -s -X POST "$BASE/api/admin/ordens-servico/UUID_DA_OS/voltar-diagnostico" \
  -H "$AUTH"
```

### Concluir serviços

```bash
curl -s -X POST "$BASE/api/admin/ordens-servico/UUID_DA_OS/concluir-servicos" \
  -H "$AUTH"
```

### Registrar entrega

```bash
curl -s -X POST "$BASE/api/admin/ordens-servico/UUID_DA_OS/registrar-entrega" \
  -H "$AUTH"
```

---

## Acompanhamento público (`/api/public/os`)

Sem autenticação. `documento` deve ser o **mesmo** documento do cliente da OS (aceita CPF/CNPJ; use só dígitos ou formatado conforme parse do domínio). `codigo` é o `codigoAcompanhamento` retornado na criação da OS.

```bash
curl -s -G "$BASE/api/public/os/acompanhar" \
  --data-urlencode "documento=52998224725" \
  --data-urlencode "codigo=CODIGO_ACOMPANHAMENTO"
```

---

## Métricas (`/api/admin/metricas`)

### Tempo médio de execução por serviço

```bash
curl -s "$BASE/api/admin/metricas/tempo-medio-execucao-servicos" -H "$AUTH"
```

---

## Jornada completa (script Bash)

Ordem sugerida: login → catálogo → peça → OS → transições → acompanhamento público → concluir → entregar → métricas.

```bash
#!/usr/bin/env bash
set -euo pipefail
BASE=http://localhost:8080

TOKEN=$(curl -s -X POST "$BASE/api/public/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r .accessToken)
AUTH="Authorization: Bearer $TOKEN"

SERVICO_ID=$(curl -s -X POST "$BASE/api/admin/servicos-catalogo" \
  -H "Content-Type: application/json" -H "$AUTH" \
  -d '{"nome":"Troca de oleo","descricao":"Exemplo","precoCentavos":15000,"tempoEstimadoMinutos":45}' \
  | jq -r .id)

PECA_ID=$(curl -s -X POST "$BASE/api/admin/pecas" \
  -H "Content-Type: application/json" -H "$AUTH" \
  -d '{"codigo":"FILTRO-01","nome":"Filtro oleo","precoCentavos":3500,"quantidadeEstoque":10}' \
  | jq -r .id)

OS=$(curl -s -X POST "$BASE/api/admin/ordens-servico" \
  -H "Content-Type: application/json" -H "$AUTH" \
  -d "{
    \"documentoCliente\": \"529.982.247-25\",
    \"nomeCliente\": \"Maria Teste\",
    \"placa\": \"ABC1D23\",
    \"marca\": \"VW\",
    \"modelo\": \"Gol\",
    \"anoVeiculo\": 2020,
    \"servicos\": [{\"servicoCatalogoId\": \"$SERVICO_ID\", \"quantidade\": 1}],
    \"pecas\": [{\"pecaId\": \"$PECA_ID\", \"quantidade\": 2}]
  }")
OS_ID=$(echo "$OS" | jq -r .id)
CODIGO=$(echo "$OS" | jq -r '."codigoAcompanhamento"')

for path in iniciar-diagnostico enviar-orcamento aprovar-orcamento; do
  curl -s -X POST "$BASE/api/admin/ordens-servico/$OS_ID/$path" -H "$AUTH" | jq -r .status
done

curl -s -G "$BASE/api/public/os/acompanhar" \
  --data-urlencode "documento=52998224725" \
  --data-urlencode "codigo=$CODIGO" | jq .

for path in concluir-servicos registrar-entrega; do
  curl -s -X POST "$BASE/api/admin/ordens-servico/$OS_ID/$path" -H "$AUTH" | jq -r .status
done

curl -s "$BASE/api/admin/metricas/tempo-medio-execucao-servicos" -H "$AUTH" | jq .
```

---

## Notas

- **Windows PowerShell:** use `curl.exe` ou `Invoke-RestMethod`; para JSON, prefira aspas simples externas e escapar internamente, ou gravar o body em ficheiro `-d @payload.json`.
- **CPF/CNPJ:** o domínio valida dígitos; exemplos de CPF válido: `52998224725`.
- **Placa:** formatos aceites incluem legado `ABC1234` e Mercosul `ABC1D23`.
