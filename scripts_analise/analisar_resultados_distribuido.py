import pandas as pd
import plotly.graph_objects as go
from plotly.subplots import make_subplots
import webbrowser
import os

print("="*80)
print("ANÁLISE DOS RESULTADOS - BENCHMARKS DISTRIBUÍDOS")
print("="*80)

# Leitura do CSV
df = pd.read_csv('resultados_benchmark_distribuido.csv')

# Calcula média e desvio padrão por grupo
df_agrupado = df.groupby(['Modelo', 'Tipo', 'Hosts', 'Cenarios']).agg({
    'Tempo_ms': ['mean', 'std', 'min', 'max']
}).reset_index()

df_agrupado.columns = ['Modelo', 'Tipo', 'Hosts', 'Cenarios', 
                        'Tempo_medio', 'Tempo_std', 'Tempo_min', 'Tempo_max']

print(f"\nTotal de testes realizados: {len(df)}")
print(f"Configurações testadas: {len(df_agrupado)}")

# =============================================================================
# GRÁFICO 1: Tempo × Número de Hosts (por número de cenários)
# =============================================================================
fig1 = go.Figure()

for cenarios in df_agrupado['Cenarios'].unique():
    dados = df_agrupado[df_agrupado['Cenarios'] == cenarios]
    fig1.add_trace(go.Scatter(
        x=dados['Hosts'],
        y=dados['Tempo_medio'],
        mode='lines+markers',
        name=f'{cenarios} cenários',
        error_y=dict(type='data', array=dados['Tempo_std']),
        hovertemplate='<b>%{fullData.name}</b><br>' +
                      'Hosts: %{x}<br>' +
                      'Tempo médio: %{y:.2f} ms<br>' +
                      'Desvio padrão: %{error_y.array:.2f} ms<br>' +
                      '<extra></extra>'
    ))

fig1.update_layout(
    title='SIR Distribuído: Tempo de Execução × Número de Hosts<br><sub>População: 1.000.000, Passos: 50.000</sub>',
    xaxis_title='Número de Hosts',
    yaxis_title='Tempo de Execução (ms)',
    hovermode='closest',
    template='plotly_white',
    font=dict(size=12)
)

# =============================================================================
# GRÁFICO 2: Speedup × Número de Hosts
# =============================================================================
fig2 = make_subplots(
    rows=1, cols=2,
    subplot_titles=('Speedup × Número de Hosts', 'Eficiência × Número de Hosts')
)

cores = ['blue', 'green', 'orange']
for idx, cenarios in enumerate(sorted(df_agrupado['Cenarios'].unique())):
    dados_cenario = df_agrupado[df_agrupado['Cenarios'] == cenarios].sort_values('Hosts')
    
    # Calcular speedup (tempo com 1 host / tempo com N hosts)
    tempo_1_host = dados_cenario[dados_cenario['Hosts'] == 1]['Tempo_medio'].values[0]
    speedups = tempo_1_host / dados_cenario['Tempo_medio']
    eficiencias = (speedups / dados_cenario['Hosts']) * 100
    
    # Speedup
    fig2.add_trace(go.Scatter(
        x=dados_cenario['Hosts'],
        y=speedups,
        mode='lines+markers',
        name=f'{cenarios} cenários',
        line=dict(color=cores[idx], width=3),
        marker=dict(size=10),
        legendgroup=f'group{idx}',
        hovertemplate='<b>%{fullData.name}</b><br>' +
                      'Hosts: %{x}<br>' +
                      'Speedup: %{y:.2f}x<br>' +
                      '<extra></extra>'
    ), row=1, col=1)
    
    # Eficiência
    fig2.add_trace(go.Scatter(
        x=dados_cenario['Hosts'],
        y=eficiencias,
        mode='lines+markers',
        name=f'{cenarios} cenários',
        line=dict(color=cores[idx], width=3),
        marker=dict(size=10),
        legendgroup=f'group{idx}',
        showlegend=False,
        hovertemplate='<b>%{fullData.name}</b><br>' +
                      'Hosts: %{x}<br>' +
                      'Eficiência: %{y:.1f}%<br>' +
                      '<extra></extra>'
    ), row=1, col=2)

# Linhas de referência - Speedup ideal
max_hosts = df_agrupado['Hosts'].max()
hosts_range = list(range(1, max_hosts + 1))
fig2.add_trace(go.Scatter(
    x=hosts_range,
    y=hosts_range,
    mode='lines',
    name='Speedup Ideal (linear)',
    line=dict(color='red', dash='dash'),
    hovertemplate='<b>Speedup Ideal</b><br>' +
                  'Hosts: %{x}<br>' +
                  'Speedup: %{y}x<br>' +
                  '<extra></extra>'
), row=1, col=1)

# Linha de referência - Eficiência ideal (100%)
fig2.add_trace(go.Scatter(
    x=hosts_range,
    y=[100] * len(hosts_range),
    mode='lines',
    name='Eficiência Ideal (100%)',
    line=dict(color='red', dash='dash'),
    showlegend=False,
    hovertemplate='<b>Eficiência Ideal</b><br>' +
                  'Hosts: %{x}<br>' +
                  'Eficiência: 100%<br>' +
                  '<extra></extra>'
), row=1, col=2)

fig2.update_xaxes(title_text="Número de Hosts", row=1, col=1)
fig2.update_xaxes(title_text="Número de Hosts", row=1, col=2)
fig2.update_yaxes(title_text="Speedup (x)", row=1, col=1)
fig2.update_yaxes(title_text="Eficiência (%)", row=1, col=2)

