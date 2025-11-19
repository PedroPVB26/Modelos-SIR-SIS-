# Centralização de Gráficos - Resumo das Mudanças

## O que foi feito?

Todos os gráficos (paralelos e distribuídos) foram **centralizados em uma única página HTML**.

## Mudanças Realizadas

### 1. **Página Índice Unificada**
- **Arquivo:** `graficos/index_graficos.html`
- **Conteúdo:** Todos os 15 gráficos em uma única página organizada
- **Design:** Interface moderna com gradientes, cards interativos e layout responsivo

### 2. **Scripts Python Atualizados**

#### `scripts_analise/analisar_resultados_interativo.py`
- ✓ Salva gráficos em `graficos/` (corrigido caminho duplicado)
- ✓ Removida criação de index separado
- ✓ Apenas gera os 10 gráficos principais (SIR e SIS)

#### `scripts_analise/analisar_resultados_distribuido_completo.py`
- ✓ Salva gráficos em `graficos/` (não mais em `scripts_analise/`)
- ✓ Removida criação de index separado
- ✓ Gera 5 gráficos distribuídos

#### `scripts_analise/gerar_index_unificado.py` **(NOVO)**
- ✓ Script dedicado para gerar a página índice unificada
- ✓ Executa ao final e abre o navegador automaticamente

### 3. **Script de Execução Atualizado**

#### `executar.ps1`
- ✓ Adicionada chamada para `gerar_index_unificado.py`
- ✓ Mensagem atualizada para indicar página única
- ✓ Resumo final mostra apenas `index_graficos.html`

## Estrutura Final

```
graficos/
├── index_graficos.html           ← PÁGINA PRINCIPAL ÚNICA
├── grafico_sir_tempo_populacao.html
├── grafico_sir_tempo_passos.html
├── grafico_sir_speedup_populacao.html
├── grafico_sir_tempo_cenarios.html
├── grafico_sir_speedup_eficiencia_cenarios.html
├── grafico_sis_tempo_populacao.html
├── grafico_sis_tempo_passos.html
├── grafico_sis_speedup_populacao.html
├── grafico_sis_tempo_cenarios.html
├── grafico_sis_speedup_eficiencia_cenarios.html
├── grafico_distribuido_sir_tempo_hosts.html
├── grafico_distribuido_sis_tempo_hosts.html
├── grafico_distribuido_comparacao_speedup.html
├── grafico_distribuido_comparacao_eficiencia.html
└── grafico_distribuido_throughput_comparativo.html
```

## Organização da Página Unificada

### Seções:
1. **Configuração dos Testes** - Informações sobre repetições, threads, população
2. **Como Usar** - Instruções interativas (zoom, hover, etc.)
3. **🚀 Benchmarks Paralelos**
   - 📈 Modelo SIR (5 gráficos)
   - 📉 Modelo SIS (5 gráficos)
4. **🌐 Benchmarks Distribuídos** (5 gráficos RMI)
5. **Interpretação dos Resultados** - Fórmulas e conceitos

## Como Usar

### Automático (Recomendado)
```powershell
.\executar.ps1
```
O navegador abrirá automaticamente com a página unificada.

### Manual
1. Execute os benchmarks
2. Gere os gráficos:
   ```powershell
   cd scripts_analise
   python analisar_resultados_interativo.py
   python analisar_resultados_distribuido_completo.py
   python gerar_index_unificado.py
   ```
3. Abra `graficos/index_graficos.html`

## Benefícios

✅ **Navegação simplificada** - Tudo em uma única página  
✅ **Design moderno** - Interface profissional com gradientes e animações  
✅ **Organização clara** - Separação por tipo de teste e modelo  
✅ **Manutenção facilitada** - Um único ponto de entrada  
✅ **Melhor experiência** - Sem necessidade de trocar entre múltiplas páginas

## Arquivos Removidos
- ❌ `graficos/index_graficos_distribuido_completo.html` (não é mais necessário)

## Compatibilidade
- ✓ Todos os gráficos individuais mantidos (podem ser abertos diretamente)
- ✓ Links relativos funcionam corretamente
- ✓ Scripts Python rodando de `scripts_analise/`
- ✓ Navegadores modernos (Chrome, Firefox, Edge, Safari)
