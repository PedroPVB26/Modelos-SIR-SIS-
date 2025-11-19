

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class ServidorModeloSIR extends UnicastRemoteObject implements ModeloSIRRemoto {

    // Construtor obrigatório para objetos remotos
    public ServidorModeloSIR() throws RemoteException {
        super();
    }

    // --- 1. A função de derivada do Modelo SIR ---
    @Override
    public double[] derivSir(double[] estadoAtual, double tempo, double populacaoTotal, double taxaTransmissao, double taxaRecuperacao) throws RemoteException {
        double suscetiveis = estadoAtual[0];
        double infectados = estadoAtual[1];

        double derivadaSuscetiveis = -taxaTransmissao * suscetiveis * infectados / populacaoTotal;
        double derivadaInfectados = taxaTransmissao * suscetiveis * infectados / populacaoTotal - taxaRecuperacao * infectados;
        double derivadaRecuperados = taxaRecuperacao * infectados;

        return new double[]{derivadaSuscetiveis, derivadaInfectados, derivadaRecuperados};
    }

    // --- 2. Implementação do Solver RK4 (Simplificado) ---
    @Override
    public double[][] rungeKutka4(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao, double infectadosIniciais, double recuperadosIniciais, double tempoMaximo, int numeroPassos)
            throws RemoteException {
        System.out.println("\n[SERVIDOR] Iniciando simulação remota...");

        double suscetiveisIniciais = populacaoTotal - infectadosIniciais - recuperadosIniciais;
        double[] estadoAtual = {suscetiveisIniciais, infectadosIniciais, recuperadosIniciais};
        int numeroCompartimentos = estadoAtual.length;
        double incrementoTempo = tempoMaximo / (numeroPassos - 1);
        double[][] historico = new double[numeroPassos][numeroCompartimentos];
        historico[0] = Arrays.copyOf(estadoAtual, numeroCompartimentos);

        long tempoInicio = System.nanoTime();

        for (int passo = 0; passo < numeroPassos - 1; passo++) {
            double tempoAtual = passo * incrementoTempo;
            double[] coeficienteK1, coeficienteK2, coeficienteK3, coeficienteK4;

            // --- 1. k1 = h * f(t_i, y_i) ---
            double[] derivada1 = derivSir(estadoAtual, tempoAtual, populacaoTotal, taxaTransmissao, taxaRecuperacao);
            coeficienteK1 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                coeficienteK1[compartimento] = derivada1[compartimento] * incrementoTempo;
            }

            // --- 2. k2 = h * f(t_i + h/2, y_i + k1/2) ---
            double[] estadoIntermediarioK1 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                estadoIntermediarioK1[compartimento] = estadoAtual[compartimento] + 0.5 * coeficienteK1[compartimento];
            }
            double[] derivada2 = derivSir(estadoIntermediarioK1, tempoAtual + incrementoTempo / 2, populacaoTotal, taxaTransmissao, taxaRecuperacao);
            coeficienteK2 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                coeficienteK2[compartimento] = derivada2[compartimento] * incrementoTempo;
            }

            // --- 3. k3 = h * f(t_i + h/2, y_i + k2/2) ---
            double[] estadoIntermediarioK2 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                estadoIntermediarioK2[compartimento] = estadoAtual[compartimento] + 0.5 * coeficienteK2[compartimento];
            }
            double[] derivada3 = derivSir(estadoIntermediarioK2, tempoAtual + incrementoTempo / 2, populacaoTotal, taxaTransmissao, taxaRecuperacao);
            coeficienteK3 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                coeficienteK3[compartimento] = derivada3[compartimento] * incrementoTempo;
            }

            // --- 4. k4 = h * f(t_i + h, y_i + k3) ---
            double[] estadoIntermediarioK3 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                estadoIntermediarioK3[compartimento] = estadoAtual[compartimento] + coeficienteK3[compartimento];
            }
            double[] derivada4 = derivSir(estadoIntermediarioK3, tempoAtual + incrementoTempo, populacaoTotal, taxaTransmissao, taxaRecuperacao);
            coeficienteK4 = new double[numeroCompartimentos];
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                coeficienteK4[compartimento] = derivada4[compartimento] * incrementoTempo;
            }

            // --- Nova solução: estadoAtual_{i+1} = estadoAtual_i + 1/6 * (k1 + 2*k2 + 2*k3 + k4) ---
            for (int compartimento = 0; compartimento < numeroCompartimentos; compartimento++) {
                estadoAtual[compartimento] += (coeficienteK1[compartimento] + 2.0 * coeficienteK2[compartimento] + 2.0 * coeficienteK3[compartimento] + coeficienteK4[compartimento]) / 6.0;
                if (estadoAtual[compartimento] < 0) {
                    estadoAtual[compartimento] = 0;
                }
            }

            historico[passo + 1] = Arrays.copyOf(estadoAtual, numeroCompartimentos);
        }

        long tempoFim = System.nanoTime();
        double tempoDecorridoMs = (tempoFim - tempoInicio) / 1_000_000.0;
        System.out.printf("[SERVIDOR] Simulação concluída em %.4f ms\n", tempoDecorridoMs);

        return historico; // O resultado é serializado e enviado de volta ao cliente
    }

    // --- 3. Main para Iniciar o Servidor RMI ---
    public static void main(String[] args) {
        try {
            // Cria a instância do objeto remoto
            ServidorModeloSIR obj = new ServidorModeloSIR();

            // Cria ou usa o Registry na porta 1099 (padrão do RMI)
            // É necessário que o Registry esteja rodando, ou que seja criado aqui.
            LocateRegistry.createRegistry(1099);

            // Registra o objeto remoto no Registry com um nome
            String nome = "ServicoModeloSIR";
            Naming.rebind(nome, obj);

            System.out.println("Servidor RMI do Modelo SIR pronto e registrado como: " + nome);

        } catch (RemoteException | java.net.MalformedURLException e) {
            System.err.println("Erro no Servidor RMI: " + e.getMessage());
        }
    }
}
