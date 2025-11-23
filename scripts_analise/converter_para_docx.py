#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script para converter APRESENTACAO_PDF.md para .docx
Usa pypandoc para conversão com suporte completo a Markdown
"""

import subprocess
import sys
from pathlib import Path

# Caminhos
SCRIPT_DIR = Path(__file__).parent
ROOT_DIR = SCRIPT_DIR.parent
MARKDOWN_FILE = ROOT_DIR / "APRESENTACAO_PDF.md"
OUTPUT_FILE = ROOT_DIR / "APRESENTACAO_PDF.docx"

def verificar_pandoc():
    """Verifica se pandoc está instalado"""
    try:
        result = subprocess.run(['pandoc', '--version'], 
                              capture_output=True, 
                              text=True)
        if result.returncode == 0:
            print("✅ Pandoc encontrado:", result.stdout.split('\n')[0])
            return True
    except FileNotFoundError:
        pass
    
    print("❌ Pandoc não encontrado!")
    print("\nInstalação:")
    print("  1. Baixe: https://pandoc.org/installing.html")
    print("  2. Ou use: choco install pandoc")
    print("  3. Ou use: winget install pandoc")
    return False

def converter_markdown_para_docx():
    """Converte o arquivo Markdown para DOCX usando pandoc"""
    
    print(f"📄 Convertendo: {MARKDOWN_FILE.name}")
    print(f"➡️  Destino: {OUTPUT_FILE.name}")
    print()
    
    if not MARKDOWN_FILE.exists():
        print(f"❌ Arquivo não encontrado: {MARKDOWN_FILE}")
        return False
    
    # Comando pandoc com opções otimizadas para o documento
    comando = [
        'pandoc',
        str(MARKDOWN_FILE),
        '-o', str(OUTPUT_FILE),
        '--from', 'markdown+emoji+pipe_tables',
        '--to', 'docx',
        '--reference-doc', str(SCRIPT_DIR / 'template.docx') if (SCRIPT_DIR / 'template.docx').exists() else '',
        '--highlight-style', 'tango',
        '--toc',
        '--toc-depth', '3',
    ]
    
    # Remove argumento vazio se template não existir
    comando = [arg for arg in comando if arg]
    
    try:
        print("🔄 Executando conversão...")
        result = subprocess.run(comando, 
                              capture_output=True, 
                              text=True,
                              cwd=str(ROOT_DIR))
        
        if result.returncode == 0:
            print("✅ Conversão concluída com sucesso!")
            print(f"📁 Arquivo criado: {OUTPUT_FILE}")
            
            # Mostrar tamanho do arquivo
            size_kb = OUTPUT_FILE.stat().st_size / 1024
            print(f"📊 Tamanho: {size_kb:.1f} KB")
            return True
        else:
            print("❌ Erro na conversão:")
            print(result.stderr)
            return False
            
    except Exception as e:
        print(f"❌ Erro ao executar pandoc: {e}")
        return False

def main():
    """Função principal"""
    print("=" * 60)
    print("  CONVERSOR MARKDOWN → DOCX")
    print("=" * 60)
    print()
    
    # Verificar pandoc
    if not verificar_pandoc():
        sys.exit(1)
    
    print()
    
    # Converter
    sucesso = converter_markdown_para_docx()
    
    print()
    if sucesso:
        print("🎉 Processo concluído!")
        print(f"\n💡 Dica: Abra o arquivo com Word para revisar formatação")
    else:
        print("⚠️  Conversão falhou. Verifique os erros acima.")
        sys.exit(1)

if __name__ == "__main__":
    main()
