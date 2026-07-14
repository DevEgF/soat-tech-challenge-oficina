<#
.SYNOPSIS
    Valida a Fase 2 do projeto Oficina de ponta a ponta (testes, Docker, Kubernetes).

.DESCRIPTION
    Roda, em etapas independentes e com mensagens claras, as validações que dependem
    de ferramentas externas (Docker, Gradle/JDK 17, Terraform, kubectl, kind):

      -Tests    Sobe um PostgreSQL temporario e roda `gradlew check` (testes + cobertura).
      -Compose  Builda a imagem e sobe app + banco via docker compose; checa o health.
      -K8s      Cria um cluster kind, aplica os manifestos de k8s/ e faz smoke test.
      -All      Executa as tres etapas acima, em ordem.
      -KeepUp   (com -Compose/-K8s) NAO derruba os ambientes ao final.

    Sem parametros, executa -Tests e -Compose (as duas mais uteis no dia a dia).

    O script e seguro: cada etapa limpa o que criou (a menos de -KeepUp) e para com
    uma mensagem util se algo faltar. Nao altera o git, nao commita nada.

.EXAMPLE
    pwsh ./scripts/validar-fase2.ps1
    pwsh ./scripts/validar-fase2.ps1 -Tests
    pwsh ./scripts/validar-fase2.ps1 -All -KeepUp
#>
[CmdletBinding()]
param(
    [switch]$Tests,
    [switch]$Compose,
    [switch]$K8s,
    [switch]$All,
    [switch]$KeepUp
)

# ---------------------------------------------------------------------------
# Infra do script (cores, helpers, raiz do repo)
# ---------------------------------------------------------------------------
$ErrorActionPreference = 'Stop'
$RepoRoot = Split-Path -Parent $PSScriptRoot
$OficinaDir = Join-Path $RepoRoot 'oficina'

function Write-Step([string]$msg) { Write-Host "`n=== $msg ===" -ForegroundColor Cyan }
function Write-Ok([string]$msg)   { Write-Host "  [OK]   $msg" -ForegroundColor Green }
function Write-Warn2([string]$msg){ Write-Host "  [AVISO] $msg" -ForegroundColor Yellow }
function Write-Info([string]$msg) { Write-Host "  $msg" -ForegroundColor Gray }

function Test-Cmd([string]$name) {
    return [bool](Get-Command $name -ErrorAction SilentlyContinue)
}

function Require-Cmd([string]$name, [string]$comoInstalar) {
    if (-not (Test-Cmd $name)) {
        throw "Ferramenta '$name' nao encontrada no PATH. $comoInstalar"
    }
}

function Require-DockerRunning {
    Require-Cmd 'docker' "Instale o Docker Desktop (veja docs/FERRAMENTAS-FASE2.md)."
    try { docker info *> $null } catch { throw "" }
    if ($LASTEXITCODE -ne 0) {
        throw "O Docker esta instalado mas NAO esta rodando. Abra o Docker Desktop e aguarde ficar 'Running'."
    }
    Write-Ok "Docker em execucao."
}

# Espera ate $TimeoutSec uma condicao (scriptblock que retorna $true) virar verdadeira.
function Wait-For([scriptblock]$Condition, [int]$TimeoutSec, [string]$What) {
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        try { if (& $Condition) { return $true } } catch { }
        Start-Sleep -Seconds 3
        Write-Host '.' -NoNewline
    }
    Write-Host ''
    throw "Tempo esgotado ($TimeoutSec s) esperando: $What"
}

