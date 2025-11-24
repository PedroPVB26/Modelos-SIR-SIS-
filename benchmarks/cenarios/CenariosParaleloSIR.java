package cenarios;

import java.util.ArrayList;
import java.util.concurrent.*;

public class CenariosParaleloSIR {
    
    // Parâmetros da simulação
    private final double populacaoBase;
    private final double infectadosIniciais;
    private final double recuperadosIniciais;
    private final double taxaRecuperacao;
    private final double tempoMaximo;
    private final int numeroPassos;
    private final int numeroCenarios;
    private final int numeroThreads;

    // Construtor
    public CenariosParaleloSIR(double populacaoBase, double infectadosIniciais, double recuperadosIniciais,
                               double taxaRecuperacao, double tempoMaximo, int numeroPassos, 
                               int numeroCenarios, int numeroThreads) {
        this.populacaoBase = populacaoBase;
        this.infectadosIniciais = infectadosIniciais;
        this.recuperadosIniciais = recuperadosIniciais;
        this.taxaRecuperacao = taxaRecuperacao;
        this.tempoMaximo = tempoMaximo;
        this.numeroPassos = numeroPassos;
        this.numeroCenarios = numeroCenarios;
        this.numeroThreads = numeroThreads;
    } 

    static class ParametrosCenario {
        double populacaoTotal, taxaTransmissao, taxaRecuperacao, infectadosIniciais, recuperadosIniciais, tempoMaximo;
        int numeroPassos;
        public ParametrosCenario(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao,
                                double infectadosIniciais, double recuperadosIniciais, double tempoMaximo, int numeroPassos) {
            this.populacaoTotal = populacaoTotal; 
            this.taxaTransmissao = taxaTransmissao; 
            this.taxaRecuperacao = taxaRecuperacao;
            this.infectadosIniciais = infectadosIniciais;
            this.recuperadosIniciais = recuperadosIniciais;
            this.tempoMaximo = tempoMaximo;
            this.numeroPassos = numeroPassos;
        }
    }
    
    // --- 1. A função de derivada do Modelo SIR ---
    public static double[] derivSir(double[] estadoAtual, double populacaoTotal, double taxaTransmissao, double taxaRecuperacao) {
        double suscetiveis = estadoAtual[0];
        double infectados = estadoAtual[1];
        
        double derivadaSuscetiveis = -taxaTransmissao * suscetiveis * infectados / populacaoTotal;
        double derivadaInfectados = taxaTransmissao * suscetiveis * infectados / populacaoTotal - taxaRecuperacao * infectados;
        double derivadaRecuperados = taxaRecuperacao * infectados;
        return new double[]{derivadaSuscetiveis, derivadaInfectados, derivadaRecuperados};
    }

