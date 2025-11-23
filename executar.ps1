# ============================================================================
# SCRIPT UNIFICADO - BENCHMARK COMPLETO
# Executa todos os testes: Sequencial, Paralelo, Cenarios e RMI
# Gera graficos e analises automaticamente
# ============================================================================

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

$ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host ""
Write-Host "========================================"  -ForegroundColor Cyan
Write-Host "  BENCHMARK COMPLETO - SIR/SIS"  -ForegroundColor Cyan
Write-Host "========================================"  -ForegroundColor Cyan
Write-Host ""

# ===========================================================================
# PASSO 1: LIMPEZA
# ===========================================================================
Write-Host "[1/7] Limpeza do ambiente..." -ForegroundColor Yellow

Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
Get-Job | Remove-Job -Force
Start-Sleep -Seconds 2

Remove-Item "$ROOT\dados\resultados_benchmark_completo.csv" -ErrorAction SilentlyContinue
Remove-Item "$ROOT\graficos\*" -Include *.png, *.html -ErrorAction SilentlyContinue

Write-Host "      Concluido" -ForegroundColor Green

# ===========================================================================
# PASSO 2: COMPILACAO
# ===========================================================================
Write-Host "[2/7] Compilacao das classes..." -ForegroundColor Yellow

# Criar pasta para arquivos compilados
$BUILD_DIR = "$ROOT\build"
if (-not (Test-Path $BUILD_DIR)) {
    New-Item -ItemType Directory -Path $BUILD_DIR | Out-Null
}

Write-Host "      - SIR..." -ForegroundColor Gray
cd "$ROOT\SIR\java"
javac -d "$BUILD_DIR" SIRSequencial.java SIRParalelo.java 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "      ERRO ao compilar SIR" -ForegroundColor Red
    exit 1
}

Write-Host "      - SIS..." -ForegroundColor Gray
cd "$ROOT\SIS\java"
javac -d "$BUILD_DIR" SISSequencial.java SISParalelo.java 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "      ERRO ao compilar SIS" -ForegroundColor Red
    exit 1
}