# ---------------------------------------------------------------------------
# Etapa 1 - Testes (gradlew check contra um Postgres temporario)
# ---------------------------------------------------------------------------
function Invoke-TestsStep {
    Write-Step 'Etapa 1/3 - Testes automatizados (gradlew check)'
    Require-DockerRunning

    $dbName = 'oficina-test-db'
    Write-Info "Subindo PostgreSQL temporario '$dbName' em localhost:5432..."

    # Remove um eventual container antigo com o mesmo nome.
    docker rm -f $dbName *> $null

    docker run -d --name $dbName `
        -e POSTGRES_DB=oficina -e POSTGRES_USER=oficina -e POSTGRES_PASSWORD=oficina `
        -p 5432:5432 postgres:16-alpine | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Falha ao subir o Postgres. A porta 5432 pode estar ocupada (outro Postgres rodando?)."
    }

    try {
        Write-Info 'Aguardando o banco aceitar conexoes'
        Wait-For -TimeoutSec 60 -What 'Postgres pronto (pg_isready)' -Condition {
            docker exec $dbName pg_isready -U oficina -d oficina *> $null
            $LASTEXITCODE -eq 0
        }
        Write-Host ''
        Write-Ok 'Postgres pronto.'

        Write-Info 'Rodando: gradlew.bat check  (testes unidade + dominio + integracao + JaCoCo)'
        Push-Location $OficinaDir
        try {
            & .\gradlew.bat check --no-daemon
            $code = $LASTEXITCODE
        } finally { Pop-Location }

        if ($code -ne 0) {
            throw "gradlew check FALHOU (exit $code). Veja o relatorio em oficina/build/reports/tests/test/index.html. Se a mensagem citar 'toolchain'/'No compatible toolchains', falta o JDK 17 (veja docs/FERRAMENTAS-FASE2.md)."
        }
        Write-Ok 'Todos os testes passaram e a cobertura (JaCoCo >= 80%) foi atingida.'
    }
    finally {
        Write-Info "Removendo o Postgres temporario '$dbName'..."
        docker rm -f $dbName *> $null
    }
}

# ---------------------------------------------------------------------------
# Etapa 2 - Docker Compose (app + banco) e health check
# ---------------------------------------------------------------------------
function Invoke-ComposeStep {
    Write-Step 'Etapa 2/3 - Docker Compose (app + PostgreSQL)'
    Require-DockerRunning

    $envFile = Join-Path $RepoRoot '.env'
    if (-not (Test-Path $envFile)) {
        Copy-Item (Join-Path $RepoRoot '.env.example') $envFile
        Write-Warn2 "Criado .env a partir do .env.example. Para uso real, edite as senhas."
    }

    Push-Location $RepoRoot
    try {
        Write-Info 'Buildando a imagem e subindo os servicos (pode demorar no primeiro build)...'
        docker compose up --build -d
        if ($LASTEXITCODE -ne 0) { throw 'docker compose up falhou.' }

        Write-Info 'Aguardando a aplicacao ficar saudavel (/actuator/health)'
        Wait-For -TimeoutSec 240 -What 'app respondendo {"status":"UP"}' -Condition {
            $r = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -TimeoutSec 5
            $r.status -eq 'UP'
        }
        Write-Host ''
        Write-Ok 'Aplicacao no ar: http://localhost:8080  (health UP)'
        Write-Info 'Swagger UI: http://localhost:8080/swagger-ui.html'
    }
    finally {
        if ($KeepUp) {
            Write-Warn2 'Ambiente mantido no ar (-KeepUp). Para derrubar: docker compose down'
        } else {
            Write-Info 'Derrubando os servicos (use -KeepUp para mante-los)...'
            docker compose down *> $null
        }
        Pop-Location
    }
}

