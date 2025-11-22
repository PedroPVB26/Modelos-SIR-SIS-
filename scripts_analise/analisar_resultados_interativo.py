import pandas as pd
import plotly.graph_objects as go
from plotly.subplots import make_subplots
import webbrowser
import os

print("="*80)
print("ANÁLISE INTERATIVA DOS RESULTADOS - BENCHMARKS")
print("="*80)

# Leitura do CSV
# Tenta primeiro o CSV unificado, depois o antigo
import os
if os.path.exists('../dados/resultados_benchmark_completo.csv'):
    df = pd.read_csv('../dados/resultados_benchmark_completo.csv')
    print("Usando arquivo: resultados_benchmark_completo.csv")
else:
    df = pd.read_csv('../dados/resultados_benchmark.csv')
    print("Usando arquivo: resultados_benchmark.csv")

# Calcula média e desvio padrão por grupo
df_agrupado = df.groupby(['Modelo', 'Tipo', 'Populacao', 'Passos', 'Cenarios', 'Threads']).agg({
    'Tempo_ms': ['mean', 'std', 'min', 'max']
}).reset_index()

df_agrupado.columns = ['Modelo', 'Tipo', 'Populacao', 'Passos', 'Cenarios', 'Threads', 
                        'Tempo_medio', 'Tempo_std', 'Tempo_min', 'Tempo_max']

print(f"\nTotal de testes realizados: {len(df)}")
print(f"Configurações testadas: {len(df_agrupado)}")

# =============================================================================
# GRÁFICO 1: SIR - Tempo × População (fixando 50.000 passos)
# =============================================================================
fig1 = go.Figure()

sir_pop = df_agrupado[(df_agrupado['Modelo'] == 'SIR') & 
                      (df_agrupado['Cenarios'] == 0) & 
                      (df_agrupado['Passos'] == 50000)]

# Adiciona sequencial
dados_seq = sir_pop[sir_pop['Tipo'] == 'Sequencial']
fig1.add_trace(go.Scatter(
    x=dados_seq['Populacao'],
    y=dados_seq['Tempo_medio'],
    mode='lines+markers',
    name='Sequencial',
    error_y=dict(type='data', array=dados_seq['Tempo_std']),
    hovertemplate='<b>Sequencial</b><br>' +
                  'População: %{x:,.0f}<br>' +
                  'Tempo médio: %{y:.2f} ms<br>' +
                  'Desvio padrão: %{error_y.array:.2f} ms<br>' +
                  '<extra></extra>'
))

# Adiciona paralelo separado por número de threads
cores_threads = {1: '#FF6B6B', 2: '#4ECDC4', 4: '#45B7D1', 8: '#FFA07A'}
for n_threads in [1, 2, 4, 8]:
    dados_par = sir_pop[(sir_pop['Tipo'] == 'Paralelo') & (sir_pop['Threads'] == n_threads)]
    if not dados_par.empty:
        fig1.add_trace(go.Scatter(
            x=dados_par['Populacao'],
            y=dados_par['Tempo_medio'],
            mode='lines+markers',
            name=f'Paralelo ({n_threads} thread{"s" if n_threads > 1 else ""})',
            line=dict(color=cores_threads[n_threads]),
            error_y=dict(type='data', array=dados_par['Tempo_std']),
            hovertemplate=f'<b>Paralelo ({n_threads} thread{"s" if n_threads > 1 else ""})</b><br>' +
                          'População: %{x:,.0f}<br>' +
                          'Tempo médio: %{y:.2f} ms<br>' +
                          'Desvio padrão: %{error_y.array:.2f} ms<br>' +
                          '<extra></extra>'
        ))

fig1.update_layout(
    title='SIR: Tempo de Execução × Tamanho da População<br><sub>Passos fixos: 50.000</sub>',
    xaxis_title='Tamanho da População',
    yaxis_title='Tempo de Execução (ms)',
    hovermode='closest',
    template='plotly_white',
    font=dict(size=12)
)

