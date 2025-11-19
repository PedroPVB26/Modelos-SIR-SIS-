
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;

/**
 * Implementação Simplificada e Paralela do Modelo SIR (RK4) com Paralelismo no
 * Lado Direito (RHS) usando ExecutorService.
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

    // Construtor
    public SIRParalelo(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao, 
                       double infectadosIniciais, double recuperadosIniciais, 
                       double tempoMaximo, int numeroPassos) {
        this.populacaoTotal = populacaoTotal;
        this.taxaTransmissao = taxaTransmissao;
        this.taxaRecuperacao = taxaRecuperacao;
        this.infectadosIniciais = infectadosIniciais;
        this.recuperadosIniciais = recuperadosIniciais;
        this.tempoMaximo = tempoMaximo;
        this.numeroPassos = numeroPassos;
    }

    // --- 1. Função de Derivada (Calcula as 3 EDOs) ---
    public static double[] derivSir(double[] estadoAtual, double tempo, double populacaoTotal, double taxaTransmissao, double taxaRecuperacao) {
        double suscetiveis = estadoAtual[0];
        double infectados = estadoAtual[1];
        // double recuperados = estadoAtual[2]; // recuperados não é explicitamente necessário para as derivadas

        double derivadaSuscetiveis = -taxaTransmissao * suscetiveis * infectados / populacaoTotal;
        double derivadaInfectados = taxaTransmissao * suscetiveis * infectados / populacaoTotal - taxaRecuperacao * infectados;
        double derivadaRecuperados = taxaRecuperacao * infectados;

        return new double[]{derivadaSuscetiveis, derivadaInfectados, derivadaRecuperados};
    }

    // --- 2. Tarefa (Callable) para o cálculo de uma componente da derivada ---
    static class TarefaDerivada implements Callable<Double> {

        private final int indiceCompartimento;
        private final double[] estadoAtual;
        private final double tempo, populacaoTotal, taxaTransmissao, taxaRecuperacao;

        public TarefaDerivada(int indiceCompartimento, double[] estadoAtual, double tempo, double populacaoTotal, double taxaTransmissao, double taxaRecuperacao) {
            this.indiceCompartimento = indiceCompartimento;
            this.estadoAtual = estadoAtual;
            this.tempo = tempo;
            this.populacaoTotal = populacaoTotal;
            this.taxaTransmissao = taxaTransmissao;
            this.taxaRecuperacao = taxaRecuperacao;
        }

        @Override
        public Double call() {
            // Retorna apenas a componente (dS/dt, dI/dt ou dR/dt) solicitada
            return derivSir(estadoAtual, tempo, populacaoTotal, taxaTransmissao, taxaRecuperacao)[this.indiceCompartimento];
        }
    }

    // --- 3. Solver RK4 Paralelo Simplificado ---
    public static double[][] rungeKutka4Paralelo(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao, double infectadosIniciais, double recuperadosIniciais, double tempoMaximo,
            int numeroPassos) {
        double suscetiveisIniciais = populacaoTotal - infectadosIniciais - recuperadosIniciais;
        double[] estadoAtual = {suscetiveisIniciais, infectadosIniciais, recuperadosIniciais};
        int numeroCompartimentos = estadoAtual.length;
        double incrementoTempo = tempoMaximo / (numeroPassos - 1);
        double[][] historico = new double[numeroPassos][numeroCompartimentos];
        historico[0] = Arrays.copyOf(estadoAtual, numeroCompartimentos);

        // Cria pool de threads
        ExecutorService executor = Executors.newFixedThreadPool(numeroCompartimentos);

        long tempoInicio = System.nanoTime();

        for (int passo = 0; passo < numeroPassos - 1; passo++) {
            double tempoAtual = passo * incrementoTempo;
            double[] coeficienteK1 = new double[numeroCompartimentos];
            double[] coeficienteK2, coeficienteK3, coeficienteK4;

            try {
                // --- K1: PARALELO (O coração da simplificação RHS) ---
                ArrayList<Future<Double>> futuros = new ArrayList<>();
                for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                    // Submete 3 tarefas (S, I, R)
                    futuros.add(executor.submit(new TarefaDerivada(compartimento, estadoAtual, tempoAtual, populacaoTotal, taxaTransmissao, taxaRecuperacao)));
                }
                for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                    // Sincroniza e coleta os resultados (derivada * incrementoTempo)
                    coeficienteK1[compartimento] = futuros.get(compartimento).get() * incrementoTempo;
                }

                // --- K2, K3, K4: Sequenciais para simplicidade (como na versão original) ---
                // K2
                double[] estadoIntermediarioK1 = new double[numeroCompartimentos];
                for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                    estadoIntermediarioK1[compartimento] = estadoAtual[compartimento] + 0.5 * coeficienteK1[compartimento];
                }
                double[] derivada2 = derivSir(estadoIntermediarioK1, tempoAtual + incrementoTempo / 2, populacaoTotal, taxaTransmissao, taxaRecuperacao);
                coeficienteK2 = new double[numeroCompartimentos];
                for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                    coeficienteK2[compartimento] = derivada2[compartimento] * incrementoTempo;
                }

                // K3
                double[] estadoIntermediarioK2 = new double[numeroCompartimentos];
                for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                    estadoIntermediarioK2[compartimento] = estadoAtual[compartimento] + 0.5 * coeficienteK2[compartimento];
                }
                double[] derivada3 = derivSir(estadoIntermediarioK2, tempoAtual + incrementoTempo / 2, populacaoTotal, taxaTransmissao, taxaRecuperacao);
                coeficienteK3 = new double[numeroCompartimentos];
                for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                    coeficienteK3[compartimento] = derivada3[compartimento] * incrementoTempo;
                }

                // K4
                double[] estadoIntermediarioK3 = new double[numeroCompartimentos];
                for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                    estadoIntermediarioK3[compartimento] = estadoAtual[compartimento] + coeficienteK3[compartimento];
                }
                double[] derivada4 = derivSir(estadoIntermediarioK3, tempoAtual + incrementoTempo, populacaoTotal, taxaTransmissao, taxaRecuperacao);
                coeficienteK4 = new double[numeroCompartimentos];
                for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                    coeficienteK4[compartimento] = derivada4[compartimento] * incrementoTempo;
                }

            } catch (Exception e) {
                // Lidar com exceções
                executor.shutdownNow();
                return historico;
            }

            // --- Passo Final RK4 (Sequencial) ---
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                // estadoAtual_{i+1} = estadoAtual_i + 1/6 * (k1 + 2*k2 + 2*k3 + k4)
                estadoAtual[compartimento] += (coeficienteK1[compartimento] + 2.0 * coeficienteK2[compartimento] + 2.0 * coeficienteK3[compartimento] + coeficienteK4[compartimento]) / 6.0;

                // Garante que os valores sejam não negativos
                if (estadoAtual[compartimento] < 0) {
                    estadoAtual[compartimento] = 0;
                }
            }

            historico[passo + 1] = Arrays.copyOf(estadoAtual, numeroCompartimentos);
        }

        executor.shutdown();
        long tempoFim = System.nanoTime();
        double tempoDecorridoMs = (tempoFim - tempoInicio) / 1_000_000.0;
        System.out.printf("Simulação Paralela (RHS) simplificada em: %.4f milissegundos\n", tempoDecorridoMs);

        return historico;
    }

    // --- 4. Método público para executar simulação ---
    public double executarSimulacao() {
        long tempoInicio = System.nanoTime();
        rungeKutka4Paralelo(populacaoTotal, taxaTransmissao, taxaRecuperacao, 
                            infectadosIniciais, recuperadosIniciais, tempoMaximo, numeroPassos);
        long tempoFim = System.nanoTime();
        return (tempoFim - tempoInicio) / 1_000_000.0;
    }

    // --- 5. Main para Execução ---
    public static void main(String[] args) {
        // Parâmetros (os mesmos para manter a comparação de desempenho)
        SIRParalelo simulacao = new SIRParalelo(1000000.0, 0.2, 1.0 / 10.0, 10.0, 0.0, 500.0, 50000);
        double tempoExecucao = simulacao.executarSimulacao();
        System.out.printf("Tempo de execução: %.4f ms\n", tempoExecucao);
    }
}
