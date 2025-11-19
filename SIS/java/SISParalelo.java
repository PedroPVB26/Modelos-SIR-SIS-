
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;

/**
 * Implementação Simplificada e Paralela do Modelo SIS (RK4) com Paralelismo no
 * Lado Direito (RHS) usando ExecutorService. Cada componente da derivada
 * (dS/dt, dI/dt) é calculada por uma thread.
 */
public class SISParalelo {

    // Parâmetros da simulação
    private final double populacaoTotal;
    private final double taxaTransmissao;
    private final double taxaRecuperacao;
    private final double infectadosIniciais;
    private final double tempoMaximo;
    private final int numeroPassos;

    // Construtor
    public SISParalelo(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao,
                       double infectadosIniciais, double tempoMaximo, int numeroPassos) {
        this.populacaoTotal = populacaoTotal;
        this.taxaTransmissao = taxaTransmissao;
        this.taxaRecuperacao = taxaRecuperacao;
        this.infectadosIniciais = infectadosIniciais;
        this.tempoMaximo = tempoMaximo;
        this.numeroPassos = numeroPassos;
    }

    // --- 1. Função de Derivada (Calcula as 2 EDOs do SIS) ---
    public static double[] derivSis(double[] estadoAtual, double tempo, double populacaoTotal, double taxaTransmissao, double taxaRecuperacao) {
        double suscetiveis = estadoAtual[0];
        double infectados = estadoAtual[1];

        // Equações SIS (Suscetível-Infectado-Suscetível)
        double derivadaSuscetiveis = -taxaTransmissao * suscetiveis * infectados / populacaoTotal + taxaRecuperacao * infectados;
        double derivadaInfectados = taxaTransmissao * suscetiveis * infectados / populacaoTotal - taxaRecuperacao * infectados;

        return new double[]{derivadaSuscetiveis, derivadaInfectados};
    }

    // --- 2. Tarefa (Callable) para o cálculo de uma componente da derivada ---
    static class TarefaDerivada implements Callable<Double> {

        private final int indiceCompartimento; // 0 para S, 1 para I
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
            // Retorna apenas a componente (dS/dt ou dI/dt) solicitada
            return derivSis(estadoAtual, tempo, populacaoTotal, taxaTransmissao, taxaRecuperacao)[this.indiceCompartimento];
        }
    }

    // --- 3. Solver RK4 Paralelo Simplificado ---
    public static double[][] rungeKutka4Paralelo(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao, double infectadosIniciais, double tempoMaximo,
            int numeroPassos) {
        double suscetiveisIniciais = populacaoTotal - infectadosIniciais; // S0 é a população menos os infectados
        double[] estadoAtual = {suscetiveisIniciais, infectadosIniciais};
        int numeroCompartimentos = estadoAtual.length; // Agora é 2 (S e I)
        double incrementoTempo = tempoMaximo / (numeroPassos - 1);
        double[][] historico = new double[numeroPassos][numeroCompartimentos];
        historico[0] = Arrays.copyOf(estadoAtual, numeroCompartimentos);

        // Cria pool de threads com tamanho 2 (para S e I)
        ExecutorService executor = Executors.newFixedThreadPool(numeroCompartimentos);

        System.out.println("Iniciando simulação SIS paralela (RHS) com " + numeroPassos + " passos...");
        long tempoInicio = System.nanoTime();

        for (int passo = 0; passo < numeroPassos - 1; passo++) {
            double tempoAtual = passo * incrementoTempo;
            double[] coeficienteK1 = new double[numeroCompartimentos];
            double[] coeficienteK2, coeficienteK3, coeficienteK4;

            try {
                // --- K1: PARALELO (Calcula dS/dt e dI/dt simultaneamente) ---
                ArrayList<Future<Double>> futuros = new ArrayList<>();
                for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                    // Submete 2 tarefas (S, I)
                    futuros.add(executor.submit(new TarefaDerivada(compartimento, estadoAtual, tempoAtual, populacaoTotal, taxaTransmissao, taxaRecuperacao)));
                }
                for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                    // Sincroniza e coleta os resultados (derivada * incrementoTempo)
                    coeficienteK1[compartimento] = futuros.get(compartimento).get() * incrementoTempo;
                }

                // --- K2, K3, K4: Sequenciais (Lógica simplificada do RK4) ---
                // K2
                double[] estadoIntermediarioK1 = new double[numeroCompartimentos];
                for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                    estadoIntermediarioK1[compartimento] = estadoAtual[compartimento] + 0.5 * coeficienteK1[compartimento];
                }
                double[] derivada2 = derivSis(estadoIntermediarioK1, tempoAtual + incrementoTempo / 2, populacaoTotal, taxaTransmissao, taxaRecuperacao);
                coeficienteK2 = new double[numeroCompartimentos];
                for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                    coeficienteK2[compartimento] = derivada2[compartimento] * incrementoTempo;
                }

                // K3
                double[] estadoIntermediarioK2 = new double[numeroCompartimentos];
                for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                    estadoIntermediarioK2[compartimento] = estadoAtual[compartimento] + 0.5 * coeficienteK2[compartimento];
                }
                double[] derivada3 = derivSis(estadoIntermediarioK2, tempoAtual + incrementoTempo / 2, populacaoTotal, taxaTransmissao, taxaRecuperacao);
                coeficienteK3 = new double[numeroCompartimentos];
                for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                    coeficienteK3[compartimento] = derivada3[compartimento] * incrementoTempo;
                }

                // K4
                double[] estadoIntermediarioK3 = new double[numeroCompartimentos];
                for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                    estadoIntermediarioK3[compartimento] = estadoAtual[compartimento] + coeficienteK3[compartimento];
                }
                double[] derivada4 = derivSis(estadoIntermediarioK3, tempoAtual + incrementoTempo, populacaoTotal, taxaTransmissao, taxaRecuperacao);
                coeficienteK4 = new double[numeroCompartimentos];
                for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                    coeficienteK4[compartimento] = derivada4[compartimento] * incrementoTempo;
                }

            } catch (Exception e) {
                System.err.println("Erro durante a iteração: " + e.getMessage());
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
        System.out.printf("Simulação Paralela (RHS) SIS concluída em: %.4f milissegundos\n", tempoDecorridoMs);

        return historico;
    }

    // --- 4. Método Público para Executar Simulação ---
    public double executarSimulacao() {
        long tempoInicio = System.nanoTime();
        rungeKutka4Paralelo(populacaoTotal, taxaTransmissao, taxaRecuperacao,
                            infectadosIniciais, tempoMaximo, numeroPassos);
        long tempoFim = System.nanoTime();
        return (tempoFim - tempoInicio) / 1_000_000.0;
    }

    // --- 5. Main para Execução ---
    public static void main(String[] args) {
        SISParalelo simulacao = new SISParalelo(1000.0, 0.3, 0.1, 1.0, 100.0, 101);
        double tempoExecucao = simulacao.executarSimulacao();
        System.out.printf("Tempo de execução: %.4f ms\n", tempoExecucao);
    }
}
