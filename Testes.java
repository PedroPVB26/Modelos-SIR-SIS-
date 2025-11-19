/**
 * Classe principal de testes que executa todas as simulações SIR e SIS
 * (Sequencial, Paralela, Distribuída e Cenários) com os mesmos parâmetros.
 */
public class Testes {
    
    public static void main(String[] args) {
        // Parâmetros comuns para todas as simulações
        double populacaoTotal = 1000000.0;
        double taxaTransmissao = 0.2;
        double taxaRecuperacao = 1.0 / 10.0;
        double infectadosIniciais = 10.0;
        double recuperadosIniciais = 0.0;
        double tempoMaximo = 500.0;
        int numeroPassos = 50000;
        
        // Parâmetros para cenários
        double populacaoBase = 1000000.0;
        int numeroCenarios = 1000;
        int numeroThreads = Runtime.getRuntime().availableProcessors();
        
        System.out.println("=".repeat(70));
        System.out.println("               TESTES DE SIMULAÇÕES EPIDEMIOLÓGICAS");
        System.out.println("=".repeat(70));
        System.out.println("Parâmetros:");
        System.out.println("  População Total: " + populacaoTotal);
        System.out.println("  Taxa de Transmissão: " + taxaTransmissao);
        System.out.println("  Taxa de Recuperação: " + taxaRecuperacao);
        System.out.println("  Infectados Iniciais: " + infectadosIniciais);
        System.out.println("  Tempo Máximo: " + tempoMaximo);
        System.out.println("  Número de Passos: " + numeroPassos);
        System.out.println("  Número de Threads: " + numeroThreads);
        System.out.println("=".repeat(70));
        System.out.println();
        
        // ==================== SIMULAÇÕES SIR ====================
        System.out.println("╔" + "═".repeat(68) + "╗");
        System.out.println("║" + " ".repeat(22) + "MODELO SIR" + " ".repeat(36) + "║");
        System.out.println("╚" + "═".repeat(68) + "╝");
        System.out.println();
        
        // SIR Sequencial
        System.out.println("┌─ SIR Sequencial ─────────────────────────────────────────────┐");
        SIRSequencial sirSeq = new SIRSequencial(
            populacaoTotal, taxaTransmissao, taxaRecuperacao, 
            infectadosIniciais, recuperadosIniciais, tempoMaximo, numeroPassos
        );
        double tempoSirSeq = sirSeq.executarSimulacao();
        System.out.printf("  Tempo de execução: %.4f ms\n", tempoSirSeq);
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        // SIR Paralelo
        System.out.println("┌─ SIR Paralelo ───────────────────────────────────────────────┐");
        SIRParalelo sirPar = new SIRParalelo(
            populacaoTotal, taxaTransmissao, taxaRecuperacao, 
            infectadosIniciais, recuperadosIniciais, tempoMaximo, numeroPassos
        );
        double tempoSirPar = sirPar.executarSimulacao();
        System.out.printf("  Tempo de execução: %.4f ms\n", tempoSirPar);
        System.out.printf("  Speedup: %.2fx\n", tempoSirSeq / tempoSirPar);
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        // SIR Cenários Sequencial
        System.out.println("┌─ SIR Cenários Sequencial ────────────────────────────────────┐");
        cenarios.CenariosSequencialSIR sirCenSeq = new cenarios.CenariosSequencialSIR(
            populacaoBase, infectadosIniciais, recuperadosIniciais, 
            taxaRecuperacao, tempoMaximo, numeroPassos, numeroCenarios
        );
        double tempoSirCenSeq = sirCenSeq.executarSimulacao();
        System.out.printf("  Tempo de execução: %.4f ms\n", tempoSirCenSeq);
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        // SIR Cenários Paralelo
        System.out.println("┌─ SIR Cenários Paralelo ──────────────────────────────────────┐");
        cenarios.CenariosParaleloSIR sirCenPar = new cenarios.CenariosParaleloSIR(
            populacaoBase, infectadosIniciais, recuperadosIniciais, 
            taxaRecuperacao, tempoMaximo, numeroPassos, numeroCenarios, numeroThreads
        );
        double tempoSirCenPar = sirCenPar.executarSimulacao();
        System.out.printf("  Tempo de execução: %.4f ms\n", tempoSirCenPar);
        System.out.printf("  Speedup: %.2fx\n", tempoSirCenSeq / tempoSirCenPar);
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        // SIR Distribuído
        System.out.println("┌─ SIR Distribuído (RMI) ──────────────────────────────────────────┐");
        try {
            ClienteModeloSIR sirDist = new ClienteModeloSIR(
                populacaoTotal, taxaTransmissao, taxaRecuperacao,
                infectadosIniciais, recuperadosIniciais, tempoMaximo, numeroPassos
            );
            double tempoSirDist = sirDist.executarSimulacao();
            System.out.printf("  Tempo de execução: %.4f ms\n", tempoSirDist);
        } catch (Exception e) {
            System.out.println("  ⚠ Servidor RMI não disponível");
            System.out.println("  Para executar, inicie o servidor com:");
            System.out.println("    java ServidorModeloSIR");
        }
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        // ==================== SIMULAÇÕES SIS ====================
        System.out.println();
        System.out.println("╔" + "═".repeat(68) + "╗");
        System.out.println("║" + " ".repeat(22) + "MODELO SIS" + " ".repeat(36) + "║");
        System.out.println("╚" + "═".repeat(68) + "╝");
        System.out.println();
        
        // Parâmetros SIS (sem recuperados iniciais)
        double populacaoTotalSis = 1000.0;
        double taxaTransmissaoSis = 0.3;
        double taxaRecuperacaoSis = 0.1;
        double infectadosIniciaisSis = 1.0;
        double tempoMaximoSis = 100.0;
        int numeroPassosSis = 101;
        
        // SIS Sequencial
        System.out.println("┌─ SIS Sequencial ─────────────────────────────────────────────┐");
        SISSequencial sisSeq = new SISSequencial(
            populacaoTotalSis, taxaTransmissaoSis, taxaRecuperacaoSis,
            infectadosIniciaisSis, tempoMaximoSis, numeroPassosSis
        );
        double tempoSisSeq = sisSeq.executarSimulacao();
        System.out.printf("  Tempo de execução: %.4f ms\n", tempoSisSeq);
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        // SIS Paralelo
        System.out.println("┌─ SIS Paralelo ───────────────────────────────────────────────┐");
        SISParalelo sisPar = new SISParalelo(
            populacaoTotalSis, taxaTransmissaoSis, taxaRecuperacaoSis,
            infectadosIniciaisSis, tempoMaximoSis, numeroPassosSis
        );
        double tempoSisPar = sisPar.executarSimulacao();
        System.out.printf("  Tempo de execução: %.4f ms\n", tempoSisPar);
        System.out.printf("  Speedup: %.2fx\n", tempoSisSeq / tempoSisPar);
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        // SIS Cenários Sequencial
        System.out.println("┌─ SIS Cenários Sequencial ────────────────────────────────────┐");
        cenarios.CenariosSequencialSIS sisCenSeq = new cenarios.CenariosSequencialSIS(
            populacaoTotalSis, infectadosIniciaisSis, taxaRecuperacaoSis,
            tempoMaximoSis, 50000, 1000
        );
        double tempoSisCenSeq = sisCenSeq.executarSimulacao();
        System.out.printf("  Tempo de execução: %.4f ms\n", tempoSisCenSeq);
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        // SIS Cenários Paralelo
        System.out.println("┌─ SIS Cenários Paralelo ──────────────────────────────────────┐");
        cenarios.CenariosParaleloSIS sisCenPar = new cenarios.CenariosParaleloSIS(
            populacaoTotalSis, infectadosIniciaisSis, taxaRecuperacaoSis,
            tempoMaximoSis, 50000, 1000, numeroThreads
        );
        double tempoSisCenPar = sisCenPar.executarSimulacao();
        System.out.printf("  Tempo de execução: %.4f ms\n", tempoSisCenPar);
        System.out.printf("  Speedup: %.2fx\n", tempoSisCenSeq / tempoSisCenPar);
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        // SIS Distribuído
        System.out.println("┌─ SIS Distribuído (RMI) ──────────────────────────────────────────┐");
        try {
            ClienteModeloSIS sisDist = new ClienteModeloSIS(
                populacaoTotalSis, taxaTransmissaoSis, taxaRecuperacaoSis,
                infectadosIniciaisSis, tempoMaximoSis, numeroPassosSis
            );
            double tempoSisDist = sisDist.executarSimulacao();
            System.out.printf("  Tempo de execução: %.4f ms\n", tempoSisDist);
        } catch (Exception e) {
            System.out.println("  ⚠ Servidor RMI não disponível");
            System.out.println("  Para executar, inicie o servidor com:");
            System.out.println("    java ServidorModeloSIS");
        }
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        // ==================== RESUMO ====================
        System.out.println("=".repeat(70));
        System.out.println("                           RESUMO GERAL");
        System.out.println("=".repeat(70));
        System.out.println();
        System.out.println("SIR:");
        System.out.printf("  Sequencial vs Paralelo      : Speedup %.2fx\n", tempoSirSeq / tempoSirPar);
        System.out.printf("  Cenários Seq vs Paralelo    : Speedup %.2fx\n", tempoSirCenSeq / tempoSirCenPar);
        System.out.println();
        System.out.println("SIS:");
        System.out.printf("  Sequencial vs Paralelo      : Speedup %.2fx\n", tempoSisSeq / tempoSisPar);
        System.out.printf("  Cenários Seq vs Paralelo    : Speedup %.2fx\n", tempoSisCenSeq / tempoSisCenPar);
        System.out.println();
        System.out.println("=".repeat(70));
    }
}
