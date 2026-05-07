# Documentação C4 - Oficina Backend

Este documento descreve a arquitetura do sistema de gerenciamento de oficina usando o modelo C4 (Context, Container, Component, Code).

## Visão Geral do Projeto

O **Oficina Backend** é uma API REST desenvolvida em Kotlin com Spring Boot que gerencia operações de uma oficina de veículos. O sistema controla clientes, veículos, ordens de trabalho, peças em estoque, catálogo de serviços e métricas operacionais.

### Tecnologias Principais
- **Framework**: Spring Boot 4.1.0 (Kotlin)
- **Banco de Dados**: PostgreSQL
- **Autenticação**: Spring Security + OAuth2 + JWT
- **Migrações**: Flyway
- **Documentação API**: OpenAPI 3.0 (Swagger)
- **Testing**: JUnit 5, MockK
- **Build**: Gradle

### Estrutura do Projeto
```
src/main/kotlin/com/soat/tech/challenge/oficina/
├── presentation/          # Controllers REST
├── application/           # Application Services (Use Cases)
├── domain/               # Entidades de negócio e Portas
├── infrastructure/       # Implementações de persistência e configurações
└── OficinaApplication.kt # Classe principal
```

## Diagramas C4

1. **[C1 - System Context](./C1_system_context.md)**: Visão geral do sistema e seus usuários/sistemas externos
2. **[C2 - Container](./C2_container.md)**: Componentes principais (API, BD, Serviços)
3. **[C3 - Component](./C3_component.md)**: Componentes internos da aplicação (Controllers, Services, Repositories)

## Padrões de Arquitetura

O projeto segue uma arquitetura em **camadas hexagonais** (Hexagonal Architecture) com separação clara de responsabilidades:

### Camadas

#### 1. **Presentation Layer** (Controladores REST)
Responsável por expor as operações da aplicação via API REST.
- Autenticação e Autorização baseada em JWT
- Validação de entrada
- Tratamento de exceções
- Serialização/Desserialização JSON

#### 2. **Application Layer** (Casos de Uso)
Contém a lógica de aplicação e orquestração de operações de negócio.
- Implementa os casos de uso
- Orquestra interações entre domínio e infraestrutura
- Mapeia DTOs para entidades de domínio

#### 3. **Domain Layer** (Lógica de Negócio)
Define o modelo de negócio e regras essenciais.
- Entidades de domínio
- Portas (interfaces) para persistência
- Exceções de domínio

#### 4. **Infrastructure Layer** (Implementações Técnicas)
Fornece implementações concretas de infraestrutura.
- Repositórios JPA
- Configurações de segurança
- Configurações de JWT
- Migrações de banco de dados

## Fluxo de uma Requisição

```
Cliente HTTP
    ↓
REST Controller (Presentation)
    ↓
Application Service (Application)
    ↓
Domain Model (Domain)
    ↓
Repository Port (Domain Interface)
    ↓
Repository Implementation (Infrastructure)
    ↓
PostgreSQL Database
```

## Recursos Principais

### Autenticação e Autorização
- **JWT**: Tokens assinados com HMAC
- **OAuth2**: Resource Server para validação de tokens
- **Autorização**: Role-based access control (RBAC) com Spring Security

### Persistência
- **ORM**: JPA/Hibernate via Spring Data JPA
- **Migrations**: Flyway para versionamento de schema
- **Banco**: PostgreSQL (suportado via driver postgresql)

### Validação
- **Bean Validation**: Anotações @Valid, @NotNull, etc.
- **Validação Customizada**: Validators e ExceptionHandlers

### Documentação
- **OpenAPI**: Documentação automática via springdoc-openapi
- **Swagger UI**: Interface gráfica para explorar a API

### Testes
- **Testes Unitários**: JUnit 5
- **Mocking**: MockK
- **Cobertura**: JaCoCo com limites mínimos de 80% no domain e application

## Segurança

### Recursos de Segurança
1. **Autenticação baseada em JWT**
2. **OAuth2 Resource Server para validação**
3. **CORS** configurado conforme necessário
4. **Validação de entrada em todos os endpoints**
5. **Tratamento de exceções seguro** (sem exposição de stack traces internos)

### Papéis de Usuário
- **ADMIN**: Acesso total ao sistema
- **TECNICO**: Acesso a ordens de trabalho e peças
- **ATENDENTE**: Acesso a ordens de trabalho públicas
- **CLIENTE**: Acesso limitado (visualização de suas próprias ordens)

## Métricas e Monitoring

### Actuator
- `/actuator/health`: Status de saúde da aplicação
- `/actuator/metrics`: Métricas da aplicação

## Próximos Passos

Para entender melhor a arquitetura:
1. Comece pelo [C1 - System Context](./C1_system_context.md) para entender o escopo
2. Veja o [C2 - Container](./C2_container.md) para entender os componentes principais
3. Explore o [C3 - Component](./C3_component.md) para detalhes de implementação

---

**Última atualização**: Maio 2026
