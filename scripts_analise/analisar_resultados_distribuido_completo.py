import pandas as pd
import plotly.graph_objects as go
from plotly.subplots import make_subplots
import webbrowser
import os

print("="*80)
print("ANÁLISE COMPLETA - BENCHMARKS DISTRIBUÍDOS SIR e SIS")
print("="*80)

# Leitura do CSV
df = pd.read_csv('../dados/resultados_benchmark_distribuido_completo.csv')

# Calcula média e desvio padrão por grupo
df_agrupado = df.groupby(['Modelo', 'Tipo', 'Hosts', 'Cenarios']).agg({
    'Tempo_ms': ['mean', 'std', 'min', 'max']
}).reset_index()

df_agrupado.columns = ['Modelo', 'Tipo', 'Hosts', 'Cenarios', 
                        'Tempo_medio', 'Tempo_std', 'Tempo_min', 'Tempo_max']

print(f"\nTotal de testes realizados: {len(df)}")
print(f"Configurações testadas: {len(df_agrupado)}")

# Separar por modelo
df_sir = df_agrupado[df_agrupado['Modelo'] == 'SIR']
df_sis = df_agrupado[df_agrupado['Modelo'] == 'SIS']

# =============================================================================
# GRÁFICO 1: SIR - Tempo × Hosts (por cenários)
# =============================================================================
fig1 = go.Figure()

for cenarios in df_sir['Cenarios'].unique():
    dados = df_sir[df_sir['Cenarios'] == cenarios]
    fig1.add_trace(go.Scatter(
        x=dados['Hosts'],
        y=dados['Tempo_medio'],
        mode='lines+markers',
        name=f'{cenarios} cenários',
        error_y=dict(type='data', array=dados['Tempo_std']),
        hovertemplate='<b>%{fullData.name}</b><br>Hosts: %{x}<br>Tempo: %{y:.2f} ms<br><extra></extra>'
    ))

fig1.update_layout(
    title='SIR Distribuído: Tempo × Número de Hosts<br><sub>População: 1.000.000, Passos: 50.000</sub>',
    xaxis_title='Número de Hosts',
    yaxis_title='Tempo de Execução (ms)',
    hovermode='closest',
    template='plotly_white'
)

# =============================================================================
# GRÁFICO 2: SIS - Tempo × Hosts (por cenários)
# =============================================================================
fig2 = go.Figure()

for cenarios in df_sis['Cenarios'].unique():
    dados = df_sis[df_sis['Cenarios'] == cenarios]
    fig2.add_trace(go.Scatter(
        x=dados['Hosts'],
        y=dados['Tempo_medio'],
        mode='lines+markers',
        name=f'{cenarios} cenários',
        error_y=dict(type='data', array=dados['Tempo_std']),
        hovertemplate='<b>%{fullData.name}</b><br>Hosts: %{x}<br>Tempo: %{y:.2f} ms<br><extra></extra>'
    ))

fig2.update_layout(
    title='SIS Distribuído: Tempo × Número de Hosts<br><sub>População: 1.000, Passos: 50.000</sub>',
    xaxis_title='Número de Hosts',
    yaxis_title='Tempo de Execução (ms)',
    hovermode='closest',
    template='plotly_white'
)

# =============================================================================
# GRÁFICO 3: Comparação SIR vs SIS - Speedup
# =============================================================================
fig3 = make_subplots(
    rows=1, cols=2,
    subplot_titles=('SIR: Speedup × Hosts', 'SIS: Speedup × Hosts')
)

cores = ['blue', 'green', 'orange']

# SIR Speedup
for idx, cenarios in enumerate(sorted(df_sir['Cenarios'].unique())):
    dados = df_sir[df_sir['Cenarios'] == cenarios].sort_values('Hosts')
    tempo_1_host = dados[dados['Hosts'] == 1]['Tempo_medio'].values[0]
    speedups = tempo_1_host / dados['Tempo_medio']
    
    fig3.add_trace(go.Scatter(
        x=dados['Hosts'],
        y=speedups,
        mode='lines+markers',
        name=f'{cenarios} cenários',
        line=dict(color=cores[idx], width=3),
        marker=dict(size=10),
        legendgroup=f'sir{idx}',
        hovertemplate='<b>SIR - %{fullData.name}</b><br>Hosts: %{x}<br>Speedup: %{y:.2f}x<br><extra></extra>'
    ), row=1, col=1)

# SIS Speedup
for idx, cenarios in enumerate(sorted(df_sis['Cenarios'].unique())):
    dados = df_sis[df_sis['Cenarios'] == cenarios].sort_values('Hosts')
    tempo_1_host = dados[dados['Hosts'] == 1]['Tempo_medio'].values[0]
    speedups = tempo_1_host / dados['Tempo_medio']
    
    fig3.add_trace(go.Scatter(
        x=dados['Hosts'],
        y=speedups,
        mode='lines+markers',
        name=f'{cenarios} cenários',
        line=dict(color=cores[idx], width=3),
        marker=dict(size=10),
        legendgroup=f'sis{idx}',
        showlegend=False,
        hovertemplate='<b>SIS - %{fullData.name}</b><br>Hosts: %{x}<br>Speedup: %{y:.2f}x<br><extra></extra>'
    ), row=1, col=2)

