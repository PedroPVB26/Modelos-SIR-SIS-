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

Write-Host "      Concluido" -ForegroundColor Green

# ===========================================================================
# PASSO 3: EXECUTAR BENCHMARKS
# ===========================================================================
Write-Host "[3/5] Executando benchmarks..." -ForegroundColor Yellow

cd "$ROOT\benchmarks"
java -cp "$BUILD_DIR;..\SIR\java;..\SIS\java" Benchmarks

if ($LASTEXITCODE -ne 0) {
    Write-Host "      ERRO durante execucao dos benchmarks" -ForegroundColor Red
    exit 1
}

Write-Host "      Concluido" -ForegroundColor Green

# ===========================================================================
# PASSO 4: GERAR GRAFICOS
# ===========================================================================
Write-Host "[4/5] Gerando graficos interativos..." -ForegroundColor Yellow

cd "$ROOT\scripts_analise"

if (Test-Path "$ROOT\dados\resultados_benchmark_completo.csv") {
    python analisar_resultados_interativo.py
    if ($LASTEXITCODE -eq 0) {
        Write-Host "      Concluido" -ForegroundColor Green
    } else {
        Write-Host "      ERRO ao gerar graficos" -ForegroundColor Red
    }
} elseif (Test-Path "$ROOT\dados\resultados_benchmark.csv") {
    python analisar_resultados_interativo.py
    if ($LASTEXITCODE -eq 0) {
        Write-Host "      Concluido" -ForegroundColor Green
    } else {
        Write-Host "      ERRO ao gerar graficos" -ForegroundColor Red
    }
} else {
    Write-Host "      ERRO: Arquivo CSV nao foi gerado!" -ForegroundColor Red
}

# ===========================================================================
# PASSO 5: GERAR INDEX
# ===========================================================================
Write-Host "[5/5] Gerando pagina de visualizacao..." -ForegroundColor Yellow

cd "$ROOT\scripts_analise"
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
Write-Host "========================================"  -ForegroundColor Green
Write-Host "  PROCESSO CONCLUIDO!"  -ForegroundColor Green
Write-Host "========================================"  -ForegroundColor Green
Write-Host ""
Write-Host "Arquivos gerados:" -ForegroundColor White
Write-Host "  Dados: dados\resultados_benchmark_completo.csv" -ForegroundColor Gray
Write-Host "  Graficos: graficos\*.html" -ForegroundColor Gray
Write-Host "  Visualizacao: graficos\index_graficos.html" -ForegroundColor Gray
Write-Host ""