# ---------------------------------------------------------------------------
# Etapa 3 - Kubernetes (cluster kind efemero + manifestos + smoke test)
# ---------------------------------------------------------------------------
function Invoke-K8sStep {
    Write-Step 'Etapa 3/3 - Kubernetes (cluster kind + manifestos k8s/)'
    Require-DockerRunning
    Require-Cmd 'kind'    'Instale o kind (veja docs/FERRAMENTAS-FASE2.md).'
    Require-Cmd 'kubectl' 'Instale o kubectl (veja docs/FERRAMENTAS-FASE2.md).'

    $cluster = 'oficina'
    $img = 'oficina-app:local'

    try {
        $exists = (kind get clusters 2>$null) -contains $cluster
        if (-not $exists) {
            Write-Info "Criando cluster kind '$cluster'..."
            kind create cluster --name $cluster
            if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar o cluster kind.' }
        } else {
            Write-Ok "Cluster kind '$cluster' ja existe."
        }

        Write-Info 'Buildando a imagem e carregando no cluster...'
        Push-Location $RepoRoot
        try {
            docker build -t $img -f Dockerfile .
            if ($LASTEXITCODE -ne 0) { throw 'docker build falhou.' }
            kind load docker-image $img --name $cluster
            if ($LASTEXITCODE -ne 0) { throw 'kind load falhou.' }

            Write-Info 'Aplicando manifestos (namespace, config, secret de teste, db, app, hpa)...'
            kubectl apply -f k8s/00-namespace.yaml
            kubectl apply -f k8s/10-configmap.yaml
            kubectl -n oficina delete secret oficina-secret --ignore-not-found *> $null
            kubectl -n oficina create secret generic oficina-secret `
                --from-literal=SPRING_DATASOURCE_PASSWORD=local-pass `
                --from-literal=POSTGRES_PASSWORD=local-pass `
                --from-literal=APP_JWT_SECRET=local-jwt-secret-suficientemente-longo-para-testes `
                --from-literal=APP_SECURITY_ADMIN_PASSWORD=admin `
                --from-literal=APP_RESEND_API_KEY=''
            kubectl apply -f k8s/20-db.yaml
            kubectl apply -f k8s/30-app.yaml
            kubectl apply -f k8s/40-hpa.yaml

            # Usa a imagem local carregada no kind (em vez de puxar do GHCR).
            kubectl -n oficina set image deployment/oficina-app app=$img
            kubectl -n oficina patch deployment oficina-app --type=json `
                -p '[{"op":"replace","path":"/spec/template/spec/containers/0/imagePullPolicy","value":"IfNotPresent"}]'

            Write-Info 'Aguardando rollout do banco e da aplicacao...'
            kubectl -n oficina rollout status deployment/oficina-db --timeout=180s
            kubectl -n oficina rollout status deployment/oficina-app --timeout=300s

            Write-Info 'Smoke test via port-forward (8080)...'
            $pf = Start-Process kubectl -ArgumentList 'port-forward','-n','oficina','svc/oficina-app','8080:8080' -PassThru -WindowStyle Hidden
            try {
                Wait-For -TimeoutSec 60 -What 'app respondendo no k8s' -Condition {
                    $r = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -TimeoutSec 5
                    $r.status -eq 'UP'
                }
                Write-Host ''
                Write-Ok 'Aplicacao saudavel no Kubernetes.'
            } finally {
                if ($pf -and -not $pf.HasExited) { Stop-Process -Id $pf.Id -Force }
            }

            Write-Info 'Recursos no namespace oficina:'
            kubectl -n oficina get pods,svc,hpa
        } finally { Pop-Location }
    }
    finally {
        if ($KeepUp) {
            Write-Warn2 "Cluster mantido (-KeepUp). Para remover: kind delete cluster --name $cluster"
        } else {
            Write-Info "Removendo o cluster kind '$cluster'..."
            kind delete cluster --name $cluster *> $null
        }
    }
}

# ---------------------------------------------------------------------------
# Orquestracao
# ---------------------------------------------------------------------------
if ($All) { $Tests = $true; $Compose = $true; $K8s = $true }
if (-not ($Tests -or $Compose -or $K8s)) { $Tests = $true; $Compose = $true }

Write-Host 'Validacao da Fase 2 - Oficina' -ForegroundColor White
Write-Info "Repo: $RepoRoot"
Write-Info ("Etapas: " + (@(
    if ($Tests)   { 'Testes' }
    if ($Compose) { 'Compose' }
    if ($K8s)     { 'K8s' }
) -join ', '))

$failures = @()
foreach ($etapa in @(
    @{ run = $Tests;   fn = { Invoke-TestsStep };   nome = 'Testes' },
    @{ run = $Compose; fn = { Invoke-ComposeStep }; nome = 'Compose' },
    @{ run = $K8s;     fn = { Invoke-K8sStep };     nome = 'K8s' }
)) {
    if (-not $etapa.run) { continue }
    try { & $etapa.fn }
    catch {
        $msg = $_.Exception.Message
        Write-Host "`n  [FALHA] Etapa '$($etapa.nome)': $msg" -ForegroundColor Red
        $failures += $etapa.nome
    }
}

Write-Host ''
if ($failures.Count -eq 0) {
    Write-Host 'Tudo validado com sucesso.' -ForegroundColor Green
    exit 0
} else {
    Write-Host ("Etapas com falha: " + ($failures -join ', ')) -ForegroundColor Red
    Write-Host 'Consulte docs/FERRAMENTAS-FASE2.md para instalar/configurar as ferramentas.' -ForegroundColor Yellow
    exit 1
}
