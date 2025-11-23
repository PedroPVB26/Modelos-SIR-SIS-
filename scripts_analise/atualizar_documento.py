#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script para atualizar automaticamente o APRESENTACAO_PDF.md com dados dos CSVs
"""

import pandas as pd
import re
from pathlib import Path

# Caminhos dos arquivos
DADOS_DIR = Path(__file__).parent.parent / "dados"
CSV_LOCAL = DADOS_DIR / "resultados_benchmark.csv"
CSV_DISTRIBUIDO = DADOS_DIR / "resultados_benchmark_distribuido_completo.csv"
DOCUMENTO = Path(__file__).parent.parent / "APRESENTACAO_PDF.md"

def carregar_dados_locais():
    """Carrega e processa dados dos benchmarks locais"""
    df = pd.read_csv(CSV_LOCAL)
    
    # SIR 100k População, 50k Passos
    sir_100k_50k = df[(df['Modelo']=='SIR') & (df['Populacao']==100000) & 
                      (df['Passos']==50000) & (df['Cenarios']==0)]
    sir_pop_stats = sir_100k_50k.groupby(['Tipo', 'Threads'])['Tempo_ms'].agg(['mean', 'std']).round(1)
    
    # SIR Cenários
    sir_cenarios = df[(df['Modelo']=='SIR') & (df['Cenarios']>0)]
    sir_cen_stats = sir_cenarios.groupby(['Tipo', 'Threads', 'Cenarios'])['Tempo_ms'].agg(['mean', 'std']).round(1)
    
    # SIS Cenários
    sis_cenarios = df[(df['Modelo']=='SIS') & (df['Cenarios']>0)]
    sis_cen_stats = sis_cenarios.groupby(['Tipo', 'Threads', 'Cenarios'])['Tempo_ms'].agg(['mean', 'std']).round(1)
    
    return sir_pop_stats, sir_cen_stats, sis_cen_stats

def carregar_dados_distribuidos():
    """Carrega e processa dados dos benchmarks distribuídos"""
    if not CSV_DISTRIBUIDO.exists():
        print(f"⚠️  Arquivo {CSV_DISTRIBUIDO} não encontrado. Pulando dados distribuídos.")
        return None
    
    df = pd.read_csv(CSV_DISTRIBUIDO)
    dist_stats = df.groupby(['Modelo', 'Hosts'])['Tempo_ms'].agg(['mean', 'std']).round(1)
    return dist_stats

def gerar_tabela_sir_populacao(stats):
    """Gera a tabela SIR População 100k, 50k Passos"""
    seq_mean = stats.loc[('Sequencial', 1), 'mean']
    seq_std = stats.loc[('Sequencial', 1), 'std']
    
    par1_mean = stats.loc[('Paralelo', 1), 'mean']
    par1_std = stats.loc[('Paralelo', 1), 'std']
    par1_vs = par1_mean / seq_mean
    
    par2_mean = stats.loc[('Paralelo', 2), 'mean']
    par2_std = stats.loc[('Paralelo', 2), 'std']
    par2_vs = par2_mean / seq_mean
    
    par4_mean = stats.loc[('Paralelo', 4), 'mean']
    par4_std = stats.loc[('Paralelo', 4), 'std']
    par4_vs = par4_mean / seq_mean
    
    par8_mean = stats.loc[('Paralelo', 8), 'mean']
    par8_std = stats.loc[('Paralelo', 8), 'std']
    par8_vs = par8_mean / seq_mean
    
    tabela = f"""#### SIR - População 100k, Passos 50k

| Threads | Tempo Médio | Desvio Padrão | vs Sequencial | Conclusão |
|---------|-------------|---------------|---------------|-----------|
| Sequencial | {seq_mean} ms | ±{seq_std} ms | - | Baseline |
| 1 thread | {par1_mean} ms | ±{par1_std} ms | {par1_vs:.2f}x {'mais rápido' if par1_vs < 1 else 'mais lento'} | Overhead {'mínimo' if par1_vs < 1.5 else 'cresce'} |
| 2 threads | {par2_mean} ms | ±{par2_std} ms | {par2_vs:.2f}x mais lento | Overhead {'cresce' if par2_vs < 3 else 'alto'} |
| 4 threads | {par4_mean} ms | ±{par4_std} ms | {par4_vs:.2f}x mais lento | Overhead alto |
| 8 threads | {par8_mean} ms | ±{par8_std} ms | {par8_vs:.2f}x mais lento | Overhead alto |"""
    
    return tabela

def gerar_tabela_sir_cenarios(stats):
    """Gera a tabela SIR Cenários"""
    linhas = []
    for cenarios in [100, 500, 1000]:
        seq_mean = stats.loc[('Cenarios_Sequencial', 1, cenarios), 'mean']
        par_mean = stats.loc[('Cenarios_Paralelo', 8, cenarios), 'mean']
        speedup = seq_mean / par_mean
        eficiencia = (speedup / 8) * 100
        
        status = "✅ Bom" if speedup > 3 else "⚠️ Regular"
        if eficiencia > 70:
            status = "✅ Muito Bom"
        elif eficiencia > 85:
            status = "✅ Excelente"
        
        linhas.append(f"| {cenarios:>4}     | {seq_mean:>8.1f} ms   | {par_mean:>8.1f} ms             | {speedup:>5.2f}x   | {eficiencia:>5.1f}%      | {status} |")
    
    tabela = f"""#### SIR - Múltiplos Cenários (População 1M, 50k passos)

