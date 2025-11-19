import pandas as pd
import matplotlib.pyplot as plt

# Configuração de estilo
plt.style.use('ggplot')

# Leitura do CSV com formato correto
df = pd.read_csv('../dados/resultados_benchmark.csv')

# Calcula média e desvio padrão por grupo
df_agrupado = df.groupby(['Modelo', 'Tipo', 'Populacao', 'Passos', 'Cenarios', 'Threads']).agg({
    'Tempo_ms': ['mean', 'std', 'min', 'max']
}).reset_index()

df_agrupado.columns = ['Modelo', 'Tipo', 'Populacao', 'Passos', 'Cenarios', 'Threads', 
                        'Tempo_medio', 'Tempo_std', 'Tempo_min', 'Tempo_max']

print("=" * 80)
print("ANÁLISE DOS RESULTADOS - BENCHMARKS")
print("=" * 80)
print(f"\nTotal de testes realizados: {len(df)}")
print(f"Configurações testadas: {len(df_agrupado)}")
print("\nPrimeiras linhas do dataset:")
print(df_agrupado.head(10))

# ============================================================================
# GRÁFICO 1: Tempo × População (SIR Sequencial vs Paralelo)
# ============================================================================
fig, ax = plt.subplots(figsize=(12, 6))

sir_seq_pop = df_agrupado[(df_agrupado['Modelo'] == 'SIR') & 
                          (df_agrupado['Tipo'] == 'Sequencial') &
                          (df_agrupado['Cenarios'] == 0) &
                          (df_agrupado['Passos'] == 50000)]

sir_par_pop = df_agrupado[(df_agrupado['Modelo'] == 'SIR') & 
                          (df_agrupado['Tipo'] == 'Paralelo') &
                          (df_agrupado['Cenarios'] == 0) &
                          (df_agrupado['Passos'] == 50000)]

if not sir_seq_pop.empty and not sir_par_pop.empty:
    ax.errorbar(sir_seq_pop['Populacao'], sir_seq_pop['Tempo_medio'], 
                yerr=sir_seq_pop['Tempo_std'], marker='o', linewidth=2, 
                capsize=5, label='Sequencial', markersize=8)
    
    ax.errorbar(sir_par_pop['Populacao'], sir_par_pop['Tempo_medio'], 
                yerr=sir_par_pop['Tempo_std'], marker='s', linewidth=2, 
                capsize=5, label='Paralelo', markersize=8)
    
    ax.set_xlabel('Tamanho da População', fontsize=12, fontweight='bold')
    ax.set_ylabel('Tempo de Execução (ms)', fontsize=12, fontweight='bold')
    ax.set_title('SIR: Tempo × População (50.000 passos)', fontsize=14, fontweight='bold')
    ax.legend(fontsize=11)
    ax.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig('grafico_sir_tempo_populacao.png', dpi=300, bbox_inches='tight')
    print("\n✓ Gráfico 1 salvo: grafico_sir_tempo_populacao.png")
    plt.close()

# ============================================================================
# GRÁFICO 2: Tempo × Número de Passos (SIR)
# ============================================================================
fig, ax = plt.subplots(figsize=(12, 6))

sir_seq_passos = df_agrupado[(df_agrupado['Modelo'] == 'SIR') & 
                             (df_agrupado['Tipo'] == 'Sequencial') &
                             (df_agrupado['Cenarios'] == 0) &
                             (df_agrupado['Populacao'] == 1000000)]

sir_par_passos = df_agrupado[(df_agrupado['Modelo'] == 'SIR') & 
                             (df_agrupado['Tipo'] == 'Paralelo') &
                             (df_agrupado['Cenarios'] == 0) &
                             (df_agrupado['Populacao'] == 1000000)]

if not sir_seq_passos.empty and not sir_par_passos.empty:
    ax.errorbar(sir_seq_passos['Passos'], sir_seq_passos['Tempo_medio'], 
                yerr=sir_seq_passos['Tempo_std'], marker='o', linewidth=2, 
                capsize=5, label='Sequencial', markersize=8)
    
    ax.errorbar(sir_par_passos['Passos'], sir_par_passos['Tempo_medio'], 
                yerr=sir_par_passos['Tempo_std'], marker='s', linewidth=2, 
                capsize=5, label='Paralelo', markersize=8)
    
    ax.set_xlabel('Número de Passos', fontsize=12, fontweight='bold')
    ax.set_ylabel('Tempo de Execução (ms)', fontsize=12, fontweight='bold')
    ax.set_title('SIR: Tempo × Número de Passos (População = 1.000.000)', fontsize=14, fontweight='bold')
    ax.legend(fontsize=11)
    ax.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig('grafico_sir_tempo_passos.png', dpi=300, bbox_inches='tight')
    print("✓ Gráfico 2 salvo: grafico_sir_tempo_passos.png")
    plt.close()

