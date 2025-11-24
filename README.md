# 🦠 Modelos Epidemiológicos SIR/SIS - Framework de Simulação Paralela e Distribuída

> Implementação completa dos modelos **SIR** e **SIS** com estratégias de paralelização (threads) e distribuição (RMI) + Framework automatizado de benchmarks e análise de desempenho.

## 📋 O que é este projeto?

Sistema de simulação epidemiológica que implementa:
- **Modelo SIR:** Suscetível → Infectado → Recuperado (imune)
- **Modelo SIS:** Suscetível → Infectado → Suscetível (reinfecção)

**Diferenciais:**
- ✅ 4 estratégias de execução (sequencial, paralelo por população, paralelo por cenários, distribuído RMI)
- ✅ Framework completo de benchmarks (2.340 testes automatizados)
- ✅ Análise de desempenho com 16 gráficos interativos
- ✅ Automação total via script PowerShell

## 📁 Estrutura do Projeto

```
Projeto Final/
├── SIR/java/                     # Modelo SIR
│   ├── SIRSequencial.java        # Versão sequencial
│   ├── SIRParalelo.java          # Versão paralela (threads)
│   ├── cenarios/                 # Múltiplos cenários paralelos
│   └── distribuido/              # Versão distribuída (RMI)
│
├── SIS/java/                     # Modelo SIS (mesma estrutura)
│
├── benchmarks/                   # Testes de desempenho
│   ├── Benchmarks.java           # Benchmarks locais
│   └── BenchmarksDistribuidoCompleto.java  # Benchmarks RMI
│
├── scripts_analise/              # Análise e visualização
│   ├── analisar_resultados_interativo.py
│   ├── analisar_resultados_distribuido_completo.py
│   └── gerar_index_unificado.py
│
├── dados/                        # Resultados dos benchmarks
│   ├── resultados_benchmark.csv
│   └── resultados_benchmark_distribuido_completo.csv
│
├── graficos/                     # Gráficos HTML gerados
│   └── index_graficos.html       # Página principal
│
├── executar.ps1                  # 🚀 SCRIPT PRINCIPAL
├── APRESENTACAO_PDF.md           # Documento completo do projeto
└── README.md                     # Este arquivo
```

## 🚀 Como Executar

### Opção 1: Execução Completa Automatizada ⭐ **RECOMENDADO**

```powershell
.\executar.ps1
```

**O que acontece:**
1. ✅ Limpa ambiente anterior
2. ✅ Compila todas as classes Java
3. ✅ Executa benchmarks locais (~15 min, 1.980 testes)
4. ✅ Executa benchmarks distribuídos (~5 min, 360 testes)
5. ✅ Gera 16 gráficos interativos HTML
6. ✅ Cria página unificada com todos os resultados
7. ✅ **Abre automaticamente no navegador**

**Resultado:** Arquivo `graficos/index_graficos.html` com análise completa!

---

### Opção 2: Execução Manual (Passo a Passo)

#### 1️⃣ Compilar

```bash
# Criar diretório de build
mkdir build

# Compilar SIR
cd SIR/java
javac -d ../../build *.java cenarios/*.java distribuido/*.java

# Compilar SIS
cd ../../SIS/java
javac -d ../../build *.java cenarios/*.java distribuido/*.java

# Compilar Benchmarks
cd ../../benchmarks
javac -d ../build -cp "../build" *.java
```

#### 2️⃣ Executar Benchmarks

```bash
# Benchmarks locais (gera resultados_benchmark.csv)
cd benchmarks
java -cp "../build" Benchmarks

# Benchmarks distribuídos (gera resultados_benchmark_distribuido_completo.csv)
# Requer iniciar servidores RMI primeiro (veja executar.ps1 para detalhes)
java -cp "../build" BenchmarksDistribuidoCompleto
```

#### 3️⃣ Gerar Gráficos

```bash
cd scripts_analise
python analisar_resultados_interativo.py              # Gráficos locais
python analisar_resultados_distribuido_completo.py    # Gráficos distribuídos
python gerar_index_unificado.py                       # Página HTML unificada
```

---

### Opção 3: Apenas Visualização (Dados Já Existentes)

Se você já tem os CSVs e quer apenas regenerar os gráficos:

```bash
cd scripts_analise
python gerar_index_unificado.py
```

Depois abra: `graficos/index_graficos.html`

## 📊 Como Verificar os Resultados

