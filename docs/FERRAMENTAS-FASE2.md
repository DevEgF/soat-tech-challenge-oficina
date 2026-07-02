# Guia das ferramentas da Fase 2 (Windows)

> Para quem nunca mexeu com essas ferramentas. O objetivo é: **instalar**, **conferir
> que funcionou** e **rodar a validação** do projeto. Tudo no Windows, usando o
> **PowerShell** e o gerenciador de pacotes **winget** (já vem no Windows 11).

## Índice
1. [Visão geral — o que é cada ferramenta](#1-visão-geral)
2. [Instalação rápida (winget)](#2-instalação-rápida-winget)
3. [Conferindo se instalou](#3-conferindo-se-instalou)
4. [O script de validação](#4-o-script-de-validação)
5. [Usando cada ferramenta neste projeto](#5-usando-cada-ferramenta-neste-projeto)
6. [Problemas comuns](#6-problemas-comuns)

---

## 1. Visão geral

| Ferramenta | O que é | Para que serve aqui |
| ---------- | ------- | ------------------- |
| **Docker Desktop** | Roda "contêineres" — caixinhas isoladas com tudo que um programa precisa. | Subir o banco PostgreSQL e a própria aplicação sem instalar nada direto no Windows. |
| **JDK 17 (Temurin)** | O "kit" do Java na versão 17. | O projeto é compilado e testado com Java **17** (mesmo que você tenha o 21, precisa do 17). |
| **Gradle** | Ferramenta que compila, testa e empacota o projeto. | Você **não instala** — o projeto traz o `gradlew`/`gradlew.bat` (o "wrapper") que baixa o Gradle certo sozinho. |
| **kubectl** | Cliente de linha de comando do Kubernetes. | Conversar com o cluster: aplicar manifestos, ver pods, etc. |
| **kind** | "Kubernetes in Docker" — cria um cluster Kubernetes de mentira, local, dentro do Docker. | Testar o deploy em Kubernetes na sua máquina, de graça. |
| **Terraform** | Cria infraestrutura a partir de arquivos de texto ("infra como código"). | Provisionar o cluster kind de forma automatizada (pasta `infra/`). |

> **Ordem de importância:** Docker + JDK 17 já bastam para o essencial (testes e
> rodar a aplicação). kind/kubectl/Terraform são para a parte de Kubernetes.

---

## 2. Instalação rápida (winget)

Abra o **PowerShell** (tecla Windows → digite "PowerShell" → Enter) e rode:

```powershell
# Essenciais
winget install -e --id Docker.DockerDesktop
winget install -e --id EclipseAdoptium.Temurin.17.JDK

# Kubernetes (opcional, para a etapa K8s)
winget install -e --id Kubernetes.kubectl
winget install -e --id Kubernetes.kind
winget install -e --id Hashicorp.Terraform
```

Depois de instalar:
- **Feche e reabra o PowerShell** (para o PATH atualizar).
- **Abra o Docker Desktop** uma vez e espere aparecer **"Running"** (baleia verde).
  O Docker precisa estar **aberto e rodando** sempre que você for usar os contêineres.

> Sem winget? Baixe manualmente:
> Docker Desktop <https://www.docker.com/products/docker-desktop/> ·
> Temurin 17 <https://adoptium.net/temurin/releases/?version=17> ·
> kubectl <https://kubernetes.io/docs/tasks/tools/> ·
> kind <https://kind.sigs.k8s.io/docs/user/quick-start/#installation> ·
> Terraform <https://developer.hashicorp.com/terraform/install>

---

## 3. Conferindo se instalou

No PowerShell, cada comando deve imprimir uma versão (não um erro):

```powershell
docker --version
docker info        # se reclamar de conexão, o Docker Desktop não está aberto
java -version      # deve listar uma versão 17 disponível
kubectl version --client
kind --version
terraform --version
```

Para confirmar que o **JDK 17** está visível (mesmo tendo o 21 instalado):

```powershell
# Lista as versões de Java instaladas
Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -ErrorAction SilentlyContinue
```

Se aparecer uma pasta `jdk-17...`, está certo. O Gradle deste projeto procura o JDK 17
automaticamente entre os instalados.

---

## 4. O script de validação

Criamos um script que faz tudo em etapas, com mensagens claras e **limpando** o que
cria. Ele fica em [`scripts/validar-fase2.ps1`](../scripts/validar-fase2.ps1).

> **Antes de rodar:** abra o **Docker Desktop** e espere ficar "Running".

Na raiz do projeto, no PowerShell:

```powershell
# Etapas padrão (testes + subir a app via Docker Compose)
pwsh ./scripts/validar-fase2.ps1

# Só os testes
pwsh ./scripts/validar-fase2.ps1 -Tests

# Só subir a aplicação e deixá-la no ar para você explorar
pwsh ./scripts/validar-fase2.ps1 -Compose -KeepUp

# Tudo, incluindo Kubernetes (precisa de kind + kubectl)
pwsh ./scripts/validar-fase2.ps1 -All
```

> Se `pwsh` não existir, use `powershell` no lugar. Se aparecer erro de "execution
> policy", rode antes:
> `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass`

O que cada etapa faz:

| Etapa | Flag | O que valida |
| ----- | ---- | ------------ |
| Testes | `-Tests` | Sobe um PostgreSQL temporário e roda `gradlew check` (testes + cobertura ≥ 80%). |
| Compose | `-Compose` | Builda a imagem Docker e sobe **app + banco**; confere `/actuator/health` = `UP`. |
| Kubernetes | `-K8s` | Cria um cluster **kind**, aplica os manifestos de `k8s/` e faz smoke test. |

Ao final, ele diz **"Tudo validado com sucesso"** ou lista as etapas que falharam.

---

## 5. Usando cada ferramenta neste projeto

Se quiser rodar manualmente, sem o script:

### Docker Compose (app + banco)
```powershell
# Na raiz do repositório
Copy-Item .env.example .env      # primeira vez; edite as senhas se quiser
docker compose up --build -d     # sobe em segundo plano
# Aplicação: http://localhost:8080/actuator/health  e  /swagger-ui.html
docker compose logs -f app       # ver logs (Ctrl+C para sair)
docker compose down              # derruba tudo (use 'down -v' para apagar o banco)
```

### Testes (Gradle)
```powershell
# Precisa de um Postgres em localhost:5432 (o script -Tests faz isso por você)
cd oficina
./gradlew.bat check              # testes + cobertura
# Relatórios: oficina/build/reports/tests/test/index.html
```

### Kubernetes (kind + kubectl)
```powershell
kind create cluster --name oficina
docker build -t oficina-app:local -f Dockerfile .
kind load docker-image oficina-app:local --name oficina
kubectl apply -f k8s/            # (crie antes o k8s/11-secret.yaml a partir do .example)
kubectl -n oficina get pods,hpa
kubectl -n oficina port-forward svc/oficina-app 8080:8080
kind delete cluster --name oficina   # remover ao terminar
```
Detalhes (metrics-server para o HPA, etc.): [`k8s/README.md`](../k8s/README.md).

### Terraform (provisionar o cluster)
```powershell
cd infra
Copy-Item terraform.tfvars.example terraform.tfvars
terraform init                   # baixa os plugins (só na primeira vez)
terraform plan                   # mostra o que será criado
terraform apply                  # cria o cluster kind (precisa do Docker rodando)
terraform destroy                # remove tudo
```
Detalhes: [`infra/README.md`](../infra/README.md).

---

## 6. Problemas comuns

| Sintoma | Causa provável | Solução |
| ------- | -------------- | ------- |
| `error during connect ... dockerDesktopLinuxEngine` | Docker Desktop fechado. | Abra o Docker Desktop e espere "Running". |
| `gradlew check` falha com "No compatible toolchains" / "toolchain" | Falta o **JDK 17**. | Instale o Temurin 17 (seção 2) e rode de novo. |
| Porta `5432` ocupada ao subir o Postgres | Já existe um Postgres rodando (local ou outro contêiner). | Pare o outro Postgres, ou rode só `-Compose` (que usa rede interna do Docker). |
| Porta `8080` ocupada | Outra app usando a 8080. | Feche a outra app, ou ajuste a porta no `docker-compose.yml`. |
| `cannot be loaded because running scripts is disabled` | Política de execução do PowerShell. | `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass` e rode de novo. |
| App demora a ficar `UP` no Compose | Primeiro build + migrações Flyway. | Aguarde (o script espera até ~4 min). Veja `docker compose logs -f app`. |
| HPA fica `<unknown>/70%` | Falta o **metrics-server** no cluster. | Veja a seção de pré-requisitos em [`k8s/README.md`](../k8s/README.md). |
| `kubectl` diz "connection refused" | Nenhum cluster ativo / contexto errado. | Crie o cluster (`kind create cluster --name oficina`) e confira `kubectl config current-context`. |

---

> Dúvida em qualquer passo? Rode o comando de conferência da **seção 3** e compare a
> saída. Quase todo problema é "Docker não está aberto" ou "falta o JDK 17".