# =============================================================================
# GRÁFICO 2: SIR - Tempo × Número de Passos (fixando 100.000 população)
# =============================================================================
fig2 = go.Figure()

sir_passos = df_agrupado[(df_agrupado['Modelo'] == 'SIR') & 
                         (df_agrupado['Cenarios'] == 0) & 
                         (df_agrupado['Populacao'] == 100000)]

# Adiciona sequencial
dados_seq = sir_passos[sir_passos['Tipo'] == 'Sequencial']
fig2.add_trace(go.Scatter(
    x=dados_seq['Passos'],
    y=dados_seq['Tempo_medio'],
    mode='lines+markers',
    name='Sequencial',
    error_y=dict(type='data', array=dados_seq['Tempo_std']),
    hovertemplate='<b>Sequencial</b><br>' +
                  'Passos: %{x:,.0f}<br>' +
                  'Tempo médio: %{y:.2f} ms<br>' +
                  'Desvio padrão: %{error_y.array:.2f} ms<br>' +
                  '<extra></extra>'
))

# Adiciona paralelo separado por número de threads
for n_threads in [1, 2, 4, 8]:
    dados_par = sir_passos[(sir_passos['Tipo'] == 'Paralelo') & (sir_passos['Threads'] == n_threads)]
    if not dados_par.empty:
        fig2.add_trace(go.Scatter(
            x=dados_par['Passos'],
            y=dados_par['Tempo_medio'],
            mode='lines+markers',
            name=f'Paralelo ({n_threads} thread{"s" if n_threads > 1 else ""})',
            error_y=dict(type='data', array=dados_par['Tempo_std']),
            hovertemplate=f'<b>Paralelo ({n_threads} thread{"s" if n_threads > 1 else ""})</b><br>' +
                          'Passos: %{x:,.0f}<br>' +
                          'Tempo médio: %{y:.2f} ms<br>' +
                          'Desvio padrão: %{error_y.array:.2f} ms<br>' +
                          '<extra></extra>'
        ))

fig2.update_layout(
    title='SIR: Tempo de Execução × Número de Passos<br><sub>População fixa: 100.000</sub>',
    xaxis_title='Número de Passos',
    yaxis_title='Tempo de Execução (ms)',
    hovermode='closest',
    template='plotly_white',
    font=dict(size=12)
)

# =============================================================================
# GRÁFICO 3: SIR - Speedup × População
# =============================================================================
fig3 = go.Figure()

# Calcula speedup para cada número de threads
for n_threads in [1, 2, 4, 8]:
    speedup_data = []
    for pop in sir_pop['Populacao'].unique():
        seq = sir_pop[(sir_pop['Tipo'] == 'Sequencial') & (sir_pop['Populacao'] == pop)]['Tempo_medio'].values
        par = sir_pop[(sir_pop['Tipo'] == 'Paralelo') & (sir_pop['Threads'] == n_threads) & (sir_pop['Populacao'] == pop)]['Tempo_medio'].values
        if len(seq) > 0 and len(par) > 0:
            speedup_data.append({'Populacao': pop, 'Speedup': seq[0] / par[0]})
    
    if speedup_data:
        speedup_df = pd.DataFrame(speedup_data)
        fig3.add_trace(go.Scatter(
            x=speedup_df['Populacao'],
            y=speedup_df['Speedup'],
            mode='lines+markers',
            name=f'{n_threads} thread{"s" if n_threads > 1 else ""}',
            hovertemplate=f'<b>{n_threads} thread{"s" if n_threads > 1 else ""}</b><br>' +
                          'População: %{x:,.0f}<br>' +
                          'Speedup: %{y:.3f}x<br>' +
                          '<extra></extra>'
        ))

# Linhas de referência: speedup ideal
if speedup_data:
    pops = sorted(sir_pop['Populacao'].unique())
    for n_threads in [2, 4, 8]:
        fig3.add_trace(go.Scatter(
            x=pops,
            y=[n_threads] * len(pops),
            mode='lines',
            name=f'Ideal {n_threads}x',
            line=dict(dash='dash'),
            hovertemplate=f'<b>Ideal {n_threads}x</b><br>' +
                          'População: %{x:,.0f}<br>' +
                          f'Speedup: {n_threads}.0x<br>' +
                          '<extra></extra>'
        ))