### 1. Visualização Gráfica (Interativa)

**Arquivo principal:** `graficos/index_graficos.html`

**16 gráficos disponíveis:**

| Categoria | Gráficos |
|-----------|----------|
| **SIR Local** | Tempo × População/Passos/Threads, Speedup, Eficiência, Cenários |
| **SIS Local** | Tempo × População/Passos/Threads, Speedup, Eficiência, Cenários |
| **Distribuído** | Tempo × Hosts, Speedup Comparativo, Eficiência, Throughput |

**Recursos interativos:**
- 🖱️ Hover para valores exatos
- 🔍 Zoom com mouse/touch
- 📐 Pan (arrastar gráfico)
- 💾 Download PNG (botão no canto)

### 2. Dados Brutos (CSV)

**Arquivos gerados:**

| Arquivo | Descrição | Testes |
|---------|-----------|--------|
| `dados/resultados_benchmark.csv` | Benchmarks locais (seq, paralelo, cenários) | 1.980 |
| `dados/resultados_benchmark_distribuido_completo.csv` | Benchmarks RMI (múltiplos hosts) | 360 |

**Colunas importantes:**
- `Modelo`: SIR ou SIS
- `Tipo`: Sequencial, Paralelo, Cenarios_Sequencial, Cenarios_Paralelo
- `Populacao`, `Passos`, `Cenarios`: Configuração do teste
- `Threads` / `Hosts`: Nível de paralelização
- `Tempo_ms`: Tempo de execução (milissegundos)

### 3. Análise Textual

**Documento completo:** `APRESENTACAO_PDF.md`
- Introdução teórica aos modelos
- Análise detalhada dos resultados
- Comparação de estratégias
- Conclusões e lições aprendidas

**Converter para DOCX:**
```bash
cd scripts_analise
python converter_md_to_docx.py
```
Gera: `APRESENTACAO_PDF.docx`

## 🎯 Principais Resultados

### Paralelização por População ❌
- **8 threads:** 1.78x **MAIS LENTO** que sequencial
- **Causa:** Overhead de sincronização > trabalho útil
- **Conclusão:** Problema de grão muito fino

### Paralelização por Cenários ✅
- **SIR (8 threads):** 3.33x a 5.63x mais rápido
- **SIS (8 threads):** 4.51x a 6.53x mais rápido
- **Eficiência:** 42% a 82%
- **Conclusão:** Cenários independentes são ideais para paralelização!

### Distribuído RMI (8 hosts) ✅
- **SIR:** Speedup 5.79x, eficiência 72.4%
- **SIS:** Speedup 6.46x, eficiência 80.8%
- **Conclusão:** Excelente escalabilidade em ambiente distribuído

## 🛠️ Requisitos

**Obrigatórios:**
- ☕ **Java:** JDK 8 ou superior
- 🐍 **Python:** 3.7+
- 💻 **PowerShell:** 5.1+ (Windows)

**Bibliotecas Python:**
```bash
pip install pandas plotly python-docx
```

## 📝 Parâmetros dos Modelos

### SIR
- **β (beta):** 0.2 - Taxa de transmissão
- **γ (gamma):** 0.1 - Taxa de recuperação  
- **I₀:** 10.0 - Infectados iniciais

### SIS
- **β (beta):** 0.3 - Taxa de transmissão
- **γ (gamma):** 0.1 - Taxa de recuperação
- **I₀:** 1.0 - Infectados iniciais

## 👥 Autores

- Leonardo Silva e Cruz
- Lucas Francisco Alves Costa
- Isabella Pires da Silva
- Pedro Paulo Valente Bittencourt

**Instituição:** UTFPR (Universidade Tecnológica Federal do Paraná)  
**Repositório:** [github.com/PedroPVB26/Modelos-SIR-SIS-](https://github.com/PedroPVB26/Modelos-SIR-SIS-)

---

## 🆘 Resolução de Problemas

**Erro de compilação:**
```powershell
# Limpe e recompile
Remove-Item -Recurse -Force build
.\executar.ps1
```

**Benchmarks não executam:**
- Verifique se as portas 1099-1107 estão livres (RMI)
- Mate processos Java pendentes: `Get-Process java | Stop-Process -Force`

**Gráficos não aparecem:**
```bash
cd scripts_analise
python gerar_index_unificado.py
```

**Python não encontra pandas/plotly:**
```bash
pip install --upgrade pandas plotly
```