# Linhas ideais
max_hosts = df_agrupado['Hosts'].max()
hosts_range = list(range(1, max_hosts + 1))

fig3.add_trace(go.Scatter(
    x=hosts_range, y=hosts_range,
    mode='lines', name='Speedup Ideal',
    line=dict(color='red', dash='dash'),
    hovertemplate='Ideal: %{y}x<br><extra></extra>'
), row=1, col=1)

fig3.add_trace(go.Scatter(
    x=hosts_range, y=hosts_range,
    mode='lines', name='Speedup Ideal',
    line=dict(color='red', dash='dash'),
    showlegend=False,
    hovertemplate='Ideal: %{y}x<br><extra></extra>'
), row=1, col=2)

fig3.update_xaxes(title_text="Número de Hosts", row=1, col=1)
fig3.update_xaxes(title_text="Número de Hosts", row=1, col=2)
fig3.update_yaxes(title_text="Speedup (x)", row=1, col=1)
fig3.update_yaxes(title_text="Speedup (x)", row=1, col=2)

fig3.update_layout(
    title_text='Comparação de Speedup Distribuído: SIR vs SIS',
    hovermode='closest',
    template='plotly_white',
    height=500
)

# =============================================================================
# GRÁFICO 4: Eficiência (%)
# =============================================================================
fig4 = make_subplots(
    rows=1, cols=2,
    subplot_titles=('SIR: Eficiência × Hosts', 'SIS: Eficiência × Hosts')
)

# SIR Eficiência
for idx, cenarios in enumerate(sorted(df_sir['Cenarios'].unique())):
    dados = df_sir[df_sir['Cenarios'] == cenarios].sort_values('Hosts')
    tempo_1_host = dados[dados['Hosts'] == 1]['Tempo_medio'].values[0]
    speedups = tempo_1_host / dados['Tempo_medio']
    eficiencias = (speedups / dados['Hosts']) * 100
    
    fig4.add_trace(go.Scatter(
        x=dados['Hosts'],
        y=eficiencias,
        mode='lines+markers',
        name=f'{cenarios} cenários',
        line=dict(color=cores[idx], width=3),
        marker=dict(size=10),
        legendgroup=f'sir_ef{idx}',
        hovertemplate='<b>SIR - %{fullData.name}</b><br>Hosts: %{x}<br>Eficiência: %{y:.1f}%<br><extra></extra>'
    ), row=1, col=1)

# SIS Eficiência
for idx, cenarios in enumerate(sorted(df_sis['Cenarios'].unique())):
    dados = df_sis[df_sis['Cenarios'] == cenarios].sort_values('Hosts')
    tempo_1_host = dados[dados['Hosts'] == 1]['Tempo_medio'].values[0]
    speedups = tempo_1_host / dados['Tempo_medio']
    eficiencias = (speedups / dados['Hosts']) * 100
    
    fig4.add_trace(go.Scatter(
        x=dados['Hosts'],
        y=eficiencias,
        mode='lines+markers',
        name=f'{cenarios} cenários',
        line=dict(color=cores[idx], width=3),
        marker=dict(size=10),
        legendgroup=f'sis_ef{idx}',
        showlegend=False,
        hovertemplate='<b>SIS - %{fullData.name}</b><br>Hosts: %{x}<br>Eficiência: %{y:.1f}%<br><extra></extra>'
    ), row=1, col=2)

# Linha ideal (100%)
fig4.add_trace(go.Scatter(
    x=hosts_range, y=[100] * len(hosts_range),
    mode='lines', name='Eficiência Ideal (100%)',
    line=dict(color='red', dash='dash'),
    hovertemplate='Ideal: 100%<br><extra></extra>'
), row=1, col=1)

fig4.add_trace(go.Scatter(
    x=hosts_range, y=[100] * len(hosts_range),
    mode='lines', name='Eficiência Ideal',
    line=dict(color='red', dash='dash'),
    showlegend=False,
    hovertemplate='Ideal: 100%<br><extra></extra>'
), row=1, col=2)

fig4.update_xaxes(title_text="Número de Hosts", row=1, col=1)
fig4.update_xaxes(title_text="Número de Hosts", row=1, col=2)
fig4.update_yaxes(title_text="Eficiência (%)", row=1, col=1)
fig4.update_yaxes(title_text="Eficiência (%)", row=1, col=2)

fig4.update_layout(
    title_text='Comparação de Eficiência Distribuída: SIR vs SIS',
    hovermode='closest',
    template='plotly_white',
    height=500
)

# =============================================================================
# GRÁFICO 5: Throughput Comparativo
# =============================================================================
fig5 = go.Figure()

