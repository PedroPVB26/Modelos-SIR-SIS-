
import java.util.Arrays;

/**
 * Implementação Sequencial Simplificada do Modelo SIR resolvido com RK4. A
 * lógica de vetor (soma e escala) foi embutida no solver.
 */
public class SIRSequencial {

    // Parâmetros da simulação
    private final double populacaoTotal;
    private final double taxaTransmissao;
    private final double taxaRecuperacao;
    private final double infectadosIniciais;
    private final double recuperadosIniciais;
    private final double tempoMaximo;
    private final int numeroPassos;

    // Construtor
    public SIRSequencial(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao, 
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

    // --- 1. A função de derivada do Modelo SIR ---
    public static double[] derivSir(double[] estadoAtual, double tempo, double populacaoTotal, double taxaTransmissao, double taxaRecuperacao) {
        double suscetiveis = estadoAtual[0];
        double infectados = estadoAtual[1];
        // recuperados = estadoAtual[2] não é necessário para o cálculo das derivadas

        double derivadaSuscetiveis = -taxaTransmissao * suscetiveis * infectados / populacaoTotal;
        double derivadaInfectados = taxaTransmissao * suscetiveis * infectados / populacaoTotal - taxaRecuperacao * infectados;
        double derivadaRecuperados = taxaRecuperacao * infectados;

        return new double[]{derivadaSuscetiveis, derivadaInfectados, derivadaRecuperados};
    }

    // --- 2. Implementação do Solver RK4 (Simplificado) ---
    public static double[][] rungeKutka4(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao, double infectadosIniciais, double recuperadosIniciais, double tempoMaximo,
            int numeroPassos) {
        double suscetiveisIniciais = populacaoTotal - infectadosIniciais - recuperadosIniciais;
        double[] estadoInicial = {suscetiveisIniciais, infectadosIniciais, recuperadosIniciais};
        int numeroCompartimentos = estadoInicial.length;
        double incrementoTempo = tempoMaximo / (numeroPassos - 1);

        double[][] historico = new double[numeroPassos][numeroCompartimentos];
        historico[0] = Arrays.copyOf(estadoInicial, numeroCompartimentos);
        double[] estadoAtual = Arrays.copyOf(estadoInicial, numeroCompartimentos);

        System.out.println("Iniciando simulação sequencial simplificada com " + numeroPassos + " passos...");
        long tempoInicio = System.nanoTime();

        for (int passo = 0; passo < numeroPassos - 1; passo++) {
            double tempoAtualPasso = passo * incrementoTempo;
            double[] coeficienteK1, coeficienteK2, coeficienteK3, coeficienteK4;

            // --- 1. k1 = h * f(t_i, y_i) ---
            double[] derivada1 = derivSir(estadoAtual, tempoAtualPasso, populacaoTotal, taxaTransmissao, taxaRecuperacao);
            coeficienteK1 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                coeficienteK1[compartimento] = derivada1[compartimento] * incrementoTempo;
            }

            // --- 2. k2 = h * f(t_i + h/2, y_i + k1/2) ---
            double[] estadoIntermediarioK1 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                estadoIntermediarioK1[compartimento] = estadoAtual[compartimento] + 0.5 * coeficienteK1[compartimento];
            }
            double[] derivada2 = derivSir(estadoIntermediarioK1, tempoAtualPasso + incrementoTempo / 2, populacaoTotal, taxaTransmissao, taxaRecuperacao);
            coeficienteK2 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                coeficienteK2[compartimento] = derivada2[compartimento] * incrementoTempo;
            }

            // --- 3. k3 = h * f(t_i + h/2, y_i + k2/2) ---
            double[] estadoIntermediarioK2 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                estadoIntermediarioK2[compartimento] = estadoAtual[compartimento] + 0.5 * coeficienteK2[compartimento];
            }
            double[] derivada3 = derivSir(estadoIntermediarioK2, tempoAtualPasso + incrementoTempo / 2, populacaoTotal, taxaTransmissao, taxaRecuperacao);
            coeficienteK3 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                coeficienteK3[compartimento] = derivada3[compartimento] * incrementoTempo;
            }

            // --- 4. k4 = h * f(t_i + h, y_i + k3) ---
            double[] estadoIntermediarioK3 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                estadoIntermediarioK3[compartimento] = estadoAtual[compartimento] + coeficienteK3[compartimento];
            }
            double[] derivada4 = derivSir(estadoIntermediarioK3, tempoAtualPasso + incrementoTempo, populacaoTotal, taxaTransmissao, taxaRecuperacao);
            coeficienteK4 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                coeficienteK4[compartimento] = derivada4[compartimento] * incrementoTempo;
            }

            // --- Nova solução: estadoAtual_{i+1} = estadoAtual_i + 1/6 * (k1 + 2*k2 + 2*k3 + k4) ---
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                estadoAtual[compartimento] += (coeficienteK1[compartimento] + 2.0 * coeficienteK2[compartimento] + 2.0 * coeficienteK3[compartimento] + coeficienteK4[compartimento]) / 6.0;

                // Garante que os números de indivíduos sejam não negativos
                if (estadoAtual[compartimento] < 0) {
                    estadoAtual[compartimento] = 0;
                }
            }

            historico[passo + 1] = Arrays.copyOf(estadoAtual, numeroCompartimentos);
        }

        long tempoFim = System.nanoTime();
        double tempoDecorridoMs = (tempoFim - tempoInicio) / 1_000_000.0;
        System.out.printf("Tempo de execução sequencial: %.4f milissegundos\n", tempoDecorridoMs);

        return historico;
    }

    // --- 3. Método público para executar simulação ---
    public double executarSimulacao() {
        long tempoInicio = System.nanoTime();
        rungeKutka4(populacaoTotal, taxaTransmissao, taxaRecuperacao, 
                    infectadosIniciais, recuperadosIniciais, tempoMaximo, numeroPassos);
        long tempoFim = System.nanoTime();
        return (tempoFim - tempoInicio) / 1_000_000.0;
    }

    // --- 4. Main para Execução ---
    public static void main(String[] args) {
        SIRSequencial simulacao = new SIRSequencial(1000000.0, 0.2, 1.0 / 10.0, 10.0, 0.0, 500.0, 50000);
        double tempoExecucao = simulacao.executarSimulacao();
        System.out.printf("Tempo de execução: %.4f ms\n", tempoExecucao);
    }
}
