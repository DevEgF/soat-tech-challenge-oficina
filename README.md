# Oficina

Monólito **Kotlin** com **Spring Boot** (JPA, Spring Security com suporte a OAuth2 Resource Server, sessão JDBC), organizado em **arquitetura em camadas**.

## Stack

- Kotlin 2.3, Java 17, Gradle
- Spring Boot (web, data-jpa, security, oauth2-resource-server, session-jdbc, actuator)
- Banco local: H2 em memória (`application.properties`)
- Banco em Docker: PostgreSQL (`docker` + `application-docker.properties`)

## Estrutura de camadas

O código fica sob o pacote `com.soat.tech.challenge.oficina`:

| Camada (requisito) | Pacote no código | Papel |
| ------------------ | ----------------- | ----- |
| **Domain** | `...domain` | Entidades de negócio, value objects, exceções de domínio e portas (interfaces de repositório ou integrações). |
| **Application** | `...application` | Casos de uso, orquestração e DTOs de aplicação. |
| **Infrastructure** | `...infrastructure` | Implementações técnicas (JPA, adapters, configuração como segurança). |
| **Interface** | `...presentation` | API HTTP (controllers REST, validação de entrada). O pacote chama-se `presentation` porque **`interface` é palavra reservada** em Kotlin/Java e não pode ser usada como nome de pacote. |

Dependência desejada entre camadas: a **Interface** chama a **Application**; a **Application** usa o **Domain** e as portas; a **Infrastructure** implementa essas portas e detalhes de persistência.

## Escolha do banco de dados

Foi adotado **PostgreSQL** (banco **relacional**) para o ambiente executado via Docker Compose.

**Justificativa técnica:** o projeto já utiliza **Spring Data JPA** e **Hibernate**, ou seja, um modelo de dados orientado a tabelas, relacionamentos e transações **ACID**. O domínio típico de uma oficina (clientes, veículos, ordens de serviço, itens, estoque) mapeia naturalmente para entidades relacionadas com integridade referencial, consultas SQL e relatórios. Além disso, o **Spring Session JDBC** armazena sessões em tabelas relacionais, alinhando-se ao mesmo tipo de armazenamento. PostgreSQL é maduro, amplamente usado em produção, gratuito, com bom suporte no ecossistema Spring e adequado a um monólito que pode evoluir com índices, constraints e migrações versionadas (recomendado além do `ddl-auto=update` usado neste MVP).

Para desenvolvimento rápido **fora** do Docker, o perfil padrão continua com **H2** em memória, sem necessidade de instalar PostgreSQL localmente.

## Como executar

### Local (Gradle)

Na pasta [`oficina/`](oficina/):

```bash
./gradlew bootRun
```

A aplicação sobe em `http://localhost:8080`. O console H2 (perfil padrão) fica em `/h2-console` quando habilitado.

### Docker Compose (aplicação + PostgreSQL)

Na **raiz** do repositório:

```bash
docker compose up --build
```

- API: `http://localhost:8080`
- Health (público): `http://localhost:8080/actuator/health`
- Variáveis principais do serviço `app`: `SPRING_PROFILES_ACTIVE=docker`, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` (valores padrão alinhados ao serviço `db` no [`docker-compose.yml`](docker-compose.yml)).

### Segurança e JWT

- **`/actuator/health`** é sempre acessível sem autenticação (útil para health checks).
- Se **`spring.security.oauth2.resourceserver.jwt.issuer-uri`** ou **`spring.security.oauth2.resourceserver.jwt.jwk-set-uri`** estiver definido, as demais rotas exigem JWT. Caso contrário, as outras rotas ficam **permitidas** (adequado a desenvolvimento local e ao Compose até configurar um emissor de tokens).

## Testes

```bash
cd oficina && ./gradlew test
```
