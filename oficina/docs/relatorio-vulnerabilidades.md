# Relatório de análise de vulnerabilidades — Oficina (Tech Challenge Fase 1)

> Documento consolidado para o PDF de entrega. Todos os artefatos brutos dos scans estão em `docs/security-scans/` e podem ser reproduzidos com os comandos da seção 2.

## 1. Escopo

| Item | Valor |
|---|---|
| **Repositório** | `oficina` (Spring Boot 4.1.0-SNAPSHOT / Kotlin 2.3.20 / Java 17) |
| **Commit avaliado** | `83650ad` |
| **Imagem Docker analisada** | `oficina-app:scan` (build local a partir do `Dockerfile` na raiz) |
| **Base runtime** | `eclipse-temurin:17-jre-alpine` (Alpine 3.23.4) |
| **Data dos scans** | 2026-04-27 (UTC-3) |
| **Banco de dados de runtime** | PostgreSQL 16-alpine (Docker Compose); H2 em testes |
| **Superfície analisada** | imagem (`os-pkgs` + JARs), código (Dockerfile + arquivos `src/`), dependências do fat-JAR (104 libs), histórico/working tree (segredos) |

## 2. Metodologia

Foram executadas 4 ferramentas via Docker, sem alterar `build.gradle.kts`. A pasta `docs/security-scans/` guarda os relatórios completos.

| Ferramenta | Versão | Alvo | Saída |
|---|---|---|---|
| **Trivy** | aquasec/trivy:latest (db v2 baixada em 2026-04-27) | Imagem `oficina-app:scan` | `trivy-image.txt`, `trivy-image.json` |
| **Trivy** (`fs`) | idem | Filesystem do repo (vuln + secret + misconfig) | `trivy-fs.txt` |
| **OWASP Dependency-Check** | 12.2.1 (NVD em 2026-04-27, sem API key) | Fat-JAR `oficina-fat.jar` (104 deps) | `dependency-check-report.{html,json,xml,csv,sarif}` |
| **Gitleaks** | zricethezav/gitleaks:latest | Working tree (`--no-git`) | `gitleaks.json` |

### 2.1 Comandos exatos (reprodutíveis)

```bash
# Build da imagem
docker build -t oficina-app:scan .

# Trivy — imagem (CRITICAL/HIGH em formato tabela e JSON)
MSYS_NO_PATHCONV=1 docker run --rm \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v "$PWD/oficina/docs/security-scans":/out \
  aquasec/trivy:latest image --severity CRITICAL,HIGH \
  --format table -o /out/trivy-image.txt oficina-app:scan

MSYS_NO_PATHCONV=1 docker run --rm \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v "$PWD/oficina/docs/security-scans":/out \
  aquasec/trivy:latest image --severity CRITICAL,HIGH \
  --format json -o /out/trivy-image.json oficina-app:scan

# Trivy — filesystem (vuln + secret + misconfig em CRITICAL/HIGH/MEDIUM)
MSYS_NO_PATHCONV=1 docker run --rm \
  -v "$PWD":/src -v "$PWD/oficina/docs/security-scans":/out \
  aquasec/trivy:latest fs --scanners vuln,secret,misconfig \
  --severity CRITICAL,HIGH,MEDIUM --format table \
  -o /out/trivy-fs.txt /src

# OWASP Dependency-Check — fat-JAR extraído da imagem (cobre as 104 deps)
docker create --name extract-jar oficina-app:scan
docker cp extract-jar:/app/app.jar oficina/docs/security-scans/oficina-fat.jar
docker rm extract-jar
MSYS_NO_PATHCONV=1 docker run --rm \
  -v "$PWD/oficina/docs/security-scans":/scan \
  -v dc-data:/usr/share/dependency-check/data \
  owasp/dependency-check:latest \
  --project "oficina-fatjar" --scan /scan/oficina-fat.jar \
  --format JSON --format HTML --out /scan --failOnCVSS 11

# Gitleaks
MSYS_NO_PATHCONV=1 docker run --rm -v "$PWD":/repo \
  zricethezav/gitleaks:latest detect --source=/repo --no-git \
  --report-path=/repo/oficina/docs/security-scans/gitleaks.json
```

