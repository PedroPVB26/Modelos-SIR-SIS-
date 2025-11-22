import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * Implementação do Modelo SIR com Paralelização por POPULAÇÃO
 * Divide a população em blocos e simula cada bloco em uma thread separada
 */
public class SIRParaleloPopulacao {

    private final double populacaoTotal;
    private final double taxaTransmissao;
    private final double taxaRecuperacao;
    private final double infectadosIniciais;
    private final double recuperadosIniciais;
    private final double tempoMaximo;
    private final int numeroPassos;
    private final int numeroThreads;

    public SIRParaleloPopulacao(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao,
                                double infectadosIniciais, double recuperadosIniciais,
                                double tempoMaximo, int numeroPassos, int numeroThreads) {
        this.populacaoTotal = populacaoTotal;
        this.taxaTransmissao = taxaTransmissao;
        this.taxaRecuperacao = taxaRecuperacao;
        this.infectadosIniciais = infectadosIniciais;
        this.recuperadosIniciais = recuperadosIniciais;
        this.tempoMaximo = tempoMaximo;
        this.numeroPassos = numeroPassos;
        this.numeroThreads = numeroThreads;
    }

    // Função de derivada SIR
    private static double[] derivSir(double[] estado, double populacaoTotal, double taxaTransmissao, double taxaRecuperacao) {
        double S = estado[0];
        double I = estado[1];
        
        double dS = -taxaTransmissao * S * I / populacaoTotal;
        double dI = taxaTransmissao * S * I / populacaoTotal - taxaRecuperacao * I;
        double dR = taxaRecuperacao * I;
        
        return new double[]{dS, dI, dR};
    }

    // Tarefa para simular um bloco da população
    static class TarefaSimulacaoBloco implements Callable<double[][]> {
        private final double populacaoBloco;
        private final double proporcaoInfectados;
        private final double proporcaoRecuperados;
        private final double populacaoTotal;
        private final double taxaTransmissao;
        private final double taxaRecuperacao;
        private final double tempoMaximo;
        private final int numeroPassos;

        public TarefaSimulacaoBloco(double populacaoBloco, double proporcaoInfectados, double proporcaoRecuperados,
                                    double populacaoTotal, double taxaTransmissao, double taxaRecuperacao,
                                    double tempoMaximo, int numeroPassos) {
            this.populacaoBloco = populacaoBloco;
            this.proporcaoInfectados = proporcaoInfectados;
            this.proporcaoRecuperados = proporcaoRecuperados;
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
            double recuperadosBloco = populacaoBloco * proporcaoRecuperados;
            double suscetiveisBloco = populacaoBloco - infectadosBloco - recuperadosBloco;
            
            double[] estadoAtual = {suscetiveisBloco, infectadosBloco, recuperadosBloco};
            double incrementoTempo = tempoMaximo / (numeroPassos - 1);
            double[][] historico = new double[numeroPassos][3];
            historico[0] = Arrays.copyOf(estadoAtual, 3);

            // Simula este bloco usando RK4
            for (int passo = 0; passo < numeroPassos - 1; passo++) {
                double h = incrementoTempo;
                
                // K1
                double[] k1 = derivSir(estadoAtual, populacaoTotal, taxaTransmissao, taxaRecuperacao);
                
                // K2
                double[] temp = new double[3];
                for (int i = 0; i < 3; i++) temp[i] = estadoAtual[i] + h * k1[i] / 2.0;
                double[] k2 = derivSir(temp, populacaoTotal, taxaTransmissao, taxaRecuperacao);
                
                // K3
                for (int i = 0; i < 3; i++) temp[i] = estadoAtual[i] + h * k2[i] / 2.0;
                double[] k3 = derivSir(temp, populacaoTotal, taxaTransmissao, taxaRecuperacao);
                
                // K4
                for (int i = 0; i < 3; i++) temp[i] = estadoAtual[i] + h * k3[i];
                double[] k4 = derivSir(temp, populacaoTotal, taxaTransmissao, taxaRecuperacao);
                
                // Atualiza estado
                for (int i = 0; i < 3; i++) {
                    estadoAtual[i] += h * (k1[i] + 2*k2[i] + 2*k3[i] + k4[i]) / 6.0;
                    if (estadoAtual[i] < 0) estadoAtual[i] = 0;
                }
                
                historico[passo + 1] = Arrays.copyOf(estadoAtual, 3);
            }
            
            return historico;
        }
    }

    public double[][] executarSimulacao() {
        double suscetiveisIniciais = populacaoTotal - infectadosIniciais - recuperadosIniciais;
        double proporcaoInfectados = infectadosIniciais / populacaoTotal;
        double proporcaoRecuperados = recuperadosIniciais / populacaoTotal;
        
        ExecutorService executor = Executors.newFixedThreadPool(numeroThreads);
        List<Future<double[][]>> futuros = new ArrayList<>();
        
        // Divide população em blocos
        double populacaoPorThread = populacaoTotal / numeroThreads;
        
        for (int t = 0; t < numeroThreads; t++) {
            futuros.add(executor.submit(new TarefaSimulacaoBloco(
                populacaoPorThread,
                proporcaoInfectados,
                proporcaoRecuperados,
                populacaoTotal,
                taxaTransmissao,
                taxaRecuperacao,
                tempoMaximo,
                numeroPassos
            )));
        }
        
        // Agrega resultados
        double[][] resultadoFinal = new double[numeroPassos][3];
        
        try {
            for (Future<double[][]> futuro : futuros) {
                double[][] historicoBloco = futuro.get();
                
                for (int passo = 0; passo < numeroPassos; passo++) {
                    for (int comp = 0; comp < 3; comp++) {
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
        System.out.println("=== SIR - Paralelização por População ===\n");
        
        // Teste com mesma configuração do SIRParalelo original
        SIRParaleloPopulacao sim = new SIRParaleloPopulacao(
            1000000.0, 0.2, 1.0/10.0, 10.0, 0.0, 500.0, 50000, 8
        );
        
        // Aquece JVM
        sim.executarSimulacao();
        
        // Mede tempo
        long inicio = System.nanoTime();
        sim.executarSimulacao();
        long fim = System.nanoTime();
        
        double tempoMs = (fim - inicio) / 1_000_000.0;
        System.out.printf("Tempo de execução: %.4f ms\n", tempoMs);
    }
}
