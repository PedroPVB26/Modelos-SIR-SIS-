# Script de compilacao e execucao do projeto
# Projeto: Benchmarks de Modelos Epidemiologicos SIR/SIS

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Compilando Projeto..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$ErrorActionPreference = "Continue"

# Compilar modelos base
Write-Host "`n[1/6] Compilando modelos SIR..." -ForegroundColor Yellow
javac -cp ".;SIR/java" SIR/java/SIRSequencial.java SIR/java/SIRParalelo.java

Write-Host "[2/6] Compilando cenarios SIR..." -ForegroundColor Yellow
javac -cp ".;SIR/java" SIR/java/cenarios/*.java

Write-Host "[3/6] Compilando modelos SIS..." -ForegroundColor Yellow
javac -cp ".;SIS/java" SIS/java/SISSequencial.java SIS/java/SISParalelo.java

Write-Host "[4/6] Compilando cenarios SIS..." -ForegroundColor Yellow
javac -cp ".;SIS/java" SIS/java/cenarios/*.java

# Compilar interfaces RMI
Write-Host "[5/6] Compilando interfaces RMI..." -ForegroundColor Yellow
Push-Location SIR/java/distribuido
javac ModeloSIRRemoto.java
javac ServidorModeloSIR.java
javac ClienteModeloSIR.java
Pop-Location

Push-Location SIS/java/distribuido
javac ModeloSISRemoto.java
javac ServidorModeloSIS.java
javac ClienteModeloSIS.java
Pop-Location

javac -cp ".;SIR/java;SIS/java;SIR/java/distribuido;SIS/java/distribuido" Testes.java

# Compilar Benchmarks
Write-Host "[6/6] Compilando Benchmarks..." -ForegroundColor Yellow
javac -cp ".;SIR/java;SIS/java" benchmarks/Benchmarks.java
javac -cp ".;SIR/java;SIS/java" benchmarks/BenchmarksDistribuidoCompleto.java

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  Compilacao Concluida!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# ============================================================================
# EXECUCAO AUTOMATICA DE TODOS OS TESTES
# ============================================================================

Write-Host "`n" -NoNewline
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  EXECUTANDO TESTES AUTOMATICAMENTE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Testes Basicos
Write-Host "`n[1/4] Executando testes basicos..." -ForegroundColor Yellow
Write-Host "      (Validacao rapida de todas as implementacoes)" -ForegroundColor Gray
java -cp ".;SIR/java;SIS/java;SIR/java/distribuido;SIS/java/distribuido" Testes

# 2. Benchmarks Completos
Write-Host "`n[2/4] Executando benchmarks completos..." -ForegroundColor Yellow
Write-Host "      (Isso pode demorar 10-30 minutos)" -ForegroundColor Gray
Write-Host "      Progresso sera salvo em: dados/resultados_benchmark.csv" -ForegroundColor Gray
java -cp ".;SIR/java;SIS/java;benchmarks" Benchmarks
Write-Host "`n      Benchmarks concluidos! CSV salvo em dados/" -ForegroundColor Green

# 3. Benchmarks Distribuidos
Write-Host "`n[3/4] Executando benchmarks distribuidos..." -ForegroundColor Yellow
Write-Host "      (Testando multiplos hosts RMI - pode demorar 15-40 minutos)" -ForegroundColor Gray
Write-Host "      Progresso sera salvo em: dados/resultados_benchmark_distribuido_completo.csv" -ForegroundColor Gray
java -cp ".;SIR/java;SIS/java;benchmarks" BenchmarksDistribuidoCompleto
Write-Host "`n      Benchmarks distribuidos concluidos!" -ForegroundColor Green

# 4. Geracao de Graficos
Write-Host "`n[4/4] Gerando graficos interativos..." -ForegroundColor Yellow
Write-Host "      (Analise visual dos resultados)" -ForegroundColor Gray

Push-Location scripts_analise
Write-Host "      - Graficos principais..." -ForegroundColor Gray
python analisar_resultados_interativo.py

Write-Host "      - Graficos distribuidos..." -ForegroundColor Gray
python analisar_resultados_distribuido_completo.py

Write-Host "      - Gerando pagina indice unificada..." -ForegroundColor Gray
python gerar_index_unificado.py
Pop-Location

Write-Host "`n      Graficos gerados em: graficos/" -ForegroundColor Green
Write-Host "      Pagina principal: graficos/index_graficos.html" -ForegroundColor Cyan

# ============================================================================
# RESUMO FINAL
# ============================================================================

Write-Host "`n" -NoNewline
Write-Host "========================================" -ForegroundColor Green
Write-Host "  EXECUCAO COMPLETA FINALIZADA!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host "`nArquivos gerados:" -ForegroundColor Cyan
Write-Host "  Resultados CSV:" -ForegroundColor White
Write-Host "    - dados/resultados_benchmark.csv" -ForegroundColor Gray
Write-Host "    - dados/resultados_benchmark_distribuido_completo.csv" -ForegroundColor Gray
Write-Host "`n  Graficos HTML:" -ForegroundColor White
Write-Host "    - graficos/index_graficos.html (TODOS OS GRAFICOS)" -ForegroundColor Cyan
Write-Host "    - 15 graficos interativos individuais" -ForegroundColor Gray

Write-Host "`nProximos passos:" -ForegroundColor Yellow
Write-Host "  1. Abra graficos/index_graficos.html no navegador" -ForegroundColor White
Write-Host "  2. Navegue entre graficos paralelos e distribuidos" -ForegroundColor White
Write-Host "  3. Analise os resultados interativos (zoom, hover, etc.)" -ForegroundColor White

Write-Host "`nPressione qualquer tecla para continuar..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