Write-Host "      - Cenarios..." -ForegroundColor Gray
cd "$ROOT\SIR\java"
javac -d "$BUILD_DIR" cenarios/*.java 2>&1 | Out-Null
cd "$ROOT\SIS\java"
javac -d "$BUILD_DIR" cenarios/*.java 2>&1 | Out-Null

Write-Host "      - Benchmark..." -ForegroundColor Gray
cd "$ROOT\benchmarks"

# Compilar com classpath incluindo a pasta build
javac -encoding UTF-8 -cp "$BUILD_DIR;..\SIR\java;..\SIS\java" -d "$BUILD_DIR" Benchmarks.java 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "      ERRO ao compilar Benchmarks" -ForegroundColor Red
    exit 1
}

Write-Host "      - Benchmark Distribuido..." -ForegroundColor Gray
javac -encoding UTF-8 -cp "$BUILD_DIR;..\SIR\java;..\SIS\java" -d "$BUILD_DIR" BenchmarksDistribuidoCompleto.java 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "      ERRO ao compilar BenchmarksDistribuidoCompleto" -ForegroundColor Red
    exit 1
}

Write-Host "      - Modelos Distribuidos (RMI)..." -ForegroundColor Gray
cd "$ROOT\SIR\java\distribuido"
javac -d "$BUILD_DIR" *.java 2>&1 | Out-Null
cd "$ROOT\SIS\java\distribuido"
javac -d "$BUILD_DIR" *.java 2>&1 | Out-Null

Write-Host "      Concluido" -ForegroundColor Green

# ===========================================================================
# PASSO 3: EXECUTAR BENCHMARKS LOCAIS
# ===========================================================================
Write-Host "[3/7] Executando benchmarks locais..." -ForegroundColor Yellow

cd "$ROOT\benchmarks"
java -cp "$BUILD_DIR;..\SIR\java;..\SIS\java" Benchmarks

if ($LASTEXITCODE -ne 0) {
    Write-Host "      ERRO durante execucao dos benchmarks" -ForegroundColor Red
    exit 1
}

Write-Host "      Concluido" -ForegroundColor Green

# ===========================================================================
# PASSO 4: EXECUTAR BENCHMARKS DISTRIBUIDOS (RMI)
# ===========================================================================
Write-Host "[4/7] Executando benchmarks distribuidos (RMI)..." -ForegroundColor Yellow

cd "$ROOT\benchmarks"

Write-Host "      Iniciando servidores RMI..." -ForegroundColor Gray

# Iniciar servidores RMI em background
$jobs = @()
for ($i = 1; $i -le 8; $i++) {
    $port = 1098 + $i
    $job = Start-Job -ScriptBlock {
        param($BuildDir, $RootDir, $Port, $HostNum)
        cd "$RootDir\benchmarks"
        java -cp "$BuildDir" -Djava.rmi.server.hostname=localhost `
             -Djava.rmi.server.codebase=file:///$BuildDir/ `
             SIRModelServer $Port $HostNum
    } -ArgumentList $BUILD_DIR, $ROOT, $port, $i
    $jobs += $job
}

Start-Sleep -Seconds 5  # Aguardar servidores iniciarem

Write-Host "      Executando testes distribuidos..." -ForegroundColor Gray
java -cp "$BUILD_DIR" BenchmarksDistribuidoCompleto

if ($LASTEXITCODE -ne 0) {
    Write-Host "      ERRO durante execucao dos benchmarks distribuidos" -ForegroundColor Red
    # Limpar jobs mesmo em caso de erro
    $jobs | Stop-Job
    $jobs | Remove-Job -Force
    exit 1
}

Write-Host "      Finalizando servidores..." -ForegroundColor Gray
$jobs | Stop-Job
$jobs | Remove-Job -Force

Write-Host "      Concluido" -ForegroundColor Green

# ===========================================================================
# PASSO 5: GERAR GRAFICOS
# ===========================================================================
Write-Host "[5/7] Gerando graficos interativos..." -ForegroundColor Yellow

cd "$ROOT\scripts_analise"

if (Test-Path "$ROOT\dados\resultados_benchmark_completo.csv") {
    python analisar_resultados_interativo.py
    if ($LASTEXITCODE -eq 0) {
        Write-Host "      Graficos principais gerados" -ForegroundColor Green
    } else {
        Write-Host "      ERRO ao gerar graficos principais" -ForegroundColor Red
    }
} elseif (Test-Path "$ROOT\dados\resultados_benchmark.csv") {
    python analisar_resultados_interativo.py
    if ($LASTEXITCODE -eq 0) {
        Write-Host "      Graficos principais gerados" -ForegroundColor Green
    } else {
        Write-Host "      ERRO ao gerar graficos principais" -ForegroundColor Red
    }
} else {
    Write-Host "      ERRO: Arquivo CSV nao foi gerado!" -ForegroundColor Red
}

# Gerar graficos distribuidos se o CSV existir
if (Test-Path "$ROOT\dados\resultados_benchmark_distribuido_completo.csv") {
    Write-Host "      Gerando graficos distribuidos..." -ForegroundColor Gray
    python analisar_resultados_distribuido_completo.py
    if ($LASTEXITCODE -eq 0) {
        Write-Host "      Graficos distribuidos gerados" -ForegroundColor Green
    } else {
        Write-Host "      AVISO: Erro ao gerar graficos distribuidos" -ForegroundColor Yellow
    }
}

Write-Host "      Concluido" -ForegroundColor Green

# ===========================================================================
# PASSO 6: ATUALIZAR DOCUMENTO
# ===========================================================================
Write-Host "[6/7] Atualizando documento com dados dos CSVs..." -ForegroundColor Yellow

cd "$ROOT\scripts_analise"
python atualizar_documento.py

if ($LASTEXITCODE -eq 0) {
    Write-Host "      Concluido" -ForegroundColor Green
} else {
    Write-Host "      ERRO ao atualizar documento" -ForegroundColor Red
}

# ===========================================================================
# PASSO 7: GERAR INDEX
# ===========================================================================
Write-Host "[7/7] Gerando pagina de visualizacao..." -ForegroundColor Yellow

python gerar_index_unificado.py

if (Test-Path "$ROOT\graficos\index_graficos.html") {
    Write-Host "      Concluido" -ForegroundColor Green
} else {
    Write-Host "      ERRO ao gerar pagina" -ForegroundColor Red
}

# ===========================================================================
# RESUMO FINAL
# ===========================================================================
Write-Host ""
Write-Host "[8/8] Abrindo navegador..." -ForegroundColor Yellow
Start-Process "$ROOT\graficos\index_graficos.html"
Write-Host ""
Write-Host "========================================"  -ForegroundColor Green
Write-Host "  PROCESSO CONCLUIDO!"  -ForegroundColor Green
Write-Host "========================================"  -ForegroundColor Green
Write-Host ""
Write-Host "Arquivos gerados:" -ForegroundColor White
Write-Host "  Dados locais: dados\resultados_benchmark.csv" -ForegroundColor Gray
Write-Host "  Dados distribuidos: dados\resultados_benchmark_distribuido_completo.csv" -ForegroundColor Gray
Write-Host "  Documento atualizado: APRESENTACAO_PDF.md" -ForegroundColor Cyan
Write-Host "  Graficos: graficos\*.html" -ForegroundColor Gray
Write-Host "  Visualizacao: graficos\index_graficos.html" -ForegroundColor Gray
Write-Host ""