fig2.update_layout(
    title_text='SIR Distribuído: Análise de Escalabilidade',
    hovermode='closest',
    template='plotly_white',
    height=500,
    font=dict(size=12)
)

# =============================================================================
# GRÁFICO 3: Comparação de Throughput (cenários/segundo)
# =============================================================================
fig3 = go.Figure()

for cenarios in df_agrupado['Cenarios'].unique():
    dados = df_agrupado[df_agrupado['Cenarios'] == cenarios]
    # Throughput = cenários / (tempo em segundos)
    throughput = cenarios / (dados['Tempo_medio'] / 1000)
    
    fig3.add_trace(go.Scatter(
        x=dados['Hosts'],
        y=throughput,
        mode='lines+markers',
        name=f'{cenarios} cenários',
        hovertemplate='<b>%{fullData.name}</b><br>' +
                      'Hosts: %{x}<br>' +
                      'Throughput: %{y:.2f} cenários/s<br>' +
                      '<extra></extra>'
    ))

fig3.update_layout(
    title='SIR Distribuído: Throughput × Número de Hosts<br><sub>Cenários processados por segundo</sub>',
    xaxis_title='Número de Hosts',
    yaxis_title='Throughput (cenários/segundo)',
    hovermode='closest',
    template='plotly_white',
    font=dict(size=12)
)

# =============================================================================
# Salvar gráficos
# =============================================================================
fig1.write_html('grafico_distribuido_tempo_hosts.html')
print("\n✓ Gráfico 1 salvo: grafico_distribuido_tempo_hosts.html")

fig2.write_html('grafico_distribuido_speedup_eficiencia.html')
print("✓ Gráfico 2 salvo: grafico_distribuido_speedup_eficiencia.html")

fig3.write_html('grafico_distribuido_throughput.html')
print("✓ Gráfico 3 salvo: grafico_distribuido_throughput.html")

# =============================================================================
# Criar página índice
# =============================================================================
index_html = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Análise de Benchmarks Distribuídos - SIR</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
            background-color: #f5f5f5;
        }
        h1 {
            color: #333;
            text-align: center;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background-color: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        .graph-list {
            list-style: none;
            padding: 0;
        }
        .graph-list li {
            margin: 10px 0;
            padding: 15px;
            background-color: #f9f9f9;
            border-left: 4px solid #2196F3;
            border-radius: 5px;
        }
        .graph-list a {
            color: #2196F3;
            text-decoration: none;
            font-size: 16px;
            font-weight: bold;
        }
        .graph-list a:hover {
            text-decoration: underline;
        }
        .info {
            background-color: #e3f2fd;
            padding: 15px;
            border-radius: 5px;
            margin: 20px 0;
        }
        .warning {
            background-color: #fff3cd;
            padding: 15px;
            border-radius: 5px;
            margin: 20px 0;
            border-left: 4px solid #ffc107;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🌐 Análise de Benchmarks Distribuídos - Modelo SIR</h1>
        
        <div class="info">
            <p><strong>Configuração dos Testes:</strong></p>
            <ul>
                <li><strong>População:</strong> 1.000.000</li>
                <li><strong>Passos:</strong> 50.000</li>
                <li><strong>Número de hosts testados:</strong> 1, 2, 4, 8</li>
                <li><strong>Cenários testados:</strong> 100, 500, 1000</li>
                <li><strong>Repetições:</strong> 5 por configuração</li>
            </ul>
        </div>
        
        <div class="warning">
            <p><strong>⚠️ Nota sobre a simulação:</strong></p>
            <p>Esta implementação simula múltiplos hosts usando diferentes portas RMI (1099, 1100, 1101, etc.) 
            na mesma máquina. Para testes reais com máquinas físicas diferentes:</p>
            <ul>
                <li>Execute ServidorModeloSIR em cada máquina</li>
                <li>Modifique o cliente para conectar aos IPs reais</li>
                <li>Configure firewall para permitir comunicação RMI</li>
            </ul>
        </div>
        
        <h2>📊 Gráficos Interativos</h2>
        
        <ul class="graph-list">
            <li>⏱️ <a href="grafico_distribuido_tempo_hosts.html" target="_blank">Tempo de Execução × Número de Hosts</a></li>
            <li>⚡ <a href="grafico_distribuido_speedup_eficiencia.html" target="_blank">Speedup e Eficiência × Número de Hosts</a></li>
            <li>📈 <a href="grafico_distribuido_throughput.html" target="_blank">Throughput × Número de Hosts</a></li>
        </ul>
        
        <h2>📊 Resumo Estatístico</h2>
        <p>Speedup esperado ao usar múltiplos hosts:</p>
        <ul>
            <li><strong>Linear (ideal):</strong> 2 hosts = 2x mais rápido, 4 hosts = 4x, etc.</li>
            <li><strong>Real:</strong> Menor devido a overhead de rede e coordenação</li>
            <li><strong>Eficiência:</strong> (Speedup real / Número de hosts) × 100%</li>
        </ul>
    </div>
</body>
</html>
"""

with open('index_graficos_distribuido.html', 'w', encoding='utf-8') as f:
    f.write(index_html)

print("\n✓ Página índice criada: index_graficos_distribuido.html")

print("\n" + "="*80)
print("✓ Análise completa!")
print("="*80)

# Abrir navegador
index_path = os.path.abspath('index_graficos_distribuido.html')
webbrowser.open('file://' + index_path)

print(f"\n💡 Arquivo: {index_path}")