fig3.update_layout(
    title='SIR: Speedup Paralelo vs Sequencial × População<br><sub>Passos fixos: 50.000</sub>',
    xaxis_title='Tamanho da População',
    yaxis_title='Speedup (vezes mais rápido)',
    hovermode='closest',
    template='plotly_white',
    font=dict(size=12)
)

# =============================================================================
# GRÁFICO 4: SIR Cenários - Tempo × Número de Cenários
# =============================================================================
fig4 = go.Figure()

sir_cenarios = df_agrupado[(df_agrupado['Modelo'] == 'SIR') & 
                           (df_agrupado['Tipo'].str.contains('Cenarios'))]

for tipo in ['Cenarios_Sequencial', 'Cenarios_Paralelo']:
    dados = sir_cenarios[sir_cenarios['Tipo'] == tipo]
    nome = tipo.replace('Cenarios_', '')
    fig4.add_trace(go.Scatter(
        x=dados['Cenarios'],
        y=dados['Tempo_medio'],
        mode='lines+markers',
        name=nome,
        error_y=dict(type='data', array=dados['Tempo_std']),
        hovertemplate='<b>%{fullData.name}</b><br>' +
                      'Cenários: %{x:,.0f}<br>' +
                      'Tempo médio: %{y:.2f} ms<br>' +
                      'Desvio padrão: %{error_y.array:.2f} ms<br>' +
                      '<extra></extra>'
    ))

fig4.update_layout(
    title='SIR Cenários: Tempo de Execução × Número de Cenários<br><sub>População: 1.000.000, Passos: 50.000</sub>',
    xaxis_title='Número de Cenários',
    yaxis_title='Tempo de Execução (ms)',
    hovermode='closest',
    template='plotly_white',
    font=dict(size=12)
)

# =============================================================================
# GRÁFICO 5: SIR Cenários - Speedup e Eficiência
# =============================================================================
fig5 = make_subplots(
    rows=1, cols=2,
    subplot_titles=('Speedup × Número de Cenários', 'Eficiência × Número de Cenários'),
    specs=[[{"secondary_y": False}, {"secondary_y": False}]]
)

speedup_cenarios = []
for cen in sir_cenarios['Cenarios'].unique():
    seq = sir_cenarios[(sir_cenarios['Tipo'] == 'Cenarios_Sequencial') & 
                       (sir_cenarios['Cenarios'] == cen)]['Tempo_medio'].values
    par = sir_cenarios[(sir_cenarios['Tipo'] == 'Cenarios_Paralelo') & 
                       (sir_cenarios['Cenarios'] == cen)]['Tempo_medio'].values
    if len(seq) > 0 and len(par) > 0:
        speedup = seq[0] / par[0]
        eficiencia = (speedup / 8) * 100
        speedup_cenarios.append({
            'Cenarios': cen, 
            'Speedup': speedup,
            'Eficiencia': eficiencia
        })

speedup_cen_df = pd.DataFrame(speedup_cenarios)

# Speedup
fig5.add_trace(go.Scatter(
    x=speedup_cen_df['Cenarios'],
    y=speedup_cen_df['Speedup'],
    mode='lines+markers',
    name='Speedup Real',
    line=dict(color='blue', width=3),
    marker=dict(size=10),
    hovertemplate='<b>Speedup</b><br>' +
                  'Cenários: %{x:,.0f}<br>' +
                  'Speedup: %{y:.2f}x<br>' +
                  '<extra></extra>'
), row=1, col=1)

fig5.add_trace(go.Scatter(
    x=speedup_cen_df['Cenarios'],
    y=[8] * len(speedup_cen_df),
    mode='lines',
    name='Ideal (8x)',
    line=dict(color='red', dash='dash'),
    showlegend=False,
    hovertemplate='<b>Ideal</b><br>' +
                  'Cenários: %{x:,.0f}<br>' +
                  'Speedup: 8.0x<br>' +
                  '<extra></extra>'
), row=1, col=1)

