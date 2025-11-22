import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Classe para executar benchmarks variando tamanhos de problema.
 * Testa diferentes configurações de população, passos e cenários.
 */
public class Benchmarks {
    
    private static final int REPETICOES = 15; // Número de repetições por teste
    private static final String ARQUIVO_CSV = "../dados/resultados_benchmark.csv";
    
    // Configurações de tamanho de problema para testar
    private static final double[] TAMANHOS_POPULACAO = {100000.0, 500000.0, 1000000.0, 2000000.0};
    private static final int[] TAMANHOS_PASSOS = {10000, 25000, 50000};
    private static final int[] TAMANHOS_CENARIOS = {100, 500, 1000};
    
    // Parâmetros fixos do modelo
    private static final double TAXA_TRANSMISSAO_SIR = 0.2;
    private static final double TAXA_RECUPERACAO_SIR = 0.1;
    private static final double INFECTADOS_INICIAIS_SIR = 10.0;
    private static final double RECUPERADOS_INICIAIS_SIR = 0.0;
    
    private static final double TAXA_TRANSMISSAO_SIS = 0.3;
    private static final double TAXA_RECUPERACAO_SIS = 0.1;
    private static final double INFECTADOS_INICIAIS_SIS = 1.0;
    
    private static PrintWriter csvWriter;
    
