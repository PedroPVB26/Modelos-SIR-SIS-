# GUIA RÁPIDO DE USO
## Projeto: Benchmarks de Modelos Epidemiológicos SIR/SIS

### 🚀 Início Rápido

#### Opção 1: Script PowerShell (Recomendado)
```powershell
./executar.ps1
```
Este script irá:
1. Compilar todo o projeto automaticamente
2. Mostrar um menu interativo com 5 opções
3. Permitir executar testes, benchmarks e gerar gráficos

#### Opção 2: Script Batch (Alternativa)
```batch
compilar_e_executar.bat
```
Funciona como alternativa ao PowerShell

#### Opção 3: Comandos Manuais
Se preferir executar manualmente:

**Compilar tudo:**
```powershell
javac -cp ".;SIR/java" SIR/java/*.java SIR/java/cenarios/*.java
javac -cp ".;SIS/java" SIS/java/*.java SIS/java/cenarios/*.java
javac -cp ".;SIR/java;SIS/java" Testes.java ModeloRemotoSIR.java ModeloRemotoSIS.java
javac -cp ".;SIR/java;SIS/java" benchmarks/*.java
```

**Executar testes básicos:**
```powershell
java -cp ".;SIR/java;SIS/java" Testes
```

**Executar benchmarks:**
```powershell
java -cp ".;SIR/java;SIS/java;benchmarks" Benchmarks
```

**Gerar gráficos:**
```powershell
cd scripts_analise
python analisar_resultados_interativo.py
```

---

### 📁 Estrutura do Projeto

```
Projeto Final/
├── SIR/java/              # Modelo SIR (Suscetível-Infectado-Recuperado)
├── SIS/java/              # Modelo SIS (Suscetível-Infectado-Suscetível)
├── benchmarks/            # Programas de benchmark
│   ├── Benchmarks.java                        # Testa variação de tamanhos
│   ├── BenchmarksDistribuido.java             # Testa SIR distribuído
│   └── BenchmarksDistribuidoCompleto.java     # Testa SIR e SIS distribuído
├── scripts_analise/       # Scripts Python para análise
│   ├── analisar_resultados.py
│   ├── analisar_resultados_interativo.py
│   └── analisar_resultados_distribuido_completo.py
├── dados/                 # CSVs com resultados
├── graficos/              # Gráficos HTML interativos
├── Testes.java           # Testes básicos
├── executar.ps1          # Script de automação PowerShell
└── README.md             # Documentação completa
```

---

### 📊 Tipos de Teste Disponíveis

#### 1️⃣ Testes Básicos (Opção 1)
- Executa simulações simples de SIR e SIS
- Mostra resultados de cenários pré-configurados
- **Tempo:** ~30 segundos

#### 2️⃣ Benchmarks Completos (Opção 2)
- Testa diferentes tamanhos de população
- Compara versões sequenciais vs paralelas
- Calcula speedup e eficiência
- **Tempo:** 5-10 minutos
- **Output:** `dados/resultados_benchmark.csv`

#### 3️⃣ Benchmarks Distribuídos (Opção 3)
- Testa sistemas distribuídos com RMI
- Simula múltiplos hosts (1-8 hosts)
- Mede comunicação de rede
- **Tempo:** 15-30 minutos
- **Output:** `dados/resultados_benchmark_distribuido_completo.csv`

#### 4️⃣ Gerar Gráficos (Opção 4)
- Cria gráficos HTML interativos com Plotly
- 10 visualizações diferentes
- Navegável via `graficos/index_graficos.html`
- **Requisitos:** Python 3 com pandas e plotly

#### 5️⃣ Abrir Gráficos (Opção 5)
- Abre a pasta `graficos/` no explorador

---

### 🔧 Requisitos

**Java:**
- JDK 8 ou superior
- Variável JAVA_HOME configurada

**Python (para gráficos):**
```powershell
pip install pandas plotly
```

---

### 📈 Métricas Calculadas

Os benchmarks medem:
- **Tempo de execução** (ms)
- **Speedup** = Tempo_Sequencial / Tempo_Paralelo
- **Eficiência** = Speedup / Número_de_Threads
- **Overhead** (para sistemas distribuídos)

---

### 🐛 Solução de Problemas

**Erro: "package cenarios does not exist"**
→ Use `-cp ".;SIR/java;SIS/java"` ao compilar

**Erro: "Could not find or load main class"**
→ Execute do diretório raiz do projeto

**Gráficos não aparecem**
→ Certifique-se que pandas e plotly estão instalados:
```powershell
pip install pandas plotly
```

**RMI Registry error**
→ Certifique-se que as portas 1099-1106 estão livres

---

### 📧 Informações do Projeto

- **Tipo:** Trabalho Final - Programação Concorrente
- **Modelos:** SIR e SIS (epidemiologia)
- **Paradigmas:** Sequencial, Paralelo (threads), Distribuído (RMI)
- **Análise:** Benchmarks com variação de tamanho e hosts

Para mais detalhes, consulte `README.md`