# Eficiência
fig5.add_trace(go.Scatter(
    x=speedup_cen_df['Cenarios'],
    y=speedup_cen_df['Eficiencia'],
    mode='lines+markers',
    name='Eficiência Real',
    line=dict(color='green', width=3),
    marker=dict(size=10),
    hovertemplate='<b>Eficiência</b><br>' +
                  'Cenários: %{x:,.0f}<br>' +
                  'Eficiência: %{y:.1f}%<br>' +
                  '<extra></extra>'
), row=1, col=2)

fig5.add_trace(go.Scatter(
    x=speedup_cen_df['Cenarios'],
    y=[100] * len(speedup_cen_df),
    mode='lines',
    name='Ideal (100%)',
    line=dict(color='red', dash='dash'),
    showlegend=False,
    hovertemplate='<b>Ideal</b><br>' +
                  'Cenários: %{x:,.0f}<br>' +
                  'Eficiência: 100%<br>' +
                  '<extra></extra>'
), row=1, col=2)

fig5.update_xaxes(title_text="Número de Cenários", row=1, col=1)
fig5.update_xaxes(title_text="Número de Cenários", row=1, col=2)
fig5.update_yaxes(title_text="Speedup (x)", row=1, col=1)
fig5.update_yaxes(title_text="Eficiência (%)", row=1, col=2)

fig5.update_layout(
    title_text='SIR Cenários: Análise de Performance Paralela',
    hovermode='closest',
    template='plotly_white',
    height=500,
    font=dict(size=12)
)

# =============================================================================
# GRÁFICO 6: SIS - Tempo × População
# =============================================================================
fig6 = go.Figure()

sis_pop = df_agrupado[(df_agrupado['Modelo'] == 'SIS') & 
                      (df_agrupado['Cenarios'] == 0) & 
                      (df_agrupado['Passos'] == 50000)]

# Adiciona sequencial
dados_seq = sis_pop[sis_pop['Tipo'] == 'Sequencial']
fig6.add_trace(go.Scatter(
    x=dados_seq['Populacao'],
    y=dados_seq['Tempo_medio'],
    mode='lines+markers',
    name='Sequencial',
    error_y=dict(type='data', array=dados_seq['Tempo_std']),
    hovertemplate='<b>Sequencial</b><br>' +
                  'População: %{x:,.0f}<br>' +
                  'Tempo médio: %{y:.2f} ms<br>' +
                  'Desvio padrão: %{error_y.array:.2f} ms<br>' +
                  '<extra></extra>'
))

# Adiciona paralelo separado por número de threads
for n_threads in [1, 2, 4, 8]:
    dados_par = sis_pop[(sis_pop['Tipo'] == 'Paralelo') & (sis_pop['Threads'] == n_threads)]
    if not dados_par.empty:
        fig6.add_trace(go.Scatter(
            x=dados_par['Populacao'],
            y=dados_par['Tempo_medio'],
            mode='lines+markers',
            name=f'Paralelo ({n_threads} thread{"s" if n_threads > 1 else ""})',
            error_y=dict(type='data', array=dados_par['Tempo_std']),
            hovertemplate=f'<b>Paralelo ({n_threads} thread{"s" if n_threads > 1 else ""})</b><br>' +
                          'População: %{x:,.0f}<br>' +
                          'Tempo médio: %{y:.2f} ms<br>' +
                          'Desvio padrão: %{error_y.array:.2f} ms<br>' +
                          '<extra></extra>'
        ))

fig6.update_layout(
    title='SIS: Tempo de Execução × Tamanho da População<br><sub>Passos fixos: 50.000</sub>',
    xaxis_title='Tamanho da População',
    yaxis_title='Tempo de Execução (ms)',
    hovermode='closest',
    template='plotly_white',
    font=dict(size=12)
)

# =============================================================================
# GRÁFICO 7: SIS - Tempo × Número de Passos (fixando 100.000 população)
# =============================================================================
fig7 = go.Figure()

