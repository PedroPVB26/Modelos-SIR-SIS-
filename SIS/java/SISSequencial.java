
import java.util.Arrays;

/**
 * Implementação Sequencial Simplificada do Modelo SIS
 * (Suscetível-Infectado-Suscetível) resolvido com RK4. Esta é a versão em Java
 * do script Python/SciPy. * S = y[0] (Suscetíveis) I = y[1] (Infectados) * Para
 * compilar e rodar: 1. javac SISSequencial.java 2. java SISSequencial
 */
public class SISSequencial {

    // Parâmetros da simulação
    private final double populacaoTotal;
    private final double taxaTransmissao;
    private final double taxaRecuperacao;
    private final double infectadosIniciais;
    private final double tempoMaximo;
    private final int numeroPassos;

    // Construtor
    public SISSequencial(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao,
                         double infectadosIniciais, double tempoMaximo, int numeroPassos) {
        this.populacaoTotal = populacaoTotal;
        this.taxaTransmissao = taxaTransmissao;
        this.taxaRecuperacao = taxaRecuperacao;
        this.infectadosIniciais = infectadosIniciais;
        this.tempoMaximo = tempoMaximo;
        this.numeroPassos = numeroPassos;
    }

    // --- 1. A função de derivada do Modelo SIS ---
    // Apenas duas dimensões (S, I)
    public static double[] derivSis(double[] estadoAtual, double tempo, double populacaoTotal, double taxaTransmissao, double taxaRecuperacao) {
        double suscetiveis = estadoAtual[0];
        double infectados = estadoAtual[1];

        // Equações SIS:
        // dS/dt = -taxaTransmissao * S * I / N + taxaRecuperacao * I (Perde por infecção, ganha por recuperação)
        // dI/dt = taxaTransmissao * S * I / N - taxaRecuperacao * I  (Ganha por infecção, perde por recuperação)
        double derivadaSuscetiveis = -taxaTransmissao * suscetiveis * infectados / populacaoTotal + taxaRecuperacao * infectados;
        double derivadaInfectados = taxaTransmissao * suscetiveis * infectados / populacaoTotal - taxaRecuperacao * infectados;

        return new double[]{derivadaSuscetiveis, derivadaInfectados};
    }

    // --- 2. Implementação do Solver RK4 (Simplificado) ---
    // O solver RK4 é genérico, adaptamos apenas as dimensões.
    public static double[][] rungeKutka4(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao, double infectadosIniciais, double tempoMaximo, int numeroPassos) {
        double suscetiveisIniciais = populacaoTotal - infectadosIniciais; // Não há recuperadosIniciais no SIS
        double[] estadoInicial = {suscetiveisIniciais, infectadosIniciais};
        int numeroCompartimentos = estadoInicial.length; // Deve ser 2 (S, I)
        double incrementoTempo = tempoMaximo / (numeroPassos - 1);

        double[][] historico = new double[numeroPassos][numeroCompartimentos];
        historico[0] = Arrays.copyOf(estadoInicial, numeroCompartimentos);
        double[] estadoAtual = Arrays.copyOf(estadoInicial, numeroCompartimentos);

        System.out.println("Iniciando simulação SIS (RK4) com " + numeroPassos + " passos...");
        long tempoInicio = System.nanoTime();

        for (int passo = 0; passo < numeroPassos - 1; passo++) {
            double tempoAtual = passo * incrementoTempo;
            double[] coeficienteK1, coeficienteK2, coeficienteK3, coeficienteK4;

            // --- 1. k1 = incrementoTempo * f(tempo_passo, estadoAtual_passo) ---
            double[] derivada1 = derivSis(estadoAtual, tempoAtual, populacaoTotal, taxaTransmissao, taxaRecuperacao);
            coeficienteK1 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                coeficienteK1[compartimento] = derivada1[compartimento] * incrementoTempo;
            }

            // --- 2. k2 = incrementoTempo * f(tempo_passo + incrementoTempo/2, estadoAtual_passo + k1/2) ---
            double[] estadoIntermediarioK1 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                estadoIntermediarioK1[compartimento] = estadoAtual[compartimento] + 0.5 * coeficienteK1[compartimento];
            }
            double[] derivada2 = derivSis(estadoIntermediarioK1, tempoAtual + incrementoTempo / 2, populacaoTotal, taxaTransmissao, taxaRecuperacao);
            coeficienteK2 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                coeficienteK2[compartimento] = derivada2[compartimento] * incrementoTempo;
            }

            // --- 3. k3 = incrementoTempo * f(tempo_passo + incrementoTempo/2, estadoAtual_passo + k2/2) ---
            double[] estadoIntermediarioK2 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                estadoIntermediarioK2[compartimento] = estadoAtual[compartimento] + 0.5 * coeficienteK2[compartimento];
            }
            double[] derivada3 = derivSis(estadoIntermediarioK2, tempoAtual + incrementoTempo / 2, populacaoTotal, taxaTransmissao, taxaRecuperacao);
            coeficienteK3 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                coeficienteK3[compartimento] = derivada3[compartimento] * incrementoTempo;
            }

            // --- 4. k4 = incrementoTempo * f(tempo_passo + incrementoTempo, estadoAtual_passo + k3) ---
            double[] estadoIntermediarioK3 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                estadoIntermediarioK3[compartimento] = estadoAtual[compartimento] + coeficienteK3[compartimento];
            }
            double[] derivada4 = derivSis(estadoIntermediarioK3, tempoAtual + incrementoTempo, populacaoTotal, taxaTransmissao, taxaRecuperacao);
            coeficienteK4 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                coeficienteK4[compartimento] = derivada4[compartimento] * incrementoTempo;
            }

            // --- Atualização: estadoAtual_{passo+1} = estadoAtual_passo + 1/6 * (k1 + 2*k2 + 2*k3 + k4) ---
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
        System.out.printf("Tempo de execução: %.4f milissegundos\n", tempoDecorridoMs);

        return historico;
    }

    // --- 3. Método Público para Executar Simulação ---
    public double executarSimulacao() {
        long tempoInicio = System.nanoTime();
        rungeKutka4(populacaoTotal, taxaTransmissao, taxaRecuperacao,
                    infectadosIniciais, tempoMaximo, numeroPassos);
        long tempoFim = System.nanoTime();
        return (tempoFim - tempoInicio) / 1_000_000.0;
    }

    // --- 4. Main para Execução ---
    public static void main(String[] args) {
        SISSequencial simulacao = new SISSequencial(1000.0, 0.3, 0.1, 1.0, 100.0, 101);
        double tempoExecucao = simulacao.executarSimulacao();
        System.out.printf("Tempo de execução: %.4f ms\n", tempoExecucao);
    }
}
