# 🧬 Benchmarks de Modelos Epidemiológicos - SIR/SIS

Análise de desempenho de simulações epidemiológicas usando abordagens sequencial, paralela e distribuída.

## 📁 Estrutura de Diretórios

```
Projeto Final/
│
├── SIR/java/                     # Modelo SIR (Susceptible-Infected-Recovered)
│   ├── SIRSequencial.java        # Versão sequencial
│   ├── SIRParalelo.java          # Versão paralela (threads)
│   ├── cenarios/                 # Simulações de múltiplos cenários
│   │   ├── SIRSequencialCenarios.java
│   │   └── SIRParaleloCenarios.java
│   └── distribuido/              # Versão RMI distribuída
│       ├── ModeloSIRRemoto.java
│       ├── ServidorModeloSIR.java
│       └── ClienteModeloSIR.java
│
├── SIS/java/                     # Modelo SIS (Susceptible-Infected-Susceptible)
│   ├── SISSequencial.java
│   ├── SISParalelo.java
│   ├── cenarios/
│   │   ├── SISSequencialCenarios.java
│   │   └── SISParaleloCenarios.java
│   └── distribuido/
│       ├── ModeloSISRemoto.java
│       ├── ServidorModeloSIS.java
│       └── ClienteModeloSIS.java
│
├── benchmarks/                   # Programas de benchmark
│   ├── Benchmarks.java          # Variação de tamanhos de problema
│   └── BenchmarksDistribuidoCompleto.java # Testes com múltiplos hosts
│
├── scripts_analise/             # Scripts Python para análise
│   ├── analisar_resultados_interativo.py  # Gráficos interativos (Plotly)
│   └── analisar_resultados_distribuido_completo.py
│
├── dados/                       # Resultados CSV dos benchmarks
│   ├── resultados_benchmark.csv
│   └── resultados_benchmark_distribuido_completo.csv
│
├── graficos/                    # Gráficos HTML gerados
│   ├── index_graficos.html     # Índice dos gráficos
│   └── grafico_*.html          # Gráficos individuais
│
├── Testes.java                 # Programa de testes rápidos
├── executar.ps1                # Script de automação PowerShell
├── compilar_e_executar.bat     # Script batch alternativo
├── GUIA_RAPIDO.md             # Guia de início rápido
└── README.md                   # Este arquivo
```

## 🚀 Execução Rápida (RECOMENDADO)

### Opção 1: Script PowerShell (Interativo)
```powershell
./executar.ps1
```

Escolha uma das opções:
1. **Executar testes básicos** - Simulações rápidas de demonstração
2. **Executar benchmarks completos** - Variação de tamanhos (demora ~10-30 min)
3. **Executar benchmarks distribuídos** - Testes com múltiplos hosts RMI
4. **Gerar gráficos interativos** - Análise visual dos resultados
5. **Abrir pasta de gráficos** - Visualizar gráficos HTML

### Opção 2: Script Batch
```batch
compilar_e_executar.bat
```

## 📋 Execução Manual

### 1. Compilar Projeto
```powershell
# Modelos SIR
javac -cp ".;SIR/java" SIR/java/SIRSequencial.java SIR/java/SIRParalelo.java
javac -cp ".;SIR/java" SIR/java/cenarios/*.java

# Modelos SIS
javac -cp ".;SIS/java" SIS/java/SISSequencial.java SIS/java/SISParalelo.java
javac -cp ".;SIS/java" SIS/java/cenarios/*.java

# Interfaces RMI
cd SIR/java/distribuido
javac ModeloSIRRemoto.java
javac ServidorModeloSIR.java
javac ClienteModeloSIR.java
cd ../../..

cd SIS/java/distribuido
javac ModeloSISRemoto.java
javac ServidorModeloSIS.java
javac ClienteModeloSIS.java
cd ../../..

# Testes e Benchmarks
javac -cp ".;SIR/java;SIS/java;SIR/java/distribuido;SIS/java/distribuido" Testes.java
javac -cp ".;SIR/java;SIS/java" benchmarks/Benchmarks.java
javac -cp ".;SIR/java;SIS/java" benchmarks/BenchmarksDistribuidoCompleto.java
```

### 2. Executar Testes Básicos
```powershell
java -cp ".;SIR/java;SIS/java;SIR/java/distribuido;SIS/java/distribuido" Testes
```

### 3. Executar Benchmarks
```powershell
# Benchmark de variação de tamanhos
java -cp ".;SIR/java;SIS/java;benchmarks" Benchmarks

# Benchmark distribuído (múltiplos hosts)
java -cp ".;SIR/java;SIS/java;benchmarks" BenchmarksDistribuidoCompleto
```

### 4. Gerar Gráficos
```powershell
cd scripts_analise
python analisar_resultados_interativo.py
python analisar_resultados_distribuido_completo.py
cd ..
```

### 5. Visualizar Resultados
Abra no navegador:
- `graficos/index_graficos.html` - Gráficos principais
- `graficos/index_graficos_distribuido.html` - Gráficos distribuídos

## 📊 Configurações de Teste

### Benchmarks.java - Variação de Tamanhos
- **População**: 100.000, 500.000, 1.000.000, 2.000.000
- **Passos de simulação**: 10.000, 25.000, 50.000
- **Número de cenários**: 100, 500, 1.000
- **Repetições**: 5 por configuração

### BenchmarksDistribuidoCompleto.java - Múltiplos Hosts
- **Número de hosts**: 1, 2, 4, 8
- **Cenários**: 100, 500, 1.000
- **Repetições**: 5 por configuração
- **Portas RMI**: 1099-1106

## 📈 Métricas Calculadas

### Desempenho
- **Tempo de execução médio** (milissegundos)
- **Desvio padrão** dos tempos
- **Throughput** (simulações/segundo)

### Paralelismo
- **Speedup**: razão tempo_sequencial / tempo_paralelo
- **Eficiência**: speedup / número_threads × 100%

### Distribuído
- **Escalabilidade**: desempenho vs número de hosts
- **Overhead de comunicação**: análise de latência RMI

## 🔧 Requisitos

### Software
- **Java**: JDK 8 ou superior
- **Python**: 3.8+ (para análise de dados)
- **Bibliotecas Python**:
  ```bash
  pip install pandas plotly
  ```

### Hardware Recomendado
- CPU: 4+ cores (testes paralelos)
- RAM: 4GB+ (simulações grandes)
- Rede: localhost (testes distribuídos simulados)

## 📖 Arquivos de Documentação

- **README.md** - Este arquivo (visão geral completa)
- **GUIA_RAPIDO.md** - Instruções de uso simplificadas
- **dados/*.csv** - Resultados brutos dos benchmarks
- **graficos/*.html** - Visualizações interativas

## 🎯 Fluxo de Trabalho Típico

1. **Compilar**: `./executar.ps1` → escolher compilação
2. **Executar testes**: Opção 1 (validação rápida)
3. **Rodar benchmarks**: Opção 2 (coleta de dados)
4. **Gerar gráficos**: Opção 4 (análise visual)
5. **Analisar resultados**: Abrir `graficos/index_graficos.html`

## 🐛 Solução de Problemas

### Erro: "cannot find symbol"
```powershell
# Recompilar tudo com classpaths corretos
./executar.ps1
```

### Erro: RMI "Connection refused"
Os testes distribuídos simulam múltiplos hosts localmente. Isso é esperado nos testes básicos quando servidores RMI não estão rodando.

### Python: "No module named 'pandas'"
```bash
pip install pandas plotly
```

## 📄 Licença

Projeto acadêmico - Análise de Desempenho de Simulações Epidemiológicas