sis_passos = df_agrupado[(df_agrupado['Modelo'] == 'SIS') & 
                         (df_agrupado['Cenarios'] == 0) & 
                         (df_agrupado['Populacao'] == 100000)]

# Adiciona sequencial
dados_seq = sis_passos[sis_passos['Tipo'] == 'Sequencial']
fig7.add_trace(go.Scatter(
    x=dados_seq['Passos'],
    y=dados_seq['Tempo_medio'],
    mode='lines+markers',
    name='Sequencial',
    error_y=dict(type='data', array=dados_seq['Tempo_std']),
    hovertemplate='<b>Sequencial</b><br>' +
                  'Passos: %{x:,.0f}<br>' +
                  'Tempo médio: %{y:.2f} ms<br>' +
                  'Desvio padrão: %{error_y.array:.2f} ms<br>' +
                  '<extra></extra>'
))

# Adiciona paralelo separado por número de threads
for n_threads in [1, 2, 4, 8]:
    dados_par = sis_passos[(sis_passos['Tipo'] == 'Paralelo') & (sis_passos['Threads'] == n_threads)]
    if not dados_par.empty:
        fig7.add_trace(go.Scatter(
            x=dados_par['Passos'],
            y=dados_par['Tempo_medio'],
            mode='lines+markers',
            name=f'Paralelo ({n_threads} thread{"s" if n_threads > 1 else ""})',
            error_y=dict(type='data', array=dados_par['Tempo_std']),
            hovertemplate=f'<b>Paralelo ({n_threads} thread{"s" if n_threads > 1 else ""})</b><br>' +
                          'Passos: %{x:,.0f}<br>' +
                          'Tempo médio: %{y:.2f} ms<br>' +
                          'Desvio padrão: %{error_y.array:.2f} ms<br>' +
                          '<extra></extra>'
        ))

fig7.update_layout(
    title='SIS: Tempo de Execução × Número de Passos<br><sub>População fixa: 100.000</sub>',
    xaxis_title='Número de Passos',
    yaxis_title='Tempo de Execução (ms)',
    hovermode='closest',
    template='plotly_white',
    font=dict(size=12)
)

# =============================================================================
# GRÁFICO 8: SIS - Speedup × População
# =============================================================================
fig8 = go.Figure()

sis_pop_speedup = df_agrupado[(df_agrupado['Modelo'] == 'SIS') & 
                              (df_agrupado['Cenarios'] == 0) & 
                              (df_agrupado['Passos'] == 50000)]

# Calcula speedup para cada número de threads
for n_threads in [1, 2, 4, 8]:
    speedup_sis_data = []
    for pop in sis_pop_speedup['Populacao'].unique():
        seq = sis_pop_speedup[(sis_pop_speedup['Tipo'] == 'Sequencial') & (sis_pop_speedup['Populacao'] == pop)]['Tempo_medio'].values
        par = sis_pop_speedup[(sis_pop_speedup['Tipo'] == 'Paralelo') & (sis_pop_speedup['Threads'] == n_threads) & (sis_pop_speedup['Populacao'] == pop)]['Tempo_medio'].values
        if len(seq) > 0 and len(par) > 0:
            speedup_sis_data.append({'Populacao': pop, 'Speedup': seq[0] / par[0]})
    
    if speedup_sis_data:
        speedup_sis_df = pd.DataFrame(speedup_sis_data)
        fig8.add_trace(go.Scatter(
            x=speedup_sis_df['Populacao'],
            y=speedup_sis_df['Speedup'],
            mode='lines+markers',
            name=f'{n_threads} thread{"s" if n_threads > 1 else ""}',
            hovertemplate=f'<b>{n_threads} thread{"s" if n_threads > 1 else ""}</b><br>' +
                          'População: %{x:,.0f}<br>' +
                          'Speedup: %{y:.3f}x<br>' +
                          '<extra></extra>'
        ))