for modelo in ['SIR', 'SIS']:
    df_modelo = df_agrupado[df_agrupado['Modelo'] == modelo]
    
    for cenarios in df_modelo['Cenarios'].unique():
        dados = df_modelo[df_modelo['Cenarios'] == cenarios]
        throughput = cenarios / (dados['Tempo_medio'] / 1000)
        
        fig5.add_trace(go.Scatter(
            x=dados['Hosts'],
            y=throughput,
            mode='lines+markers',
            name=f'{modelo} - {cenarios} cenários',
            hovertemplate='<b>%{fullData.name}</b><br>Hosts: %{x}<br>Throughput: %{y:.2f} cenários/s<br><extra></extra>'
        ))

fig5.update_layout(
    title='Throughput Distribuído: SIR vs SIS<br><sub>Cenários processados por segundo</sub>',
    xaxis_title='Número de Hosts',
    yaxis_title='Throughput (cenários/segundo)',
    hovermode='closest',
    template='plotly_white'
)

# =============================================================================
# Salvar gráficos
# =============================================================================
fig1.write_html('grafico_distribuido_sir_tempo_hosts.html')
print("\n✓ Gráfico 1 salvo: grafico_distribuido_sir_tempo_hosts.html")

fig2.write_html('grafico_distribuido_sis_tempo_hosts.html')
print("✓ Gráfico 2 salvo: grafico_distribuido_sis_tempo_hosts.html")

fig3.write_html('grafico_distribuido_comparacao_speedup.html')
print("✓ Gráfico 3 salvo: grafico_distribuido_comparacao_speedup.html")

fig4.write_html('grafico_distribuido_comparacao_eficiencia.html')
print("✓ Gráfico 4 salvo: grafico_distribuido_comparacao_eficiencia.html")

fig5.write_html('grafico_distribuido_throughput_comparativo.html')
print("✓ Gráfico 5 salvo: grafico_distribuido_throughput_comparativo.html")

# =============================================================================
# Página índice
# =============================================================================
index_html = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Benchmarks Distribuídos - SIR e SIS</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        h1 { color: #333; text-align: center; }
        .container { max-width: 1200px; margin: 0 auto; background-color: white; 
                    padding: 20px; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        .graph-list { list-style: none; padding: 0; }
        .graph-list li { margin: 10px 0; padding: 15px; background-color: #f9f9f9; 
                        border-left: 4px solid #2196F3; border-radius: 5px; }
        .graph-list a { color: #2196F3; text-decoration: none; font-size: 16px; font-weight: bold; }
        .graph-list a:hover { text-decoration: underline; }
        .info { background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #2196F3; color: white; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🌐 Análise Completa - Benchmarks Distribuídos SIR e SIS</h1>
        
        <div class="info">
            <h3>Configuração dos Testes</h3>
            <table>
                <tr><th>Parâmetro</th><th>SIR</th><th>SIS</th></tr>
                <tr><td>População</td><td>1.000.000</td><td>1.000</td></tr>
                <tr><td>Passos</td><td>50.000</td><td>50.000</td></tr>
                <tr><td>Número de hosts</td><td colspan="2">1, 2, 4, 8</td></tr>
                <tr><td>Cenários testados</td><td colspan="2">100, 500, 1000</td></tr>
                <tr><td>Repetições</td><td colspan="2">5 por configuração</td></tr>
            </table>
        </div>
        
        <h2>📊 Gráficos Interativos</h2>
        <ul class="graph-list">
            <li>⏱️ <a href="grafico_distribuido_sir_tempo_hosts.html" target="_blank">SIR: Tempo × Número de Hosts</a></li>
            <li>⏱️ <a href="grafico_distribuido_sis_tempo_hosts.html" target="_blank">SIS: Tempo × Número de Hosts</a></li>
            <li>⚡ <a href="grafico_distribuido_comparacao_speedup.html" target="_blank">Comparação de Speedup: SIR vs SIS</a></li>
            <li>📊 <a href="grafico_distribuido_comparacao_eficiencia.html" target="_blank">Comparação de Eficiência: SIR vs SIS</a></li>
            <li>📈 <a href="grafico_distribuido_throughput_comparativo.html" target="_blank">Throughput Comparativo</a></li>
        </ul>
        
        <h2>📝 Interpretação dos Resultados</h2>
        <ul>
            <li><strong>Speedup:</strong> Quantas vezes mais rápido fica ao usar múltiplos hosts</li>
            <li><strong>Eficiência:</strong> Quão bem os hosts estão sendo utilizados (ideal = 100%)</li>
            <li><strong>Throughput:</strong> Quantos cenários são processados por segundo</li>
            <li><strong>Overhead:</strong> Comunicação RMI e coordenação reduzem eficiência</li>
        </ul>
    </div>
</body>
</html>
"""

with open('../graficos/index_graficos_distribuido_completo.html', 'w', encoding='utf-8') as f:
    f.write(index_html)

print("\n✓ Página índice criada: ../graficos/index_graficos_distribuido_completo.html")
print("\n" + "="*80)
print("✓ Análise completa!")
print("="*80)

index_path = os.path.abspath('../graficos/index_graficos_distribuido_completo.html')
webbrowser.open('file://' + index_path)
print(f"\n💡 Arquivo: {index_path}")