# ============================================================================
# GRÁFICO 3: Speedup × Tamanho de População (SIR)
# ============================================================================
fig, ax = plt.subplots(figsize=(12, 6))

sir_speedup = pd.merge(
    sir_seq_pop[['Populacao', 'Tempo_medio']],
    sir_par_pop[['Populacao', 'Tempo_medio']],
    on='Populacao',
    suffixes=('_seq', '_par')
)

if not sir_speedup.empty:
    sir_speedup['Speedup'] = sir_speedup['Tempo_medio_seq'] / sir_speedup['Tempo_medio_par']
    
    ax.plot(sir_speedup['Populacao'], sir_speedup['Speedup'], 
            marker='o', linewidth=2, markersize=10, color='darkgreen')
    ax.axhline(y=1, color='red', linestyle='--', linewidth=2, label='Speedup = 1 (sem ganho)')
    
    ax.set_xlabel('Tamanho da População', fontsize=12, fontweight='bold')
    ax.set_ylabel('Speedup (Sequencial / Paralelo)', fontsize=12, fontweight='bold')
    ax.set_title('SIR: Speedup × População', fontsize=14, fontweight='bold')
    ax.legend(fontsize=11)
    ax.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig('grafico_sir_speedup_populacao.png', dpi=300, bbox_inches='tight')
    print("✓ Gráfico 3 salvo: grafico_sir_speedup_populacao.png")
    plt.close()

# ============================================================================
# GRÁFICO 4: Tempo × Número de Cenários (SIR)
# ============================================================================
fig, ax = plt.subplots(figsize=(12, 6))

sir_cen_seq = df_agrupado[(df_agrupado['Modelo'] == 'SIR') & 
                          (df_agrupado['Tipo'] == 'Cenarios_Sequencial') &
                          (df_agrupado['Cenarios'] > 0)]

sir_cen_par = df_agrupado[(df_agrupado['Modelo'] == 'SIR') & 
                          (df_agrupado['Tipo'] == 'Cenarios_Paralelo') &
                          (df_agrupado['Cenarios'] > 0)]

if not sir_cen_seq.empty and not sir_cen_par.empty:
    ax.errorbar(sir_cen_seq['Cenarios'], sir_cen_seq['Tempo_medio'], 
                yerr=sir_cen_seq['Tempo_std'], marker='o', linewidth=2, 
                capsize=5, label='Sequencial', markersize=8)
    
    ax.errorbar(sir_cen_par['Cenarios'], sir_cen_par['Tempo_medio'], 
                yerr=sir_cen_par['Tempo_std'], marker='s', linewidth=2, 
                capsize=5, label='Paralelo', markersize=8)
    
    ax.set_xlabel('Número de Cenários', fontsize=12, fontweight='bold')
    ax.set_ylabel('Tempo de Execução (ms)', fontsize=12, fontweight='bold')
    ax.set_title('SIR Cenários: Tempo × Número de Cenários', fontsize=14, fontweight='bold')
    ax.legend(fontsize=11)
    ax.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig('grafico_sir_tempo_cenarios.png', dpi=300, bbox_inches='tight')
    print("✓ Gráfico 4 salvo: grafico_sir_tempo_cenarios.png")
    plt.close()

# ============================================================================
# GRÁFICO 5: Speedup × Número de Cenários (SIR)
# ============================================================================
fig, ax = plt.subplots(figsize=(12, 6))

sir_cen_speedup = pd.merge(
    sir_cen_seq[['Cenarios', 'Tempo_medio']],
    sir_cen_par[['Cenarios', 'Tempo_medio']],
    on='Cenarios',
    suffixes=('_seq', '_par')
)