# Linhas de referência: speedup ideal
if speedup_sis_data:
    pops = sorted(sis_pop_speedup['Populacao'].unique())
    for n_threads in [2, 4, 8]:
        fig8.add_trace(go.Scatter(
            x=pops,
            y=[n_threads] * len(pops),
            mode='lines',
            name=f'Ideal {n_threads}x',
            line=dict(dash='dash'),
            hovertemplate=f'<b>Ideal {n_threads}x</b><br>' +
                          'População: %{x:,.0f}<br>' +
                          f'Speedup: {n_threads}.0x<br>' +
                          '<extra></extra>'
        ))

fig8.update_layout(
    title='SIS: Speedup Paralelo vs Sequencial × População<br><sub>Passos fixos: 50.000</sub>',
    xaxis_title='Tamanho da População',
    yaxis_title='Speedup (vezes mais rápido)',
    hovermode='closest',
    template='plotly_white',
    font=dict(size=12)
)

# =============================================================================
# GRÁFICO 9: SIS Cenários - Tempo × Número de Cenários
# =============================================================================
fig9 = go.Figure()

sis_cenarios = df_agrupado[(df_agrupado['Modelo'] == 'SIS') & 
                           (df_agrupado['Tipo'].str.contains('Cenarios'))]

for tipo in ['Cenarios_Sequencial', 'Cenarios_Paralelo']:
    dados = sis_cenarios[sis_cenarios['Tipo'] == tipo]
    nome = tipo.replace('Cenarios_', '')
    fig9.add_trace(go.Scatter(
        x=dados['Cenarios'],
        y=dados['Tempo_medio'],
        mode='lines+markers',
        name=nome,
        error_y=dict(type='data', array=dados['Tempo_std']),
        hovertemplate='<b>%{fullData.name}</b><br>' +
                      'Cenários: %{x:,.0f}<br>' +
                      'Tempo médio: %{y:.2f} ms<br>' +
                      'Desvio padrão: %{error_y.array:.2f} ms<br>' +
                      '<extra></extra>'
    ))

fig9.update_layout(
    title='SIS Cenários: Tempo de Execução × Número de Cenários<br><sub>População fixa: 1.000.000</sub>',
    xaxis_title='Número de Cenários',
    yaxis_title='Tempo de Execução (ms)',
    hovermode='closest',
    template='plotly_white',
    font=dict(size=12)
)

# =============================================================================
# GRÁFICO 10: SIS Cenários - Speedup e Eficiência
# =============================================================================
fig10 = make_subplots(
    rows=1, cols=2,
    subplot_titles=('Speedup × Número de Cenários', 'Eficiência × Número de Cenários'),
    specs=[[{"secondary_y": False}, {"secondary_y": False}]]
)

speedup_sis_cenarios = []
for cen in sis_cenarios['Cenarios'].unique():
    seq = sis_cenarios[(sis_cenarios['Tipo'] == 'Cenarios_Sequencial') & 
                       (sis_cenarios['Cenarios'] == cen)]['Tempo_medio'].values
    par = sis_cenarios[(sis_cenarios['Tipo'] == 'Cenarios_Paralelo') & 
                       (sis_cenarios['Cenarios'] == cen)]['Tempo_medio'].values
    if len(seq) > 0 and len(par) > 0:
        speedup = seq[0] / par[0]
        eficiencia = (speedup / 8) * 100
        speedup_sis_cenarios.append({
            'Cenarios': cen, 
            'Speedup': speedup,
            'Eficiencia': eficiencia
        })

speedup_sis_cen_df = pd.DataFrame(speedup_sis_cenarios)

# Speedup
fig10.add_trace(go.Scatter(
    x=speedup_sis_cen_df['Cenarios'],
    y=speedup_sis_cen_df['Speedup'],
    mode='lines+markers',
    name='Speedup Real',
    line=dict(color='blue', width=3),
    marker=dict(size=10),
    hovertemplate='<b>Speedup</b><br>' +
                  'Cenários: %{x:,.0f}<br>' +
                  'Speedup: %{y:.2f}x<br>' +
                  '<extra></extra>'
), row=1, col=1)

