@echo off
REM Script para compilar e executar testes do projeto

echo ========================================
echo Compilando projeto...
echo ========================================

cd /d "%~dp0"

REM Compilar modelos base
javac SIR/java/SIRSequencial.java SIR/java/SIRParalelo.java
javac SIS/java/SISSequencial.java SIS/java/SISParalelo.java

REM Compilar cenarios
javac SIR/java/cenarios/*.java
javac SIS/java/cenarios/*.java

REM Compilar classe de testes
javac -cp ".;SIR/java;SIS/java" Testes.java

REM Compilar benchmarks
javac -cp ".;SIR/java;SIS/java" benchmarks/Benchmarks.java
javac ModeloRemotoSIR.java ModeloRemotoSIS.java
javac benchmarks/BenchmarksDistribuidoCompleto.java

echo.
echo ========================================
echo Compilacao concluida!
echo ========================================
echo.
echo Opcoes disponiveis:
echo   1. Executar testes basicos
echo   2. Executar benchmarks completos
echo   3. Gerar graficos interativos
echo.

set /p opcao="Escolha uma opcao (1-3): "

if "%opcao%"=="1" (
    echo.
    echo Executando testes basicos...
    java -cp ".;SIR/java;SIS/java" Testes
)

if "%opcao%"=="2" (
    echo.
    echo Executando benchmarks ^(isso pode demorar varios minutos^)...
    java -cp ".;SIR/java;SIS/java;benchmarks" Benchmarks
)

if "%opcao%"=="3" (
    echo.
    echo Gerando graficos interativos...
    cd scripts_analise
    python analisar_resultados_interativo.py
    cd ..
)

echo.
pause