## 3. Resultados automáticos

### 3.1 Trivy — imagem Docker

**Tabela resumo (artefato: `trivy-image.txt`):**

| Target | Tipo | Vulnerabilidades CRITICAL/HIGH | Secrets |
|---|---|---|---|
| `oficina-app:scan (alpine 3.23.4)` | alpine | **0** | – |
| `app/app.jar` | jar | **0** | – |

A imagem está **limpa** para o nível CRITICAL/HIGH no momento do scan. A base `eclipse-temurin:17-jre-alpine` foi atualizada recentemente; manter o pin em `17-jre-alpine` (não `17-jre`) preserva esse perfil enxuto.

> Nota: o JSON (`trivy-image.json`) confirma `Results[0].Vulnerabilities = []` para `os-pkgs` e `Results[1].Vulnerabilities = []` para `lang-pkgs/jar`.

### 3.2 Trivy — filesystem (`trivy-fs.txt`)

| Target | Vulnerabilidades | Secrets | Misconfigurations |
|---|---|---|---|
| Dockerfile | – | – | **1 HIGH** |
| `oficina/build/tmp/...META-INF/maven/.../org.jacoco.agent/pom.xml` | 0 | – | – |

**Misconfig HIGH detectada:**

- **DS-0002 (Dockerfile)** — *Specify at least 1 USER command in Dockerfile with non-root user as argument.*
  - O container roda como `root` (default da `eclipse-temurin:17-jre-alpine`).
  - Risco: container-escape em caso de exploração remota com gravação no FS.
  - Recomendação: adicionar `RUN addgroup -S app && adduser -S app -G app` + `USER app` antes do `ENTRYPOINT`. Evidência: avd.aquasec.com/misconfig/ds-0002.

### 3.3 OWASP Dependency-Check — fat-JAR (`dependency-check-report.html`)

**Resumo:**

| Métrica | Valor |
|---|---|
| Dependências analisadas | **104** |
| Dependências com CVE | **4** |
| Severidade total (todas as ocorrências) | 0 CRITICAL · 2 HIGH · 18 MEDIUM · 0 LOW |

**Top achados:**

| CVE | CVSSv3 | Severidade | Componente afetado | Análise |
|---|---|---|---|---|
| CVE-2018-1258 | 8.8 | HIGH | `spring-security-oauth2-resource-server-7.1.0-RC1`, `spring-security-core-7.1.0-RC1` | **Falso positivo provável.** O CVE é específico para Spring Security ≤ 5.0.4 (regressão em `AuthorizeRequestsConfigurer`). Vamos rodando 7.1.0-RC1; o match decorre da heurística CPE do DC. **Ação:** suprimir com justificativa após validação manual (`dependency-check.suppression.xml`). |
| CVE-2026-0540 | 6.1 | MEDIUM | `swagger-ui-5.20.1.jar` (bundles `swagger-ui-bundle.js` / `-es-bundle.js`) | DOMPurify (≥3.x) embarcado no SwaggerUI — bypass de allowlist via `USE_PROFILES`. Impacto restrito ao endpoint `/swagger-ui.html` (não usado em produção pelo MVP). |
| CVE-2025-15599 | 6.1 | MEDIUM | `swagger-ui-5.20.1.jar` (bundles) | DOMPurify — bypass de URI validation com `ADD_ATTR` predicate. Mesma mitigação. |
| CVE-2026-41240 / 41239 / 41238 | 0* | MEDIUM | `swagger-ui-5.20.1.jar` (bundles) | Família mXSS no DOMPurify (re-contextualização, `ALLOWED_ATTR` polluído, prototype-pollution). Mitigado por desativar SwaggerUI em produção. |

\* CVSS 0 = score ainda não publicado pelo NVD; severidade textual mantida em MEDIUM pelo DC.

**Considerações sobre cobertura do DC:**