if not sir_cen_speedup.empty:
    sir_cen_speedup['Speedup'] = sir_cen_speedup['Tempo_medio_seq'] / sir_cen_speedup['Tempo_medio_par']
    
    ax.plot(sir_cen_speedup['Cenarios'], sir_cen_speedup['Speedup'], 
            marker='o', linewidth=2, markersize=10, color='darkblue')
    ax.axhline(y=1, color='red', linestyle='--', linewidth=2, label='Speedup = 1')
    
    # Adiciona anotação com o número de threads
    if not sir_cen_par.empty:
        threads = sir_cen_par['Threads'].iloc[0]
        ax.axhline(y=threads, color='orange', linestyle='--', linewidth=2, 
                   label=f'Speedup Linear ({threads} threads)')
    
    ax.set_xlabel('Número de Cenários', fontsize=12, fontweight='bold')
    ax.set_ylabel('Speedup (Sequencial / Paralelo)', fontsize=12, fontweight='bold')
    ax.set_title('SIR Cenários: Speedup × Número de Cenários', fontsize=14, fontweight='bold')
    ax.legend(fontsize=11)
    ax.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig('grafico_sir_speedup_cenarios.png', dpi=300, bbox_inches='tight')
    print("✓ Gráfico 5 salvo: grafico_sir_speedup_cenarios.png")
    plt.close()

# ============================================================================
# GRÁFICO 6: Comparação SIS - Tempo × População
# ============================================================================
fig, ax = plt.subplots(figsize=(12, 6))

sis_seq_pop = df_agrupado[(df_agrupado['Modelo'] == 'SIS') & 
                          (df_agrupado['Tipo'] == 'Sequencial') &
                          (df_agrupado['Cenarios'] == 0)]

sis_par_pop = df_agrupado[(df_agrupado['Modelo'] == 'SIS') & 
                          (df_agrupado['Tipo'] == 'Paralelo') &
                          (df_agrupado['Cenarios'] == 0)]

if not sis_seq_pop.empty and not sis_par_pop.empty:
    ax.errorbar(sis_seq_pop['Populacao'], sis_seq_pop['Tempo_medio'], 
                yerr=sis_seq_pop['Tempo_std'], marker='o', linewidth=2, 
                capsize=5, label='Sequencial', markersize=8)
    
    ax.errorbar(sis_par_pop['Populacao'], sis_par_pop['Tempo_medio'], 
                yerr=sis_par_pop['Tempo_std'], marker='s', linewidth=2, 
                capsize=5, label='Paralelo', markersize=8)
    
    ax.set_xlabel('Tamanho da População', fontsize=12, fontweight='bold')
    ax.set_ylabel('Tempo de Execução (ms)', fontsize=12, fontweight='bold')
    ax.set_title('SIS: Tempo × População', fontsize=14, fontweight='bold')
    ax.legend(fontsize=11)
    ax.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig('grafico_sis_tempo_populacao.png', dpi=300, bbox_inches='tight')
    print("✓ Gráfico 6 salvo: grafico_sis_tempo_populacao.png")
    plt.close()

# ============================================================================
# GRÁFICO 7: Eficiência Paralela (SIR Cenários)
# ============================================================================
if not sir_cen_speedup.empty and not sir_cen_par.empty:
    fig, ax = plt.subplots(figsize=(12, 6))
    
    threads = sir_cen_par['Threads'].iloc[0]
    sir_cen_speedup['Eficiencia'] = (sir_cen_speedup['Speedup'] / threads) * 100
    
    ax.plot(sir_cen_speedup['Cenarios'], sir_cen_speedup['Eficiencia'], 
            marker='o', linewidth=2, markersize=10, color='purple')
    ax.axhline(y=100, color='green', linestyle='--', linewidth=2, label='Eficiência Ideal (100%)')
    
    ax.set_xlabel('Número de Cenários', fontsize=12, fontweight='bold')
    ax.set_ylabel('Eficiência Paralela (%)', fontsize=12, fontweight='bold')
    ax.set_title(f'SIR Cenários: Eficiência Paralela ({threads} threads)', fontsize=14, fontweight='bold')
    ax.legend(fontsize=11)
    ax.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig('grafico_sir_eficiencia.png', dpi=300, bbox_inches='tight')
    print("✓ Gráfico 7 salvo: grafico_sir_eficiencia.png")
    plt.close()

# ============================================================================
# TABELA RESUMO
# ============================================================================
print("\n" + "=" * 80)
print("TABELA RESUMO - SPEEDUPS")
print("=" * 80)

print("\n--- SIR: Speedup por População (50.000 passos) ---")
if not sir_speedup.empty:
    print(sir_speedup[['Populacao', 'Speedup']].to_string(index=False))

print("\n--- SIR Cenários: Speedup por Número de Cenários ---")
if not sir_cen_speedup.empty:
    print(sir_cen_speedup[['Cenarios', 'Speedup', 'Eficiencia']].to_string(index=False))

print("\n" + "=" * 80)
print("✓ Todos os gráficos foram gerados com sucesso!")
print("=" * 80)
