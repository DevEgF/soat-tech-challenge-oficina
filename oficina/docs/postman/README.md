# Postman — Oficina Tech Challenge

Coleção Postman cobrindo toda a API do MVP (Auth, CRUDs administrativos, fluxo da OS por papel e o fluxo completo do event storm com asserções automáticas de status).

## Arquivos

- `Oficina.postman_collection.json` — coleção principal (Postman v2.1).
- `Oficina-Local.postman_environment.json` — environment apontando para `http://localhost:8080` com os usuários in-memory padrão.

## Importar

1. Postman → **Import** → selecione os dois arquivos.
2. No canto superior direito, selecione o environment **Oficina - Local**.
3. Suba a aplicação local (`./gradlew bootRun` ou `docker compose up`).

## Usuários padrão

| Login | Senha | Scope |
|---|---|---|
| `master` | `master` | `SCOPE_MASTER`, `SCOPE_ADMIN`, `SCOPE_ATTENDANT`, `SCOPE_TECHNICIAN`, `SCOPE_WAREHOUSE` |
| `admin` | `admin` | `SCOPE_ADMIN` |
| `atendente` | `atendente` | `SCOPE_ATTENDANT` |
| `tecnico` | `tecnico` | `SCOPE_TECHNICIAN` |
| `almoxarife` | `almoxarife` | `SCOPE_WAREHOUSE` |

O `master` é o atalho para testar tudo com um login só (tem todos os scopes).

A senha do `admin` pode ser sobrescrita via `app.security.admin.password` (env var no `docker-compose.yml` — por padrão o `.env` do projeto define `change-me-in-local-env`; se você subir via Docker com esse `.env`, ajuste `adminPassword` no environment do Postman para o mesmo valor).

## Como usar

### Caminho rápido (Runner)

1. Abra a pasta **Fluxo completo (Event Storm)** na coleção.
2. Clique em **Run** (Postman Runner) → **Run Oficina - Tech Challenge**.
3. Os 16 requests rodam em sequência, populando variáveis (`workOrderId`, `trackingCode`, `customerId`, `partId`, `catalogServiceId`, tokens) e fazendo asserções de transição de status:
   - `RECEIVED → IN_DIAGNOSIS → PENDING_INTERNAL_APPROVAL → PENDING_APPROVAL → AWAITING_PARTS_RELEASE → IN_EXECUTION → FINALIZED → DELIVERED`.

### Uso manual

1. **Auth → Login admin/atendente/tecnico/almoxarife** — popula automaticamente `adminToken`, `attendantToken`, etc.
2. Use as pastas por papel; cada uma já está configurada com o Bearer token correspondente.
3. As pastas escrevem variáveis (`customerId`, `vehicleId`, `workOrderId`, `trackingCode`, etc.) que os endpoints subsequentes consomem.

## Observação sobre o fluxo

O cliente aprovar o orçamento **não** dispara `IN_EXECUTION` diretamente — o status passa por `AWAITING_PARTS_RELEASE` e só vira `IN_EXECUTION` quando o almoxarife confirma a saída de peças (transição automática via política do event storm). A coleção expressa isso no passo 12 (espera `AWAITING_PARTS_RELEASE`) e no passo 14 (espera `IN_EXECUTION` após o passo 13 do almoxarife).