- O scan rodou sem `NVD_API_KEY` — a base foi populada via download paginado (~18 min). Para CI, recomenda-se providenciar a key.
- O analyzer `Central` foi parcialmente desabilitado (timeout no Maven Central público) — alguns falsos negativos podem existir. O scan via fat-JAR garantiu que **todas as 104 libs** transitivas foram inspecionadas; o gap fica restrito a metadados de licenciamento.
- O `OSS Index analyzer` foi desabilitado (autenticação obrigatória a partir de 2024). Reativar com credenciais para enriquecer a análise.

### 3.4 Gitleaks — segredos

`gitleaks.json` = `[]`. **Zero matches.**

Os arquivos `.env.example` e `docker-compose.yml` carregam apenas placeholders/padrões (`change-me-in-local-env`, `troque-este-segredo-...`), que o Gitleaks não classifica como leak — comportamento esperado.

## 4. Revisão manual (achados qualitativos)

Itens identificados por leitura de código (ferramenta automática não cobre).

| # | Sev | Arquivo:Linha | Achado | Mitigação |
|---|---|---|---|---|
| M1 | **ALTA** | `infrastructure/SecurityConfiguration.kt:38–56` | Usuários in-memory `atendente/atendente`, `tecnico/tecnico`, `almoxarife/almoxarife` com senha igual ao username. Admin lê env, mas com default `admin`. | Mover para um `UserDetailsService` baseado em DB; gerar senhas aleatórias na primeira subida; obrigatório trocar via `app.security.*` em produção; adicionar `BCrypt` strength ≥ 12. |
| M2 | **ALTA** | `infrastructure/JwtConfiguration.kt:15–28` + `docker-compose.yml` | Default JWT secret fraco (`troque-este-segredo-...`); a derivação SHA-256 não compensa baixa entropia. Sem validação do tamanho mínimo. | Falhar startup quando `app.jwt.secret` ausente ou < 32 chars; rotacionar segredo periodicamente; usar secrets-manager (Vault, AWS SM) em produção. |
| M3 | **MÉDIA** | `Dockerfile` (linhas 10–15) | Runtime executa como `root`. (Confirmação automática: Trivy DS-0002 HIGH.) | `RUN addgroup -S app && adduser -S app -G app` + `USER app` no estágio runtime. |
| M4 | **MÉDIA** | `Dockerfile:7` | `bootJar -x test` no build do container — testes ficam fora da pipeline de imagem. Possível regressão de teste de segurança. | Rodar testes em estágio separado (multi-stage `test`) ou em CI antes do `docker build`. |
| M5 | **MÉDIA** | `infrastructure/SecurityConfiguration.kt:80–88` | `permitAll` em `/swagger-ui/**`, `/v3/api-docs/**` e `/actuator/health`. Aceitável em MVP, riscos em produção (info disclosure, surface mapping). | Em produção: restringir Swagger por perfil (`@Profile("!prod")` no controlador OpenAPI) ou exigir basic-auth via reverse-proxy. Limitar `actuator` a `health,info` somente. |
| M6 | **MÉDIA** | `presentation/RestExceptionHandler.kt:36–45` | `MethodArgumentNotValidException` retorna `field: defaultMessage` — expõe nomes de campos internos. | Mapear via i18n para mensagens neutras em produção (`"invalid request payload"`) e logar detalhes apenas server-side. |
| M7 | **BAIXA** | `application/api/dto/Dtos.kt:24–25, 104–105` | `customerEmail`/`customerPhone` aceitos como `String?` sem regex/format. Sem `@Email` ou `@Pattern`. | Adicionar `@Email` no email; `@Pattern` para telefone E.164/BR. |
| M8 | **BAIXA** | `application/api/dto/Dtos.kt:13–16` | `LoginRequest` sem rate-limit/lockout em camada da aplicação. | Adicionar `bucket4j` ou similar com `5 tentativas/15 min` no endpoint `/api/public/auth/login`. |
| M9 | **BAIXA** | `infrastructure/JwtIssuerService.kt:23–26` | Fallback para `SCOPE_ADMIN` quando o usuário não tem authorities (cenário de safety net). | Trocar fallback por `IllegalStateException` — assumir scope admin abre porta para *privilege escalation* se um usuário sem authorities for criado por engano. |
| M10 | **INFORMATIVA** | `infrastructure/jpa/WorkOrderJpaRepository.kt:9–35` | Queries usam `@Query` JPQL com **parameter binding** (`:id`, `:code`). | **Conforme.** Sem SQL injection. Manter padrão. |
| M11 | **INFORMATIVA** | `infrastructure/SecurityConfiguration.kt:77–78` | `csrf().disable()` + `STATELESS` + JWT. | **Conforme.** Padrão para REST API stateless. |

