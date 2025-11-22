
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * Implementação Paralela do Modelo SIR (RK4) com Paralelização por População.
 * Divide a população em blocos e cada thread simula um bloco independente.
 */
public class SIRParalelo {

    // Parâmetros da simulação
    private final double populacaoTotal;
    private final double taxaTransmissao;
    private final double taxaRecuperacao;
    private final double infectadosIniciais;
    private final double recuperadosIniciais;
    private final double tempoMaximo;
    private final int numeroPassos;
    private final int numeroThreads;

    // Construtor
    public SIRParalelo(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao, 
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

    // --- 1. Função de Derivada (Calcula as 3 EDOs) ---
    private static double[] derivSir(double[] estadoAtual, double tempo, double populacaoTotal, 
                                     double taxaTransmissao, double taxaRecuperacao) {
        double suscetiveis = estadoAtual[0];
        double infectados = estadoAtual[1];

        double derivadaSuscetiveis = -taxaTransmissao * suscetiveis * infectados / populacaoTotal;
        double derivadaInfectados = taxaTransmissao * suscetiveis * infectados / populacaoTotal - taxaRecuperacao * infectados;
        double derivadaRecuperados = taxaRecuperacao * infectados;

        return new double[]{derivadaSuscetiveis, derivadaInfectados, derivadaRecuperados};
    }

    // --- 2. Tarefa para simular um bloco da população ---
    static class TarefaSimulacaoBloco implements Callable<double[][]> {
        private final double populacaoBloco;
        private final double taxaTransmissao;
        private final double taxaRecuperacao;
        private final double infectadosIniciais;
        private final double recuperadosIniciais;
        private final double tempoMaximo;
        private final int numeroPassos;

        public TarefaSimulacaoBloco(double populacaoBloco, double taxaTransmissao, double taxaRecuperacao,
                                    double infectadosIniciais, double recuperadosIniciais,
                                    double tempoMaximo, int numeroPassos) {
            this.populacaoBloco = populacaoBloco;
            this.taxaTransmissao = taxaTransmissao;
            this.taxaRecuperacao = taxaRecuperacao;
            this.infectadosIniciais = infectadosIniciais;
            this.recuperadosIniciais = recuperadosIniciais;
            this.tempoMaximo = tempoMaximo;
            this.numeroPassos = numeroPassos;
        }

        @Override
        public double[][] call() {
            return rungeKutta4Sequencial(populacaoBloco, taxaTransmissao, taxaRecuperacao,
                                         infectadosIniciais, recuperadosIniciais, tempoMaximo, numeroPassos);
        }
    }

    // --- 3. Solver RK4 Sequencial (usado por cada thread) ---
    private static double[][] rungeKutta4Sequencial(double populacaoTotal, double taxaTransmissao, 
                                                    double taxaRecuperacao, double infectadosIniciais,
                                                    double recuperadosIniciais, double tempoMaximo, int numeroPassos) {
        double suscetiveisIniciais = populacaoTotal - infectadosIniciais - recuperadosIniciais;
        double[] estadoAtual = {suscetiveisIniciais, infectadosIniciais, recuperadosIniciais};
        int numeroCompartimentos = 3;
        double incrementoTempo = tempoMaximo / (numeroPassos - 1);
        double[][] historico = new double[numeroPassos][numeroCompartimentos];
        historico[0] = Arrays.copyOf(estadoAtual, numeroCompartimentos);

        for (int passo = 0; passo < numeroPassos - 1; passo++) {
            double tempoAtual = passo * incrementoTempo;
            
            // K1
            double[] k1 = derivSir(estadoAtual, tempoAtual, populacaoTotal, taxaTransmissao, taxaRecuperacao);
            
            // K2
            double[] estadoK1 = new double[numeroCompartimentos];
            for (int i = 0; i < numeroCompartimentos; i++) {
                estadoK1[i] = estadoAtual[i] + 0.5 * incrementoTempo * k1[i];
            }
            double[] k2 = derivSir(estadoK1, tempoAtual + incrementoTempo / 2, populacaoTotal, taxaTransmissao, taxaRecuperacao);
            
            // K3
            double[] estadoK2 = new double[numeroCompartimentos];
            for (int i = 0; i < numeroCompartimentos; i++) {
                estadoK2[i] = estadoAtual[i] + 0.5 * incrementoTempo * k2[i];
            }
            double[] k3 = derivSir(estadoK2, tempoAtual + incrementoTempo / 2, populacaoTotal, taxaTransmissao, taxaRecuperacao);
            
            // K4
            double[] estadoK3 = new double[numeroCompartimentos];
            for (int i = 0; i < numeroCompartimentos; i++) {
                estadoK3[i] = estadoAtual[i] + incrementoTempo * k3[i];
            }
            double[] k4 = derivSir(estadoK3, tempoAtual + incrementoTempo, populacaoTotal, taxaTransmissao, taxaRecuperacao);
            
            // Atualização RK4
            for (int i = 0; i < numeroCompartimentos; i++) {
                estadoAtual[i] += incrementoTempo * (k1[i] + 2*k2[i] + 2*k3[i] + k4[i]) / 6.0;
                if (estadoAtual[i] < 0) estadoAtual[i] = 0;
            }
            
            historico[passo + 1] = Arrays.copyOf(estadoAtual, numeroCompartimentos);
        }
        
        return historico;
    }

    // --- 4. Método para executar simulação com paralelização por população ---
    public double executarSimulacao() {
        long tempoInicio = System.nanoTime();
        
        ExecutorService executor = Executors.newFixedThreadPool(numeroThreads);
        List<Future<double[][]>> futuros = new ArrayList<>();
        
        // Divide a população em blocos
        double populacaoPorThread = populacaoTotal / numeroThreads;
        double infectadosPorThread = infectadosIniciais / numeroThreads;
        double recuperadosPorThread = recuperadosIniciais / numeroThreads;
        
        // Submete tarefas para cada bloco de população
        for (int i = 0; i < numeroThreads; i++) {
            TarefaSimulacaoBloco tarefa = new TarefaSimulacaoBloco(
                populacaoPorThread, taxaTransmissao, taxaRecuperacao,
                infectadosPorThread, recuperadosPorThread, tempoMaximo, numeroPassos
            );
            futuros.add(executor.submit(tarefa));
        }
        
        // Agrega resultados de todos os blocos
        try {
            double[][] resultadoAgregado = new double[numeroPassos][3];
            
            for (Future<double[][]> futuro : futuros) {
                double[][] resultadoBloco = futuro.get();
                for (int passo = 0; passo < numeroPassos; passo++) {
                    for (int comp = 0; comp < 3; comp++) {
                        resultadoAgregado[passo][comp] += resultadoBloco[passo][comp];
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
        
        long tempoFim = System.nanoTime();
        return (tempoFim - tempoInicio) / 1_000_000.0;
    }

    // --- 5. Main para Execução ---
    public static void main(String[] args) {
        SIRParalelo simulacao = new SIRParalelo(1000000.0, 0.2, 1.0 / 10.0, 10.0, 0.0, 500.0, 50000, 8);
        double tempoExecucao = simulacao.executarSimulacao();
        System.out.printf("Tempo de execução: %.4f ms\n", tempoExecucao);
    }
}