    public static void main(String[] args) {
        System.out.println("═".repeat(80));
        System.out.println("          BENCHMARKS - VARIAÇÃO DE TAMANHOS DE PROBLEMA");
        System.out.println("═".repeat(80));
        System.out.println("Configuração:");
        System.out.println("  Repetições por teste: " + REPETICOES);
        System.out.println("  Tamanhos de população: " + arrayToString(TAMANHOS_POPULACAO));
        System.out.println("  Tamanhos de passos: " + arrayToString(TAMANHOS_PASSOS));
        System.out.println("  Tamanhos de cenários: " + arrayToString(TAMANHOS_CENARIOS));
        System.out.println("═".repeat(80));
        System.out.println();
        
        try {
            inicializarCSV();
            
            // Testes SIR - Variação de População e Passos
            System.out.println("\n┌─ TESTES SIR - VARIAÇÃO DE POPULAÇÃO E PASSOS ─────────────┐");
            testarSIRVariandoTamanho();
            System.out.println("└────────────────────────────────────────────────────────────┘\n");
            
            // Testes SIR - Variação de Cenários
            System.out.println("\n┌─ TESTES SIR CENÁRIOS - VARIAÇÃO DE NÚMERO DE CENÁRIOS ────┐");
            testarSIRCenariosVariandoTamanho();
            System.out.println("└────────────────────────────────────────────────────────────┘\n");
            
            // Testes SIS - Variação de População e Passos
            System.out.println("\n┌─ TESTES SIS - VARIAÇÃO DE POPULAÇÃO E PASSOS ─────────────┐");
            testarSISVariandoTamanho();
            System.out.println("└────────────────────────────────────────────────────────────┘\n");
            
            // Testes SIS - Variação de Cenários
            System.out.println("\n┌─ TESTES SIS CENÁRIOS - VARIAÇÃO DE NÚMERO DE CENÁRIOS ────┐");
            testarSISCenariosVariandoTamanho();
            System.out.println("└────────────────────────────────────────────────────────────┘\n");
            
            csvWriter.close();
            
            System.out.println("\n═".repeat(80));
            System.out.println("  ✓ Benchmarks concluídos com sucesso!");
            System.out.println("  ✓ Resultados salvos em: " + ARQUIVO_CSV);
            System.out.println("═".repeat(80));
            
        } catch (IOException e) {
            System.err.println("Erro ao escrever arquivo CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void inicializarCSV() throws IOException {
        csvWriter = new PrintWriter(new FileWriter(ARQUIVO_CSV));
        // Cabeçalho do CSV
        csvWriter.println("Timestamp,Modelo,Tipo,Populacao,Passos,Cenarios,Threads,Repeticao,Tempo_ms");
    }
    
    private static void testarSIRVariandoTamanho() {
        // Testa com diferentes números de threads
        int[] numerosThreads = {1, 2, 4, 8};
        
        for (double populacao : TAMANHOS_POPULACAO) {
            for (int passos : TAMANHOS_PASSOS) {
                double tempoMaximo = passos / 100.0; // Mantém proporção
                
                System.out.printf("\n  Testando: População=%.0f, Passos=%d\n", populacao, passos);
                
                // SIR Sequencial
                System.out.print("    • SIR Sequencial...");
                for (int rep = 1; rep <= REPETICOES; rep++) {
                    SIRSequencial sirSeq = new SIRSequencial(
                        populacao, TAXA_TRANSMISSAO_SIR, TAXA_RECUPERACAO_SIR,
                        INFECTADOS_INICIAIS_SIR, RECUPERADOS_INICIAIS_SIR, tempoMaximo, passos
                    );
                    double tempo = sirSeq.executarSimulacao();
                    gravarResultado("SIR", "Sequencial", populacao, passos, 0, 1, rep, tempo);
                }
                System.out.println(" ✓");
                
                // SIR Paralelo com diferentes números de threads
                for (int numThreads : numerosThreads) {
                    System.out.printf("    • SIR Paralelo (%d threads)...", numThreads);
                    for (int rep = 1; rep <= REPETICOES; rep++) {
                        SIRParalelo sirPar = new SIRParalelo(
                            populacao, TAXA_TRANSMISSAO_SIR, TAXA_RECUPERACAO_SIR,
                            INFECTADOS_INICIAIS_SIR, RECUPERADOS_INICIAIS_SIR, tempoMaximo, passos, numThreads
                        );
                        double tempo = sirPar.executarSimulacao();
                        gravarResultado("SIR", "Paralelo", populacao, passos, 0, numThreads, rep, tempo);
                    }
                    System.out.println(" ✓");
                }
            }
        }
    }
    
    private static void testarSIRCenariosVariandoTamanho() {
        int numeroThreads = Runtime.getRuntime().availableProcessors();
        double populacao = 1000000.0; // População fixa para testes de cenários
        int passos = 50000;
        double tempoMaximo = 500.0;
        
        for (int numeroCenarios : TAMANHOS_CENARIOS) {
            System.out.printf("\n  Testando: Cenários=%d\n", numeroCenarios);
            
            // SIR Cenários Sequencial
            System.out.print("    • SIR Cenários Sequencial...");
            for (int rep = 1; rep <= REPETICOES; rep++) {
                cenarios.CenariosSequencialSIR sirCenSeq = new cenarios.CenariosSequencialSIR(
                    populacao, INFECTADOS_INICIAIS_SIR, RECUPERADOS_INICIAIS_SIR,
                    TAXA_RECUPERACAO_SIR, tempoMaximo, passos, numeroCenarios
                );
                double tempo = sirCenSeq.executarSimulacao();
                gravarResultado("SIR", "Cenarios_Sequencial", populacao, passos, numeroCenarios, 1, rep, tempo);
            }
            System.out.println(" ✓");
            
            // SIR Cenários Paralelo
            System.out.print("    • SIR Cenários Paralelo...");
            for (int rep = 1; rep <= REPETICOES; rep++) {
                cenarios.CenariosParaleloSIR sirCenPar = new cenarios.CenariosParaleloSIR(
                    populacao, INFECTADOS_INICIAIS_SIR, RECUPERADOS_INICIAIS_SIR,
                    TAXA_RECUPERACAO_SIR, tempoMaximo, passos, numeroCenarios, numeroThreads
                );
                double tempo = sirCenPar.executarSimulacao();
                gravarResultado("SIR", "Cenarios_Paralelo", populacao, passos, numeroCenarios, numeroThreads, rep, tempo);
            }
            System.out.println(" ✓");
        }
    }
    
    private static void testarSISVariandoTamanho() {
        // Testa com diferentes números de threads
        int[] numerosThreads = {1, 2, 4, 8};
        
        // Para SIS, usamos as MESMAS populações do SIR para consistência
        double[] populacoesSIS = TAMANHOS_POPULACAO; // 100k, 500k, 1M, 2M
        int[] passosSIS = TAMANHOS_PASSOS; // 10k, 25k, 50k
        
        for (double populacao : populacoesSIS) {
            for (int passos : passosSIS) {
                double tempoMaximo = passos / 100.0; // Mantém proporção
                
                System.out.printf("\n  Testando: População=%.0f, Passos=%d\n", populacao, passos);
                
                // SIS Sequencial
                System.out.print("    • SIS Sequencial...");
                for (int rep = 1; rep <= REPETICOES; rep++) {
                    SISSequencial sisSeq = new SISSequencial(
                        populacao, TAXA_TRANSMISSAO_SIS, TAXA_RECUPERACAO_SIS,
                        INFECTADOS_INICIAIS_SIS, tempoMaximo, passos
                    );
                    double tempo = sisSeq.executarSimulacao();
                    gravarResultado("SIS", "Sequencial", populacao, passos, 0, 1, rep, tempo);
                }
                System.out.println(" ✓");
                
                // SIS Paralelo com diferentes números de threads
                for (int numThreads : numerosThreads) {
                    System.out.printf("    • SIS Paralelo (%d threads)...", numThreads);
                    for (int rep = 1; rep <= REPETICOES; rep++) {
                        SISParalelo sisPar = new SISParalelo(
                            populacao, TAXA_TRANSMISSAO_SIS, TAXA_RECUPERACAO_SIS,
                            INFECTADOS_INICIAIS_SIS, tempoMaximo, passos, numThreads
                        );
                        double tempo = sisPar.executarSimulacao();
                        gravarResultado("SIS", "Paralelo", populacao, passos, 0, numThreads, rep, tempo);
                    }
                    System.out.println(" ✓");
                }
            }
        }
    }
    
    private static void testarSISCenariosVariandoTamanho() {
        int numeroThreads = Runtime.getRuntime().availableProcessors();
        double populacao = 1000000.0; // Mesma população do SIR
        int passos = 50000;
        double tempoMaximo = 100.0;
        
        for (int numeroCenarios : TAMANHOS_CENARIOS) {
            System.out.printf("\n  Testando: Cenários=%d\n", numeroCenarios);
            
            // SIS Cenários Sequencial
            System.out.print("    • SIS Cenários Sequencial...");
            for (int rep = 1; rep <= REPETICOES; rep++) {
                cenarios.CenariosSequencialSIS sisCenSeq = new cenarios.CenariosSequencialSIS(
                    populacao, INFECTADOS_INICIAIS_SIS, TAXA_RECUPERACAO_SIS,
                    tempoMaximo, passos, numeroCenarios
                );
                double tempo = sisCenSeq.executarSimulacao();
                gravarResultado("SIS", "Cenarios_Sequencial", populacao, passos, numeroCenarios, 1, rep, tempo);
            }
            System.out.println(" ✓");
            
            // SIS Cenários Paralelo
            System.out.print("    • SIS Cenários Paralelo...");
            for (int rep = 1; rep <= REPETICOES; rep++) {
                cenarios.CenariosParaleloSIS sisCenPar = new cenarios.CenariosParaleloSIS(
                    populacao, INFECTADOS_INICIAIS_SIS, TAXA_RECUPERACAO_SIS,
                    tempoMaximo, passos, numeroCenarios, numeroThreads
                );
                double tempo = sisCenPar.executarSimulacao();
                gravarResultado("SIS", "Cenarios_Paralelo", populacao, passos, numeroCenarios, numeroThreads, rep, tempo);
            }
            System.out.println(" ✓");
        }
    }
    
    private static void gravarResultado(String modelo, String tipo, double populacao, 
                                       int passos, int cenarios, int threads, int repeticao, double tempo) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        csvWriter.printf(Locale.US, "%s,%s,%s,%.0f,%d,%d,%d,%d,%.4f\n",
            timestamp, modelo, tipo, populacao, passos, cenarios, threads, repeticao, tempo);
        csvWriter.flush(); // Garante gravação imediata
    }
    
    private static String arrayToString(double[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(String.format("%.0f", arr[i]));
            if (i < arr.length - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
    
    private static String arrayToString(int[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
