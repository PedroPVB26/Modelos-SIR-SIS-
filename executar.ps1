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

# Menu de opcoes
Write-Host "`nOpcoes disponiveis:" -ForegroundColor Cyan
Write-Host "  1. Executar testes basicos" -ForegroundColor White
Write-Host "  2. Executar benchmarks completos (tamanhos)" -ForegroundColor White
Write-Host "  3. Executar benchmarks distribuidos" -ForegroundColor White
Write-Host "  4. Gerar graficos interativos" -ForegroundColor White
Write-Host "  5. Abrir pasta de graficos" -ForegroundColor White
Write-Host "  0. Sair" -ForegroundColor White

$opcao = Read-Host "`nEscolha uma opcao (0-5)"

switch ($opcao) {
    "1" {
        Write-Host "`nExecutando testes basicos..." -ForegroundColor Yellow
        java -cp ".;SIR/java;SIS/java;SIR/java/distribuido;SIS/java/distribuido" Testes
    }
    "2" {
        Write-Host "`nExecutando benchmarks (isso pode demorar varios minutos)..." -ForegroundColor Yellow
        Write-Host "Progresso sera salvo em: dados/resultados_benchmark.csv" -ForegroundColor Gray
        java -cp ".;SIR/java;SIS/java;benchmarks" Benchmarks
        Write-Host "`nBenchmarks concluidos! CSV salvo em dados/" -ForegroundColor Green
    }
    "3" {
        Write-Host "`nExecutando benchmarks distribuidos..." -ForegroundColor Yellow
        Write-Host "ATENCAO: Isso testa multiplos hosts RMI e pode demorar!" -ForegroundColor Red
        java -cp ".;SIR/java;SIS/java;benchmarks" BenchmarksDistribuidoCompleto
        Write-Host "`nBenchmarks distribuidos concluidos!" -ForegroundColor Green
    }
    "4" {
        Write-Host "`nGerando graficos interativos..." -ForegroundColor Yellow
        Push-Location scripts_analise
        python analisar_resultados_interativo.py
        Pop-Location
        Write-Host "`nGraficos gerados em: graficos/" -ForegroundColor Green
        Write-Host "  Abra graficos/index_graficos.html no navegador" -ForegroundColor Gray
    }
    "5" {
        Write-Host "`nAbrindo pasta de graficos..." -ForegroundColor Yellow
        Invoke-Item graficos
    }
    "0" {
        Write-Host "`nSaindo..." -ForegroundColor Yellow
        exit
    }
    default {
        Write-Host "`nOpcao invalida!" -ForegroundColor Red
    }
}

Write-Host "`nPressione qualquer tecla para continuar..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