fig10.add_trace(go.Scatter(
    x=speedup_sis_cen_df['Cenarios'],
    y=[8] * len(speedup_sis_cen_df),
    mode='lines',
    name='Ideal (8x)',
    line=dict(color='red', dash='dash'),
    showlegend=False,
    hovertemplate='<b>Ideal</b><br>' +
                  'Cenários: %{x:,.0f}<br>' +
                  'Speedup: 8.0x<br>' +
                  '<extra></extra>'
), row=1, col=1)

# Eficiência
fig10.add_trace(go.Scatter(
    x=speedup_sis_cen_df['Cenarios'],
    y=speedup_sis_cen_df['Eficiencia'],
    mode='lines+markers',
    name='Eficiência Real',
    line=dict(color='green', width=3),
    marker=dict(size=10),
    hovertemplate='<b>Eficiência</b><br>' +
                  'Cenários: %{x:,.0f}<br>' +
                  'Eficiência: %{y:.1f}%<br>' +
                  '<extra></extra>'
), row=1, col=2)

fig10.add_trace(go.Scatter(
    x=speedup_sis_cen_df['Cenarios'],
    y=[100] * len(speedup_sis_cen_df),
    mode='lines',
    name='Ideal (100%)',
    line=dict(color='red', dash='dash'),
    showlegend=False,
    hovertemplate='<b>Ideal</b><br>' +
                  'Cenários: %{x:,.0f}<br>' +
                  'Eficiência: 100%<br>' +
                  '<extra></extra>'
), row=1, col=2)

fig10.update_xaxes(title_text="Número de Cenários", row=1, col=1)
fig10.update_xaxes(title_text="Número de Cenários", row=1, col=2)
fig10.update_yaxes(title_text="Speedup (x)", row=1, col=1)
fig10.update_yaxes(title_text="Eficiência (%)", row=1, col=2)

fig10.update_layout(
    title_text='SIS Cenários: Análise de Performance Paralela',
    hovermode='closest',
    template='plotly_white',
    height=500,
    font=dict(size=12)
)

# =============================================================================
# Salvar todos os gráficos em HTML
# =============================================================================
html_files = []

fig1.write_html('../graficos/grafico_sir_tempo_populacao.html')
print("✓ Gráfico 1 salvo: grafico_sir_tempo_populacao.html")

fig2.write_html('../graficos/grafico_sir_tempo_passos.html')
print("✓ Gráfico 2 salvo: grafico_sir_tempo_passos.html")

fig3.write_html('../graficos/grafico_sir_speedup_populacao.html')
print("✓ Gráfico 3 salvo: grafico_sir_speedup_populacao.html")

fig4.write_html('../graficos/grafico_sir_tempo_cenarios.html')
print("✓ Gráfico 4 salvo: grafico_sir_tempo_cenarios.html")

fig5.write_html('../graficos/grafico_sir_speedup_eficiencia_cenarios.html')
print("✓ Gráfico 5 salvo: grafico_sir_speedup_eficiencia_cenarios.html")

fig6.write_html('../graficos/grafico_sis_tempo_populacao.html')
print("✓ Gráfico 6 salvo: grafico_sis_tempo_populacao.html")

fig7.write_html('../graficos/grafico_sis_tempo_passos.html')
print("✓ Gráfico 7 salvo: grafico_sis_tempo_passos.html")

fig8.write_html('../graficos/grafico_sis_speedup_populacao.html')
print("✓ Gráfico 8 salvo: grafico_sis_speedup_populacao.html")

fig9.write_html('../graficos/grafico_sis_tempo_cenarios.html')
print("✓ Gráfico 9 salvo: grafico_sis_tempo_cenarios.html")

fig10.write_html('../graficos/grafico_sis_speedup_eficiencia_cenarios.html')
print("✓ Gráfico 10 salvo: grafico_sis_speedup_eficiencia_cenarios.html")

print("\n" + "="*80)
print("✓ Gráficos de benchmarks normais gerados com sucesso!")
print("="*80)
