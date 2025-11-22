import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * Implementação do Modelo SIS com Paralelização por POPULAÇÃO
 * Divide a população em blocos e simula cada bloco em uma thread separada
 */
public class SISParaleloPopulacao {

    private final double populacaoTotal;
    private final double taxaTransmissao;
    private final double taxaRecuperacao;
    private final double infectadosIniciais;
    private final double tempoMaximo;
    private final int numeroPassos;
    private final int numeroThreads;

    public SISParaleloPopulacao(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao,
                                double infectadosIniciais, double tempoMaximo, int numeroPassos, int numeroThreads) {
        this.populacaoTotal = populacaoTotal;
        this.taxaTransmissao = taxaTransmissao;
        this.taxaRecuperacao = taxaRecuperacao;
        this.infectadosIniciais = infectadosIniciais;
        this.tempoMaximo = tempoMaximo;
        this.numeroPassos = numeroPassos;
        this.numeroThreads = numeroThreads;
    }

    // Função de derivada SIS
    private static double[] derivSis(double[] estado, double populacaoTotal, double taxaTransmissao, double taxaRecuperacao) {
        double S = estado[0];
        double I = estado[1];
        
        double dS = -taxaTransmissao * S * I / populacaoTotal + taxaRecuperacao * I;
        double dI = taxaTransmissao * S * I / populacaoTotal - taxaRecuperacao * I;
        
        return new double[]{dS, dI};
    }

    // Tarefa para simular um bloco da população
    static class TarefaSimulacaoBloco implements Callable<double[][]> {
        private final double populacaoBloco;
        private final double proporcaoInfectados;
        private final double populacaoTotal;
        private final double taxaTransmissao;
        private final double taxaRecuperacao;
        private final double tempoMaximo;
        private final int numeroPassos;

        public TarefaSimulacaoBloco(double populacaoBloco, double proporcaoInfectados,
                                    double populacaoTotal, double taxaTransmissao, double taxaRecuperacao,
                                    double tempoMaximo, int numeroPassos) {
            this.populacaoBloco = populacaoBloco;
            this.proporcaoInfectados = proporcaoInfectados;
            this.populacaoTotal = populacaoTotal;
            this.taxaTransmissao = taxaTransmissao;
            this.taxaRecuperacao = taxaRecuperacao;
            this.tempoMaximo = tempoMaximo;
            this.numeroPassos = numeroPassos;
        }

        @Override
        public double[][] call() {
            // Estado inicial para este bloco
            double infectadosBloco = populacaoBloco * proporcaoInfectados;
            double suscetiveisBloco = populacaoBloco - infectadosBloco;
            
            double[] estadoAtual = {suscetiveisBloco, infectadosBloco};
            double incrementoTempo = tempoMaximo / (numeroPassos - 1);
            double[][] historico = new double[numeroPassos][2];
            historico[0] = Arrays.copyOf(estadoAtual, 2);

            // Simula este bloco usando RK4
            for (int passo = 0; passo < numeroPassos - 1; passo++) {
                double h = incrementoTempo;
                
                // K1
                double[] k1 = derivSis(estadoAtual, populacaoTotal, taxaTransmissao, taxaRecuperacao);
                
                // K2
                double[] temp = new double[2];
                for (int i = 0; i < 2; i++) temp[i] = estadoAtual[i] + h * k1[i] / 2.0;
                double[] k2 = derivSis(temp, populacaoTotal, taxaTransmissao, taxaRecuperacao);
                
                // K3
                for (int i = 0; i < 2; i++) temp[i] = estadoAtual[i] + h * k2[i] / 2.0;
                double[] k3 = derivSis(temp, populacaoTotal, taxaTransmissao, taxaRecuperacao);
                
                // K4
                for (int i = 0; i < 2; i++) temp[i] = estadoAtual[i] + h * k3[i];
                double[] k4 = derivSis(temp, populacaoTotal, taxaTransmissao, taxaRecuperacao);
                
                // Atualiza estado
                for (int i = 0; i < 2; i++) {
                    estadoAtual[i] += h * (k1[i] + 2*k2[i] + 2*k3[i] + k4[i]) / 6.0;
                    if (estadoAtual[i] < 0) estadoAtual[i] = 0;
                }
                
                historico[passo + 1] = Arrays.copyOf(estadoAtual, 2);
            }
            
            return historico;
        }
    }

    public double[][] executarSimulacao() {
        double proporcaoInfectados = infectadosIniciais / populacaoTotal;
        
        ExecutorService executor = Executors.newFixedThreadPool(numeroThreads);
        List<Future<double[][]>> futuros = new ArrayList<>();
        
        // Divide população em blocos
        double populacaoPorThread = populacaoTotal / numeroThreads;
        
        for (int t = 0; t < numeroThreads; t++) {
            futuros.add(executor.submit(new TarefaSimulacaoBloco(
                populacaoPorThread,
                proporcaoInfectados,
                populacaoTotal,
                taxaTransmissao,
                taxaRecuperacao,
                tempoMaximo,
                numeroPassos
            )));
        }
        
        // Agrega resultados
        double[][] resultadoFinal = new double[numeroPassos][2];
        
        try {
            for (Future<double[][]> futuro : futuros) {
                double[][] historicoBloco = futuro.get();
                
                for (int passo = 0; passo < numeroPassos; passo++) {
                    for (int comp = 0; comp < 2; comp++) {
                        resultadoFinal[passo][comp] += historicoBloco[passo][comp];
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        executor.shutdown();
        return resultadoFinal;
    }

    public static void main(String[] args) {
        System.out.println("=== SIS - Paralelização por População ===\n");
        
        // Testa com diferentes configurações
        double[] populacoes = {1000, 5000, 10000, 50000};
        int[] threads = {1, 2, 4, 8};
        
        for (double pop : populacoes) {
            System.out.printf("\n--- População: %.0f ---\n", pop);
            
            for (int numThreads : threads) {
                SISParaleloPopulacao sim = new SISParaleloPopulacao(
                    pop, 0.3, 0.1, 1.0, 100.0, 1001, numThreads
                );
                
                // Aquece JVM
                sim.executarSimulacao();
                
                // Mede tempo
                long inicio = System.nanoTime();
                sim.executarSimulacao();
                long fim = System.nanoTime();
                
                double tempoMs = (fim - inicio) / 1_000_000.0;
                System.out.printf("  Threads: %d | Tempo: %.4f ms\n", numThreads, tempoMs);
            }
        }
    }
}
