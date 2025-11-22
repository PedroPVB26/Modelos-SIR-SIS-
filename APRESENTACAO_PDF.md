# Modelagem Epidemiológica Paralela e Distribuída
## Modelos SIR e SIS - Framework Completo de Benchmarks

**Membros**  
- Leonardo Silva e Cruz
- Lucas Francisco Alves Costa
- Isabella Pires da Silva
- Pedro Paulo Valente Bittencourt


**Instituição:** Universidade Tecnológica Federal Do Paraná (UTFPR)
**Data:** Novembro de 2025  
**Repositório:** [github.com/PedroPVB26/Modelos-SIR-SIS-](https://github.com/PedroPVB26/Modelos-SIR-SIS-)

---

## 1. Problema

### Contexto
Simulações epidemiológicas podem ser computacionalmente intensivas com:
- Populações grandes (milhões de indivíduos)
- Longos períodos de simulação (milhares de passos temporais)
- Múltiplos cenários para análise de sensibilidade

### Modelos Implementados

**Modelo SIR:** Doenças com imunidade permanente (Sarampo, Rubéola)
```
S → I → R
dS/dt = -β·S·I/N
dI/dt = β·S·I/N - γ·I
dR/dt = γ·I
```

**Modelo SIS:** Doenças sem imunidade (Gripe, Resfriado)
```
S ⇄ I
dS/dt = -β·S·I/N + γ·I
dI/dt = β·S·I/N - γ·I
```

**Método Numérico:** Runge-Kutta 4ª ordem (RK4) - precisão e estabilidade

---

## 2. Proposta de Solução

### Implementações Desenvolvidas

**1. SEQUENCIAL (Baseline)**
- Implementação direta do RK4
- Simulação completa da população
- Referência para comparações

**2. PARALELO - Divisão por População com Threads (1,2,4,8)**
- Estratégia: Dividir população em N threads
- Cada thread simula um bloco de indivíduos
- Exemplo: 100k indivíduos ÷ 8 threads = 12.5k por thread
- **Resultado:** Overhead domina o trabalho útil

**3. PARALELO - Cenários Independentes (SUCESSO)**
- Estratégia: Simular múltiplos cenários simultaneamente
- Uso: Análise de sensibilidade, variação de parâmetros
- **Resultado:** Speedup de 3.4x até 7.6x

**4. DISTRIBUÍDO (RMI)**
- Cliente-servidor com múltiplos hosts simulados (portas 1099-1106)
- Testes extensivos com 100, 500 e 1000 cenários
- **Resultado**: Speedup de 3-5x com 8 hosts

---

## 3. Framework de Automação e Benchmarks

### Sistema de Execução Centralizado

**Script Principal:** `executar.ps1` (PowerShell)
- ✅ Compilação automática de todo o projeto (6 etapas)
- ✅ Execução de testes básicos (validação rápida)
- ✅ Benchmarks completos (~1.620 testes)
- ✅ Benchmarks distribuídos RMI (múltiplos hosts, ~540 testes)
- ✅ Geração automática de 15 gráficos interativos
- ✅ Abertura automática no navegador

**Organização do Build:**
- Código-fonte: `SIR/java/`, `SIS/java/`, `benchmarks/`
- Bytecode compilado: `build/` (separado, em .gitignore)
- Resultados: `datos/` (CSVs com timestamps)
- Visualizações: `graficos/` (HTML interativos com Plotly)

**Comando único:**
```powershell
./executar.ps1
```

### Dados Coletados

**Benchmarks.java** - Variação de tamanhos:
- População: 100k, 500k, 1M, 2M indivíduos
- Passos: 10k, 25k, 50k
- Cenários: 100, 500, 1000
- Repetições: 15 por configuração
- Threads testadas: Sequencial, 1, 2, 4, 8
- Total: ~1.620 testes (4 pop × 3 passos × 5 threads × 15 rep) + (3 cenários × 5 threads × 15 rep × 2 modelos)

**BenchmarksDistribuidoCompleto.java** - RMI:
- Hosts simulados: 1, 2, 4, 8 (portas 1099-1106)
- Cenários: 100, 500, 1000
- Repetições: 15 por configuração
- Modelos: SIR e SIS
- Total: ~540 testes (3 cenários × 4 hosts × 15 rep × 2 modelos)

**Total geral:** ~2.160 testes executados

---

## 4. Resultados de Desempenho

### 🟡 Paralelização de População - Análise Detalhada

#### SIR - População 100k, Passos 50k

| Threads | Tempo Médio | Desvio Padrão | vs Sequencial | Conclusão |
|---------|-------------|---------------|---------------|-----------|
| Sequencial | 3.9 ms | ±0.47 ms | - | Baseline |
| 1 thread | 4.2 ms | ±0.79 ms | 1.08x mais lento | Overhead mínimo |
| 2 threads | 5.9 ms | ±4.56 ms | 1.51x mais lento | Overhead cresce |
| 4 threads | 11.3 ms | ±9.08 ms | 2.90x mais lento | Overhead alto |
| 8 threads | 10.2 ms | ±4.37 ms | 2.62x mais lento | Overhead alto |

**📊 ANÁLISE:**
- Sequencial: ~4ms de trabalho útil (±0.47ms - muito estável)
- Paralelo 8 threads: ~10ms (2.6x MAIS LENTO, ±4.37ms)
- **Overhead de paralelização: ~6ms** (criação threads + sincronização)
- **Grão muito fino:** trabalho por thread (~0.5ms) << overhead (~6ms)
- **Desvio padrão cresce com threads:** sequencial ±0.5ms → 8 threads ±4.4ms (instabilidade)

**Por que não há ganho?**
- Trabalho por thread: 3.9ms ÷ 8 = **0.49ms**
- Overhead de sincronização: **~6ms**
- Overhead é **12x maior** que trabalho útil por thread

---

### ✅ SUCESSO: Paralelização de Cenários

#### SIR - Múltiplos Cenários (População 1M, 50k passos)

| Cenários | Sequencial | Paralelo (8 threads) | Speedup | Eficiência | Status |
|----------|------------|----------------------|---------|------------|--------|
| 100      | 257.2 ms   | 75.3 ms              | 3.42x   | 42.7%      | ✅ Bom |
| 500      | 1310.7 ms  | 171.8 ms             | 7.63x   | 95.4%      | ✅ Excelente |
| 1000     | 2056.3 ms  | 331.0 ms             | 6.21x   | 77.6%      | ✅ Muito Bom |

**🟢 ANÁLISE:**
- Speedup consistente entre 3.4x e 7.6x
- Eficiência > 75% para 500+ cenários
- Overhead diluído em trabalho substancial (257-2056ms)
- **Grão grosso:** cada cenário é independente e completo

#### SIS - Múltiplos Cenários (População 1M, 50k passos)

| Cenários | Sequencial | Paralelo (8 threads) | Speedup | Eficiência |
|----------|------------|----------------------|---------|------------|
| 100      | 216.4 ms   | 51.3 ms              | 4.22x   | 52.7%      |
| 500      | 1176.4 ms  | 168.2 ms             | 6.99x   | 87.4%      |
| 1000     | 2139.0 ms  | 318.9 ms             | 6.71x   | 83.9%      |

**Resultado:** Ambos modelos (SIR e SIS) têm excelente speedup em cenários!

---

### 🌐 DISTRIBUÍDO (RMI): Múltiplos Hosts

#### SIR - Cenários com Hosts RMI (População 1M, 50k passos)

| Cenários | 1 Host | 2 Hosts | 4 Hosts | 8 Hosts | Speedup (8 hosts) | Eficiência |
|----------|--------|---------|---------|---------|-------------------|------------|
| 100 | 253.5 ms | 121.6 ms | 68.7 ms | 67.4 ms | 3.76x | 47.0% |
| 500 | 1318.1 ms | 676.8 ms | 335.2 ms | 272.2 ms | 4.84x | 60.5% |
| 1000 | 2415.8 ms | 1241.0 ms | 690.8 ms | 449.8 ms | 5.37x | 67.1% |

**📊 ANÁLISE:**
- Speedup consistente: 3.76x até 5.37x com 8 hosts
- Eficiência cresce com número de cenários (47% → 67%)
- Overhead de RMI visível: ~30-40% vs paralelo local
- Comunicação de rede adiciona latência (~50-100ms)

#### SIS - Cenários com Hosts RMI (População 1M, 50k passos)

| Cenários | 1 Host | 2 Hosts | 4 Hosts | 8 Hosts | Speedup (8 hosts) | Eficiência |
|----------|--------|---------|---------|---------|-------------------|------------|
| 100 | 315.1 ms | 168.0 ms | 82.7 ms | 60.9 ms | 5.17x | 64.6% |
| 500 | 1479.0 ms | 768.4 ms | 317.6 ms | 158.5 ms | 9.33x | 116.6% | 
| 1000 | 2274.2 ms | 1180.5 ms | 622.4 ms | 326.8 ms | 6.96x | 87.0% |

**🟢 ANÁLISE:**
- SIS mostra melhor escalabilidade que SIR no RMI
- 500 cenários: eficiência > 100% (super-linear!) devido a cache
- Overhead RMI bem compensado em problemas grandes
- Speedup máximo: 9.33x (500 cenários, 8 hosts)

**Comparação Local vs Distribuído (500 cenários, 8 threads/hosts):**

| Modelo | Local (threads) | Distribuído (RMI) | Overhead RMI |
|--------|----------------|-------------------|---------------|
| SIR | 171.8 ms | 272.2 ms | +58% |
| SIS | 168.2 ms | 158.5 ms | -6% (melhor!) |

**Conclusão:** Distribuído RMI viável para problemas grandes, mas com overhead de rede.

---

### Comparação Direta: Estratégias de Paralelização

| Estratégia             | Trabalho/Thread | Overhead | Speedup | Eficiência | Resultado |
|------------------------|----------------|----------|---------|------------|-----------|
| 1 Thread (População)   | ~4 ms          | ~0.2 ms  | 1.08x ↓ | -8%        | 🟡 Neutro |
| 2 Threads (População)  | ~2 ms          | ~2 ms    | 1.51x ↓ | -25%       | 🟠 Ruim   |
| 4 Threads (População)  | ~1 ms          | ~7 ms    | 2.90x ↓ | -48%       | 🔴 Péssimo|
| 8 Threads (População)  | ~0.5 ms        | ~6 ms    | 2.62x ↓ | -20%       | 🔴 Péssimo|
| **Cenários (100)**     | **257 ms**     | **~15 ms** | **3.42x ↑** | **43%** | ✅ **Bom** |
| **Cenários (500)**     | **1311 ms**    | **~15 ms** | **7.63x ↑** | **95%** | ✅ **Ótimo** |
| **Cenários (1000)**    | **2056 ms**    | **~15 ms** | **6.21x ↑** | **78%** | ✅ **Muito Bom** |
| **Distribuído RMI (100)** | **253 ms** | **~50 ms** | **3.76x ↑** | **47%** | ✅ **Bom** |
| **Distribuído RMI (500)** | **1318 ms** | **~80 ms** | **4.84x ↑** | **61%** | ✅ **Bom** |
| **Distribuído RMI (1000)** | **2416 ms** | **~100 ms** | **5.37x ↑** | **67%** | ✅ **Muito Bom** |

**Diferença de Desempenho:**
- População (8 threads): **2.62x MAIS LENTO** (perda de 162%)
- Cenários (500): **7.63x MAIS RÁPIDO** (ganho de 663%)
- **Gap entre estratégias: ~1000% de diferença!**

---

## 4. Desafios e Soluções

### Desafio 1: Overhead de Threads Supera Ganho 🟡

**O que implementamos:**
```java
// Dividir população em N threads (1, 2, 4, 8)
int tamanhoBlocoPopulacao = populacaoTotal / numeroThreads;

for (int t = 0; t < numeroThreads; t++) {
    int inicio = t * tamanhoBlocoPopulacao;
    int fim = (t == numeroThreads - 1) ? populacaoTotal : inicio + tamanhoBlocoPopulacao;
    executor.submit(() -> simularBloco(inicio, fim));
}
// Aguardar todas threads e agregar resultados
```

**Por que não escalou bem:**

**Análise de Custos (100k população, 50k passos):**
```
SEQUENCIAL:
  Trabalho útil total: 3.9 ms

PARALELO (8 THREADS):
  Criação de threads:        ~2.0 ms
  Sincronização/agregação:   ~4.0 ms
  Trabalho útil (8 threads): ~4.0 ms (não paraleliza bem!)
  ────────────────────────────────
  Total paralelo:           ~10.0 ms
  
RESULTADO: 2.6x MAIS LENTO!
```

**Por que trabalho não paraleliza?**
- Problema muito pequeno: 3.9ms total
- Dividir em 8 partes: 0.49ms por thread
- **Tempo mínimo de escalonamento Java: ~0.5-1ms**
- Threads passam mais tempo esperando CPU do que computando!

**Solução:** Use menos threads ou problemas maiores. Para 100k população:
- 1-2 threads: overhead tolerável
- 4-8 threads: overhead domina

---

### Desafio 2: Dependências Matemáticas no RK4 ❌

**Problema:** Tentar paralelizar cálculo de k1, k2, k3, k4

**Por que é matematicamente impossível:**
```
k1 = h · f(t, y)
k2 = h · f(t + h/2, y + k1/2)  ← Precisa de k1!
k3 = h · f(t + h/2, y + k2/2)  ← Precisa de k2!
k4 = h · f(t + h, y + k3)      ← Precisa de k3!
```

**Dependências sequenciais obrigatórias:** k1 → k2 → k3 → k4

**Análise:** Lei de Amdahl
- Parte sequencial (RK4): 100%
- Parte paralelizável: 0%
- **Speedup máximo teórico: 1.0x** (nenhum ganho possível)

**Solução:** Aceitar que RK4 é inerentemente sequencial.

---

### Desafio 3: Encontrar Granularidade Adequada ✅

**Problema:** Overhead de threads domina em trabalhos pequenos

**Análise de Granularidade:**

| Estratégia | Trabalho/Thread | Overhead | Razão | Speedup | Resultado |
|-----------|----------------|----------|-------|---------|-----------|
| 1 thread (pop.) | 3.9 ms | ~0.2 ms | 20:1 | 1.08x ↓ | 🟡 Limiar |
| 2 threads (pop.) | 2.0 ms | ~2.0 ms | 1:1 | 1.51x ↓ | 🟠 Ruim |
| 4 threads (pop.) | 1.0 ms | ~7.0 ms | 1:7 | 2.90x ↓ | 🔴 Péssimo |
| 8 threads (pop.) | 0.5 ms | ~6.0 ms | 1:12 | 2.62x ↓ | 🔴 Péssimo |
| **100 cenários** | **257 ms** | **~15 ms** | **17:1** | **3.42x ↑** | ✅ **Bom** |
| **500 cenários** | **1311 ms** | **~15 ms** | **87:1** | **7.63x ↑** | ✅ **Ótimo** |
| **1000 cenários** | **2056 ms** | **~15 ms** | **137:1** | **6.21x ↑** | ✅ **Muito Bom** |

**Regra descoberta:** Trabalho útil deve ser > **20x** o overhead para ter ganho

**Solução adotada:** 
- ❌ Evitar: paralelização de população.
- ✅ Usar: cenários paralelos.

---

### Lição Principal: Grão-Fino vs Grão-Grosso

| Aspecto | Grão-Fino (População) | Grão-Grosso (Cenários) |
|---------|----------------------|------------------------|
| Trabalho/Thread | 0.5-4 ms | 257-2056 ms |
| Overhead | 2-7 ms | ~15 ms |
| Razão | Overhead 1.75-14x > trabalho | Trabalho 17-137x > overhead |
| Speedup | 1.08-2.90x ↓ (PERDA!) | 3.42-7.63x ↑ (GANHO!) |
| Eficiência | -8% a -48% | 43-95% |
| Desvio Padrão | Alto (±4-15ms) | Baixo (±2-5ms) |
| **Conclusão** | ❌ NÃO VALE A PENA | ✅ EXCELENTE |

**Regra crítica:** Para paralelização valer a pena:
```
Trabalho_útil_por_thread >> Overhead_paralelização

✅ Cenários (500): 1311ms / 8 threads = 164ms >> 15ms overhead (razão 11:1)
❌ População (8t): 3.9ms / 8 threads = 0.5ms << 6ms overhead (razão 1:12)
```

**Lei de Amdahl aplicada:**
- População: Fração paralelizável baixa (trabalho real < 40%)
- Cenários: Fração paralelizável alta (trabalho real > 90%)

---

## 5. Arquitetura do Projeto

### Estrutura de Diretórios

```
Projeto Final/
├── SIR/java/                    
│   ├── SIRSequencial.java       
│   ├── SIRParalelo.java        
│   ├── cenarios/                
│   └── distribuido/             
├── SIS/java/                    
├── benchmarks/                  
│   ├── Benchmarks.java          
│   └── BenchmarksDistribuidoCompleto.java
├── scripts_analise/            
│   ├── analisar_resultados_interativo.py
│   ├── analisar_resultados_distribuido_completo.py
│   └── gerar_index_unificado.py
├── build/                      
├── datos/                       
│   ├── resultados_benchmark.csv
│   └── resultados_benchmark_distribuido_completo.csv
├── graficos/                    
│   ├── index_graficos.html      
│   └── grafico_*.html           
└── executar.ps1                
```

### Tecnologias Utilizadas

**Java 8+:**
- `ExecutorService` + `CountDownLatch` (paralelização)
- `java.rmi.*` (computação distribuída)
- Streams para processamento de dados

**Python 3.x:**
- `pandas`: Análise de dados
- `plotly`: Gráficos interativos HTML
- Scripts automatizados de geração

**PowerShell 5.1:**
- Automação de compilação
- Execução sequencial de todas as etapas
- Abertura automática de resultados

---

## 6. Conclusões

### Objetivos Alcançados
- Implementação completa: 2 modelos × 4 abordagens (8 classes principais)  
- Análise profunda: **paralelização não é sempre benéfica**  
- Solução eficiente: **cenários paralelos (até 7.63x speedup)**  
- Framework completo: **script centralizado + build organizado**  
- ~2.160 testes executados automaticamente (15 repetições cada)  
- 15 gráficos interativos com análise detalhada  
- Compreensão de granularidade e overhead  
- Documentação extensa (5 arquivos README)  

### Principais Aprendizados
**1. Nem toda computação deve ser paralelizada**
- Overhead pode superar o ganho (população: 2.6x mais lento)
- Cenários: trabalho 17-137x > overhead = sucesso
- Medição é essencial para decisões informadas

**2. Granularidade é CRÍTICA**
- Grão-fino (< 5ms/thread): overhead domina
- Grão-médio (5-100ms/thread): depende do contexto
- Grão-grosso (> 100ms/thread): paralelização vale a pena

**3. Trade-offs devem ser medidos, não assumidos**
- População parecia boa ideia: falhou nos testes
- Cenários não eram óbvios: provaram-se excelentes
- Dados reais (15 repetições) revelaram a verdade

**4. Flexibilidade na solução é essencial**
- Estratégia inicial (população) não escalou
- Pivotamos para cenários com sucesso
- Resultado: speedup real de até 7.63x


---

## Principais Gráficos


## Contribuições Individuais
- **Análise do problema e de possíveis soluções:** Todos;
- **Codificação da Solução** *Leonardo Silva e Cruz* e *Pedro Paulo Valente Bittencourt*. Vale ressaltar que a parte de "perfumaria", geração de gráficos, salvamento de dados em .csv, execução automática via código powershell foi feita pelo *GitHub Copilot*, sendo que o mesmo foi responsável pela solução sequencial inicial;
- **Documentação do Projeto e Análise dos Testes:** *Lucas Francisco Alves Costa* e *Isabella Pires da Silva*. Vale ressaltar que também houve auxílio do *GitHub Copilot*;