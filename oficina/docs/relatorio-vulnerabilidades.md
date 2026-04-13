# Relatório de vulnerabilidades (modelo — Tech Challenge)

Este documento serve de base para o PDF de entrega. Preencha as seções **Resultados** com a saída real dos scans após executá-los no seu ambiente.

## 1. Escopo

- **Código:** repositório `oficina` (Kotlin / Spring Boot).
- **Imagem de runtime:** `eclipse-temurin:17-jre-alpine` (vide [Dockerfile](../../Dockerfile)).
- **Dependências:** Maven Central / Gradle (BOM Spring Boot).

## 2. Ferramentas sugeridas

| Ferramenta        | Alvo                         | Comando exemplo |
| ----------------- | ---------------------------- | ----------------- |
| **Trivy**         | Imagem Docker                | `docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image --severity HIGH,CRITICAL oficina-app:local` (após `docker build -t oficina-app:local .` na raiz do repo) |
| **OWASP Dependency-Check** | Dependências Gradle   | `.\gradlew.bat dependencyCheckAnalyze` (se o plugin for adicionado) ou CLI standalone apontando para `oficina/build.gradle.kts` |
| **SonarQube / SonarCloud** | Código + deps          | Projeto configurado no servidor Sonar |

## 3. Resultados (preencher)

### 3.1 Scan de imagem (Trivy)

- **Data:**
- **Versão da imagem:**
- **Vulnerabilidades CRITICAL / HIGH:** (colar resumo ou anexar CSV)
- **Análise:** distinguir CVEs com patch disponível vs. baseline aceita para o MVP; planejar atualização de tag base (`eclipse-temurin`) em ciclo posterior.

### 3.2 Dependências (Dependency-Check ou equivalente)

- **Data:**
- **Supressões / falsos positivos:** (listar IDs e justificativa curta)
- **Ações:** atualizar versão de biblioteca / substituir artefato, quando aplicável.

### 3.3 Observações de configuração

- **Segredos:** `APP_JWT_SECRET`, `APP_SECURITY_ADMIN_PASSWORD` e senha do Postgres não devem usar valores padrão em produção.
- **Swagger:** exposto em `/swagger-ui.html` no MVP; em produção, restringir por rede, autenticação ou desativar o perfil.
- **H2 / Flyway:** desenvolvimento local com H2 e testes sem Flyway; Docker usa PostgreSQL + Flyway (`db/migration`).

## 4. Conclusão

Resumo em 3–5 frases sobre risco residual aceito para a fase 1 e próximos passos (atualização de imagem, pipeline CI com scan obrigatório, etc.).