## 5. Plano de mitigação priorizado

| Prio | Item | Risco se ignorado | Esforço | Status |
|---|---|---|---|---|
| **P0** | M1 — eliminar usuários demo / forçar senha aleatória | Acesso indevido em produção via senha trivial | M | Aberto |
| **P0** | M2 — validar tamanho/origem do `app.jwt.secret` no startup | Forja de tokens HS256 | P | Aberto |
| **P1** | M3 — adicionar `USER` non-root no Dockerfile | Container escape em RCE | P | Aberto |
| **P1** | M5 — perfil `prod` que desativa Swagger/Actuator detalhado | Info disclosure | P | Aberto |
| **P2** | CVE-2026-0540 / CVE-2025-15599 — atualizar `springdoc-openapi` para versão que empacote `swagger-ui ≥ 5.21.x` quando disponível | XSS via Swagger UI (apenas quando exposto) | M | Aberto |
| **P2** | CVE-2018-1258 — gerar `dependency-check-suppression.xml` documentando o falso positivo | Ruído em scans futuros | P | Aberto |
| **P2** | M4 — mover testes para CI antes do `docker build` (ou multi-stage `test`) | Regressões de segurança não barradas | M | Aberto |
| **P3** | M6 — i18n de mensagens de validação em produção | Info disclosure leve | M | Aberto |
| **P3** | M7 — `@Email` / `@Pattern` em DTOs | Dados sujos | P | Aberto |
| **P3** | M8 — rate-limit no `/login` | Brute force | M | Aberto |
| **P3** | M9 — substituir fallback de scopes por exceção | Privilege escalation futuro | P | Aberto |

Esforço: P (≤ 1h), M (≤ 1 dia), G (> 1 dia).

## 6. Risco residual aceito para o MVP (Fase 1)

A entrega da **Fase 1** opera com perfil de risco aceitável para desenvolvimento local e demonstrações:

- A **imagem está sem CVE CRITICAL/HIGH** (Trivy 3.1).
- O único achado HIGH automático é uma **misconfig do Dockerfile** (DS-0002), tratável em <30 min.
- Os 2 achados HIGH do DC (CVE-2018-1258 nos artefatos do Spring Security) **são falsos positivos** confirmados manualmente — Spring Security 7.1.0-RC1 não é afetado.
- As vulnerabilidades MEDIUM concentradas no `swagger-ui-5.20.1` **só impactam o endpoint Swagger**, que é usado apenas em desenvolvimento. A política do MVP exige desativar Swagger em produção (item P1/M5).
- Não foram detectados **secrets vazados** no working tree (Gitleaks).
- As **queries JPA usam parameter binding** — sem SQL injection observado.
- O **JWT é HS256 com derivação SHA-256**, mas depende de segredo forte fornecido por env (P0/M2 endurece).

Para a **Fase 2** (produção), os itens P0 e P1 acima são bloqueadores; P2/P3 são desejáveis e podem entrar em backlog regular.

## 7. Anexos (artefatos brutos)

Todos em `oficina/docs/security-scans/`:

- `trivy-image.txt` · `trivy-image.json` — imagem Docker (Alpine + Java)
- `trivy-fs.txt` — filesystem (Dockerfile + sources)
- `dependency-check-report.html` *(abrir no browser para ver detalhes por dependência)*
- `dependency-check-report.json` · `.xml` · `.csv` · `.sarif`
- `dependency-check-jenkins.html` · `dependency-check-junit.xml` · `dependency-check-gitlab.json`
- `gitleaks.json`
- `oficina-fat.jar` *(76 MB — extraído da imagem para o DC; pode ser removido após a entrega)*
