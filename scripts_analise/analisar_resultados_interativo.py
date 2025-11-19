import pandas as pd
import plotly.graph_objects as go
from plotly.subplots import make_subplots
import webbrowser
import os

print("="*80)
print("ANÁLISE INTERATIVA DOS RESULTADOS - BENCHMARKS")
print("="*80)

# Leitura do CSV
df = pd.read_csv('../dados/resultados_benchmark.csv')

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

for tipo in ['Sequencial', 'Paralelo']:
    dados = sir_pop[sir_pop['Tipo'] == tipo]
    fig1.add_trace(go.Scatter(
        x=dados['Populacao'],
        y=dados['Tempo_medio'],
        mode='lines+markers',
        name=tipo,
        error_y=dict(type='data', array=dados['Tempo_std']),
        hovertemplate='<b>%{fullData.name}</b><br>' +
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

for tipo in ['Sequencial', 'Paralelo']:
    dados = sir_passos[sir_passos['Tipo'] == tipo]
    fig2.add_trace(go.Scatter(
        x=dados['Passos'],
        y=dados['Tempo_medio'],
        mode='lines+markers',
        name=tipo,
        error_y=dict(type='data', array=dados['Tempo_std']),
        hovertemplate='<b>%{fullData.name}</b><br>' +
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

speedup_data = []
for pop in sir_pop['Populacao'].unique():
    seq = sir_pop[(sir_pop['Tipo'] == 'Sequencial') & (sir_pop['Populacao'] == pop)]['Tempo_medio'].values
    par = sir_pop[(sir_pop['Tipo'] == 'Paralelo') & (sir_pop['Populacao'] == pop)]['Tempo_medio'].values
    if len(seq) > 0 and len(par) > 0:
        speedup_data.append({'Populacao': pop, 'Speedup': seq[0] / par[0]})

speedup_df = pd.DataFrame(speedup_data)

fig3.add_trace(go.Scatter(
    x=speedup_df['Populacao'],
    y=speedup_df['Speedup'],
    mode='lines+markers',
    name='Speedup Real',
    line=dict(color='green', width=3),
    marker=dict(size=10),
    hovertemplate='<b>Speedup Real</b><br>' +
                  'População: %{x:,.0f}<br>' +
                  'Speedup: %{y:.3f}x<br>' +
                  '<extra></extra>'
))

# Linha de referência: speedup ideal (linear = 8x para 8 threads)
fig3.add_trace(go.Scatter(
    x=speedup_df['Populacao'],
    y=[8] * len(speedup_df),
    mode='lines',
    name='Speedup Ideal (8x)',
    line=dict(color='red', dash='dash'),
    hovertemplate='<b>Speedup Ideal</b><br>' +
                  'População: %{x:,.0f}<br>' +
                  'Speedup: 8.0x<br>' +
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
                      (df_agrupado['Passos'] == 1001)]

for tipo in ['Sequencial', 'Paralelo']:
    dados = sis_pop[sis_pop['Tipo'] == tipo]
    fig6.add_trace(go.Scatter(
        x=dados['Populacao'],
        y=dados['Tempo_medio'],
        mode='lines+markers',
        name=tipo,
        error_y=dict(type='data', array=dados['Tempo_std']),
        hovertemplate='<b>%{fullData.name}</b><br>' +
                      'População: %{x:,.0f}<br>' +
                      'Tempo médio: %{y:.2f} ms<br>' +
                      'Desvio padrão: %{error_y.array:.2f} ms<br>' +
                      '<extra></extra>'
    ))

fig6.update_layout(
    title='SIS: Tempo de Execução × Tamanho da População<br><sub>Passos fixos: 1.001</sub>',
    xaxis_title='Tamanho da População',
    yaxis_title='Tempo de Execução (ms)',
    hovermode='closest',
    template='plotly_white',
    font=dict(size=12)
)

# =============================================================================
# GRÁFICO 7: SIS - Tempo × Número de Passos (fixando 10.000 população)
# =============================================================================
fig7 = go.Figure()

sis_passos = df_agrupado[(df_agrupado['Modelo'] == 'SIS') & 
                         (df_agrupado['Cenarios'] == 0) & 
                         (df_agrupado['Populacao'] == 10000)]

for tipo in ['Sequencial', 'Paralelo']:
    dados = sis_passos[sis_passos['Tipo'] == tipo]
    fig7.add_trace(go.Scatter(
        x=dados['Passos'],
        y=dados['Tempo_medio'],
        mode='lines+markers',
        name=tipo,
        error_y=dict(type='data', array=dados['Tempo_std']),
        hovertemplate='<b>%{fullData.name}</b><br>' +
                      'Passos: %{x:,.0f}<br>' +
                      'Tempo médio: %{y:.2f} ms<br>' +
                      'Desvio padrão: %{error_y.array:.2f} ms<br>' +
                      '<extra></extra>'
    ))

fig7.update_layout(
    title='SIS: Tempo de Execução × Número de Passos<br><sub>População fixa: 10.000</sub>',
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
                              (df_agrupado['Passos'] == 1001)]

speedup_sis_data = []
for pop in sis_pop_speedup['Populacao'].unique():
    seq = sis_pop_speedup[(sis_pop_speedup['Tipo'] == 'Sequencial') & (sis_pop_speedup['Populacao'] == pop)]['Tempo_medio'].values
    par = sis_pop_speedup[(sis_pop_speedup['Tipo'] == 'Paralelo') & (sis_pop_speedup['Populacao'] == pop)]['Tempo_medio'].values
    if len(seq) > 0 and len(par) > 0:
        speedup_sis_data.append({'Populacao': pop, 'Speedup': seq[0] / par[0]})

speedup_sis_df = pd.DataFrame(speedup_sis_data)

fig8.add_trace(go.Scatter(
    x=speedup_sis_df['Populacao'],
    y=speedup_sis_df['Speedup'],
    mode='lines+markers',
    name='Speedup Real',
    line=dict(color='green', width=3),
    marker=dict(size=10),
    hovertemplate='<b>Speedup Real</b><br>' +
                  'População: %{x:,.0f}<br>' +
                  'Speedup: %{y:.3f}x<br>' +
                  '<extra></extra>'
))

# Linha de referência: speedup ideal (linear = 8x para 8 threads)
fig8.add_trace(go.Scatter(
    x=speedup_sis_df['Populacao'],
    y=[8] * len(speedup_sis_df),
    mode='lines',
    name='Speedup Ideal (8x)',
    line=dict(color='red', dash='dash'),
    hovertemplate='<b>Speedup Ideal</b><br>' +
                  'População: %{x:,.0f}<br>' +
                  'Speedup: 8.0x<br>' +
                  '<extra></extra>'
))

fig8.update_layout(
    title='SIS: Speedup Paralelo vs Sequencial × População<br><sub>Passos fixos: 1.001</sub>',
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
    title='SIS Cenários: Tempo de Execução × Número de Cenários<br><sub>População: 1.000, Passos: 50.000</sub>',
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