| Cenários | Sequencial | Paralelo (8 threads) | Speedup | Eficiência | Status |
|----------|------------|----------------------|---------|------------|--------|
{chr(10).join(linhas)}"""
    
    return tabela

def gerar_tabela_sis_cenarios(stats):
    """Gera a tabela SIS Cenários"""
    linhas = []
    for cenarios in [100, 500, 1000]:
        seq_mean = stats.loc[('Cenarios_Sequencial', 1, cenarios), 'mean']
        par_mean = stats.loc[('Cenarios_Paralelo', 8, cenarios), 'mean']
        speedup = seq_mean / par_mean
        eficiencia = (speedup / 8) * 100
        
        linhas.append(f"| {cenarios:>4}     | {seq_mean:>8.1f} ms   | {par_mean:>8.1f} ms              | {speedup:>5.2f}x   | {eficiencia:>5.1f}%      |")
    
    tabela = f"""#### SIS - Múltiplos Cenários (População 1M, 50k passos)

| Cenários | Sequencial | Paralelo (8 threads) | Speedup | Eficiência |
|----------|------------|----------------------|---------|------------|
{chr(10).join(linhas)}"""
    
    return tabela

def gerar_tabela_distribuido(stats):
    """Gera a tabela de benchmarks distribuídos RMI"""
    if stats is None:
        return None
    
    linhas = []
    for modelo in ['SIR', 'SIS']:
        if modelo not in stats.index.get_level_values(0):
            continue
        
        for hosts in [1, 2, 4, 8]:
            if (modelo, hosts) not in stats.index:
                continue
            
            mean = stats.loc[(modelo, hosts), 'mean']
            std = stats.loc[(modelo, hosts), 'std']
            
            # Calcular speedup vs 1 host
            if hosts == 1:
                baseline = mean
                speedup = 1.0
            else:
                speedup = baseline / mean
            
            eficiencia = (speedup / hosts) * 100 if hosts > 1 else 100
            
            linhas.append(f"| {modelo:>3} | {hosts:>1} | {mean:>8.1f} ms | ±{std:>5.1f} ms | {speedup:>5.2f}x | {eficiencia:>5.1f}% |")
    
    if not linhas:
        return None
    
    tabela = f"""#### Benchmarks Distribuídos (RMI)

| Modelo | Hosts | Tempo Médio | Desvio Padrão | Speedup | Eficiência |
|--------|-------|-------------|---------------|---------|------------|
{chr(10).join(linhas)}"""
    
    return tabela

def atualizar_documento():
    """Atualiza o documento com os novos dados"""
    print("🔄 Carregando dados dos CSVs...")
    
    # Carregar dados
    sir_pop, sir_cen, sis_cen = carregar_dados_locais()
    dist_stats = carregar_dados_distribuidos()
    
    print("📊 Gerando tabelas...")
    
    # Gerar tabelas
    tabela_sir_pop = gerar_tabela_sir_populacao(sir_pop)
    tabela_sir_cen = gerar_tabela_sir_cenarios(sir_cen)
    tabela_sis_cen = gerar_tabela_sis_cenarios(sis_cen)
    tabela_dist = gerar_tabela_distribuido(dist_stats) if dist_stats is not None else None
    
    # Ler documento
    print("📝 Lendo documento...")
    with open(DOCUMENTO, 'r', encoding='utf-8') as f:
        conteudo = f.read()
    
    # Substituir tabela SIR População
    print("   ↳ Atualizando tabela SIR População...")
    padrao_sir_pop = r'#### SIR - População 100k, Passos 50k\n\n\| Threads.*?\| 8 threads.*?\|.*?\n'
    conteudo = re.sub(padrao_sir_pop, tabela_sir_pop + '\n', conteudo, flags=re.DOTALL)
    
    # Substituir tabela SIR Cenários
    print("   ↳ Atualizando tabela SIR Cenários...")
    padrao_sir_cen = r'#### SIR - Múltiplos Cenários \(População 1M, 50k passos\)\n\n\| Cenários.*?\| 1000.*?\|.*?\n'
    conteudo = re.sub(padrao_sir_cen, tabela_sir_cen + '\n', conteudo, flags=re.DOTALL)
    
    # Substituir tabela SIS Cenários
    print("   ↳ Atualizando tabela SIS Cenários...")
    padrao_sis_cen = r'#### SIS - Múltiplos Cenários \(População 1M, 50k passos\)\n\n\| Cenários.*?\| 1000.*?\|.*?\n'
    conteudo = re.sub(padrao_sis_cen, tabela_sis_cen + '\n', conteudo, flags=re.DOTALL)
    
    # Substituir tabela distribuída (se existir)
    if tabela_dist:
        print("   ↳ Atualizando tabela Distribuída (RMI)...")
        padrao_dist = r'#### Benchmarks Distribuídos \(RMI\)\n\n\| Modelo.*?\n(?:\|.*?\n)+'
        if re.search(padrao_dist, conteudo):
            conteudo = re.sub(padrao_dist, tabela_dist + '\n', conteudo, flags=re.DOTALL)
        else:
            # Se não existir, adiciona após tabela SIS
            conteudo = re.sub(
                r'(#### SIS - Múltiplos Cenários.*?\n(?:\|.*?\n)+)',
                r'\1\n' + tabela_dist + '\n',
                conteudo,
                flags=re.DOTALL
            )
    
    # Salvar documento
    print("💾 Salvando documento...")
    with open(DOCUMENTO, 'w', encoding='utf-8') as f:
        f.write(conteudo)
    
    print("✅ Documento atualizado com sucesso!")
    print(f"   📄 {DOCUMENTO}")

if __name__ == "__main__":
    try:
        atualizar_documento()
    except Exception as e:
        print(f"❌ Erro ao atualizar documento: {e}")
        import traceback
        traceback.print_exc()
        exit(1)