    // --- 2. Implementação do Solver RK4 Sequencial para UM Cenário (SIMPLIFICADO) ---
    public static double resolverRungeKutka4(ParametrosCenario parametros) {
        double suscetiveisIniciais = parametros.populacaoTotal - parametros.infectadosIniciais - parametros.recuperadosIniciais;
        double[] estadoAtual = {suscetiveisIniciais, parametros.infectadosIniciais, parametros.recuperadosIniciais};
        int numeroCompartimentos = estadoAtual.length;
        double incrementoTempo = parametros.tempoMaximo / (parametros.numeroPassos - 1);
        double maxInfectados = parametros.infectadosIniciais;
        double[] coeficienteK1, coeficienteK2, coeficienteK3, coeficienteK4;

        for (int passo = 0; passo < parametros.numeroPassos - 1; passo++) {
            
            // k1 = h * f(y)
            double[] derivada1 = derivSir(estadoAtual, parametros.populacaoTotal, parametros.taxaTransmissao, parametros.taxaRecuperacao);
            coeficienteK1 = new double[numeroCompartimentos];
            for(int compartimento=0; compartimento<numeroCompartimentos; compartimento++) coeficienteK1[compartimento] = derivada1[compartimento] * incrementoTempo;

            // k2
            double[] estadoIntermediarioK1 = new double[numeroCompartimentos];
            for(int compartimento=0; compartimento<numeroCompartimentos; compartimento++) estadoIntermediarioK1[compartimento] = estadoAtual[compartimento] + 0.5 * coeficienteK1[compartimento];
            double[] derivada2 = derivSir(estadoIntermediarioK1, parametros.populacaoTotal, parametros.taxaTransmissao, parametros.taxaRecuperacao);
            coeficienteK2 = new double[numeroCompartimentos];
            for(int compartimento=0; compartimento<numeroCompartimentos; compartimento++) coeficienteK2[compartimento] = derivada2[compartimento] * incrementoTempo;

            // k3
            double[] estadoIntermediarioK2 = new double[numeroCompartimentos];
            for(int compartimento=0; compartimento<numeroCompartimentos; compartimento++) estadoIntermediarioK2[compartimento] = estadoAtual[compartimento] + 0.5 * coeficienteK2[compartimento];
            double[] derivada3 = derivSir(estadoIntermediarioK2, parametros.populacaoTotal, parametros.taxaTransmissao, parametros.taxaRecuperacao);
            coeficienteK3 = new double[numeroCompartimentos];
            for(int compartimento=0; compartimento<numeroCompartimentos; compartimento++) coeficienteK3[compartimento] = derivada3[compartimento] * incrementoTempo;

            // k4
            double[] estadoIntermediarioK3 = new double[numeroCompartimentos];
            for(int compartimento=0; compartimento<numeroCompartimentos; compartimento++) estadoIntermediarioK3[compartimento] = estadoAtual[compartimento] + coeficienteK3[compartimento];
            double[] derivada4 = derivSir(estadoIntermediarioK3, parametros.populacaoTotal, parametros.taxaTransmissao, parametros.taxaRecuperacao);
            coeficienteK4 = new double[numeroCompartimentos];
            for(int compartimento=0; compartimento<numeroCompartimentos; compartimento++) coeficienteK4[compartimento] = derivada4[compartimento] * incrementoTempo;

            // Atualização Y (y_{i+1})
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                estadoAtual[compartimento] += (coeficienteK1[compartimento] + 2.0 * coeficienteK2[compartimento] + 2.0 * coeficienteK3[compartimento] + coeficienteK4[compartimento]) / 6.0;
                if (estadoAtual[compartimento] < 0) estadoAtual[compartimento] = 0;
            }
            
            if (estadoAtual[1] > maxInfectados) maxInfectados = estadoAtual[1];
        }
        return maxInfectados;
    }

    // --- 3. Callable para a Tarefa de Cenário ---
    static class TarefaCenario implements Callable<Double> {
        private final ParametrosCenario parametros;

        public TarefaCenario(ParametrosCenario parametros) {
            this.parametros = parametros;
        }

        @Override
        public Double call() throws Exception {
            // Cada thread executa o solver sequencial completo e simplificado
            return resolverRungeKutka4(parametros);
        }
    }

    // --- 5. Método público para executar simulação ---
    public double executarSimulacao() {
        // Geração dos Cenários (Variando taxaTransmissao)
        ArrayList<ParametrosCenario> cenarios = new ArrayList<>();
        for (int indiceCenario = 0; indiceCenario < numeroCenarios; indiceCenario++) {
            double taxaTransmissao = 0.1 + (0.4 * indiceCenario) / (numeroCenarios - 1);
            cenarios.add(new ParametrosCenario(populacaoBase, taxaTransmissao, taxaRecuperacao,
                                               infectadosIniciais, recuperadosIniciais, tempoMaximo, numeroPassos));
        }

        System.out.println("--- SIMULAÇÃO PARALELA DE MÚLTIPLOS CENÁRIOS ---");
        System.out.println("Total de simulações: " + numeroCenarios);
        System.out.println("Threads Utilizadas: " + numeroThreads);

        long tempoInicio = System.nanoTime();

        ExecutorService executor = Executors.newFixedThreadPool(numeroThreads);
        ArrayList<Future<Double>> futuros = new ArrayList<>();

        for (ParametrosCenario parametros : cenarios) {
            futuros.add(executor.submit(new TarefaCenario(parametros)));
        }

        double totalMaxInfectados = 0;
        try {
            for (Future<Double> futuro : futuros) {
                totalMaxInfectados += futuro.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Erro na execução paralela: " + e.getMessage());
        } finally {
            executor.shutdown();
        }

        long tempoFim = System.nanoTime();
        double tempoDecorridoMs = (tempoFim - tempoInicio) / 1_000_000.0;
        System.out.printf("Tempo de execução paralelo total: %.4f milissegundos\n", tempoDecorridoMs);
        return tempoDecorridoMs;
    }

    // --- 6. Main para Execução ---
    public static void main(String[] args) {
        CenariosParaleloSIR simulacao = new CenariosParaleloSIR(
            1000000.0, 10.0, 0.0, 1.0 / 10.0, 500.0, 50000, 1000, 
            Runtime.getRuntime().availableProcessors()
        );
        double tempoExecucao = simulacao.executarSimulacao();
        System.out.printf("Tempo de execução: %.4f ms\n", tempoExecucao);
    }
}
