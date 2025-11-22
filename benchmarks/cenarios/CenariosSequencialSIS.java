package cenarios;

import java.util.ArrayList;

/**
 * Versão Sequencial de Cenários do Modelo SIS. Executa múltiplas simulações SIS
 * completas (variando o parâmetro taxaTransmissao) em série. * Para compilar e
 * rodar: 1. javac cenarios/CenariosSequencialSIS.java 2. java
 * cenarios.CenariosSequencialSIS
 */
public class CenariosSequencialSIS {

    // Parâmetros da simulação
    private final double populacaoBase;
    private final double infectadosIniciais;
    private final double taxaRecuperacao;
    private final double tempoMaximo;
    private final int numeroPassos;
    private final int numeroCenarios;

    // Construtor
    public CenariosSequencialSIS(double populacaoBase, double infectadosIniciais, double taxaRecuperacao,
                                 double tempoMaximo, int numeroPassos, int numeroCenarios) {
        this.populacaoBase = populacaoBase;
        this.infectadosIniciais = infectadosIniciais;
        this.taxaRecuperacao = taxaRecuperacao;
        this.tempoMaximo = tempoMaximo;
        this.numeroPassos = numeroPassos;
        this.numeroCenarios = numeroCenarios;
    }

    // Estrutura para guardar parâmetros específicos de cada cenário
    static class ParametrosCenario {
        double populacaoTotal, taxaTransmissao, taxaRecuperacao, infectadosIniciais, tempoMaximo;
        int numeroPassos;

        public ParametrosCenario(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao,
                                double infectadosIniciais, double tempoMaximo, int numeroPassos) {
            this.populacaoTotal = populacaoTotal;
            this.taxaTransmissao = taxaTransmissao;
            this.taxaRecuperacao = taxaRecuperacao;
            this.infectadosIniciais = infectadosIniciais;
            this.tempoMaximo = tempoMaximo;
            this.numeroPassos = numeroPassos;
        }
    }

    // --- 1. A função de derivada do Modelo SIS (EDOs) ---
    public static double[] derivSis(double[] estadoAtual, double populacaoTotal, double taxaTransmissao, double taxaRecuperacao) {
        double suscetiveis = estadoAtual[0];
        double infectados = estadoAtual[1];

        double derivadaSuscetiveis = -taxaTransmissao * suscetiveis * infectados / populacaoTotal + taxaRecuperacao * infectados;
        double derivadaInfectados = taxaTransmissao * suscetiveis * infectados / populacaoTotal - taxaRecuperacao * infectados;

        return new double[]{derivadaSuscetiveis, derivadaInfectados};
    }

    // --- 2. Implementação do Solver RK4 Sequencial para UM Cenário ---
    // Retorna apenas o pico de infectados (maximoInfectados) para fins de agregação.
    public static double resolverRungeKutka4(ParametrosCenario parametros) {
        double suscetiveisIniciais = parametros.populacaoTotal - parametros.infectadosIniciais;
        double[] estadoAtual = {suscetiveisIniciais, parametros.infectadosIniciais};
        int numeroCompartimentos = estadoAtual.length;
        double incrementoTempo = parametros.tempoMaximo / (parametros.numeroPassos - 1);
        double maximoInfectados = parametros.infectadosIniciais;
        double[] coeficienteK1, coeficienteK2, coeficienteK3, coeficienteK4;

        for (int passo = 0; passo < parametros.numeroPassos - 1; passo++) {

            // k1 = incrementoTempo * f(estadoAtual)
            double[] derivada1 = derivSis(estadoAtual, parametros.populacaoTotal, parametros.taxaTransmissao, parametros.taxaRecuperacao);
            coeficienteK1 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                coeficienteK1[compartimento] = derivada1[compartimento] * incrementoTempo;
            }

            // k2
            double[] estadoIntermediarioK1 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                estadoIntermediarioK1[compartimento] = estadoAtual[compartimento] + 0.5 * coeficienteK1[compartimento];
            }
            double[] derivada2 = derivSis(estadoIntermediarioK1, parametros.populacaoTotal, parametros.taxaTransmissao, parametros.taxaRecuperacao);
            coeficienteK2 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                coeficienteK2[compartimento] = derivada2[compartimento] * incrementoTempo;
            }

            // k3
            double[] estadoIntermediarioK2 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                estadoIntermediarioK2[compartimento] = estadoAtual[compartimento] + 0.5 * coeficienteK2[compartimento];
            }
            double[] derivada3 = derivSis(estadoIntermediarioK2, parametros.populacaoTotal, parametros.taxaTransmissao, parametros.taxaRecuperacao);
            coeficienteK3 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                coeficienteK3[compartimento] = derivada3[compartimento] * incrementoTempo;
            }

            // k4
            double[] estadoIntermediarioK3 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                estadoIntermediarioK3[compartimento] = estadoAtual[compartimento] + coeficienteK3[compartimento];
            }
            double[] derivada4 = derivSis(estadoIntermediarioK3, parametros.populacaoTotal, parametros.taxaTransmissao, parametros.taxaRecuperacao);
            coeficienteK4 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                coeficienteK4[compartimento] = derivada4[compartimento] * incrementoTempo;
            }

            // Atualização Y (estadoAtual_{passo+1})
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                estadoAtual[compartimento] += (coeficienteK1[compartimento] + 2.0 * coeficienteK2[compartimento] + 2.0 * coeficienteK3[compartimento] + coeficienteK4[compartimento]) / 6.0;
                if (estadoAtual[compartimento] < 0) {
                    estadoAtual[compartimento] = 0;
                }
            }

            if (estadoAtual[1] > maximoInfectados) {
                maximoInfectados = estadoAtual[1];
            }
        }
        return maximoInfectados;
    }

    // --- 3. Método público para executar simulação ---
    public double executarSimulacao() {
        // Geração dos Cenários (Variando taxaTransmissao)
        ArrayList<ParametrosCenario> cenarios = new ArrayList<>();
        for (int indiceCenario = 0; indiceCenario < numeroCenarios; indiceCenario++) {
            double taxaTransmissao = 0.1 + (0.4 * indiceCenario) / (numeroCenarios - 1);
            cenarios.add(new ParametrosCenario(populacaoBase, taxaTransmissao, taxaRecuperacao,
                                               infectadosIniciais, tempoMaximo, numeroPassos));
        }

        System.out.println("--- SIMULAÇÃO SEQUENCIAL DE MÚLTIPLOS CENÁRIOS (SIS) ---");
        System.out.println("Total de simulações: " + numeroCenarios);
        long tempoInicio = System.nanoTime();

        double totalMaximoInfectados = 0;
        for (ParametrosCenario parametros : cenarios) {
            totalMaximoInfectados += resolverRungeKutka4(parametros);
        }

        long tempoFim = System.nanoTime();
        double tempoDecorridoMs = (tempoFim - tempoInicio) / 1_000_000.0;
        System.out.printf("Tempo de execução sequencial total: %.4f milissegundos\n", tempoDecorridoMs);
        return tempoDecorridoMs;
    }

    // --- 4. Main para Execução Sequencial ---
    public static void main(String[] args) {
        CenariosSequencialSIS simulacao = new CenariosSequencialSIS(
            1000.0, 1.0, 0.1, 100.0, 50000, 1000
        );
        double tempoExecucao = simulacao.executarSimulacao();
        System.out.printf("Tempo de execução: %.4f ms\n", tempoExecucao);
    }
}
