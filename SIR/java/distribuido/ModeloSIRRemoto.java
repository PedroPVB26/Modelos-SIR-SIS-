

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ModeloSIRRemoto extends Remote {

    /**
     * Resolve a simulação do Modelo SIR usando RK4 com os parâmetros
     * fornecidos.
     *
     * @param populacaoTotal População total.
     * @param taxaTransmissao Taxa de transmissão.
     * @param taxaRecuperacao Taxa de recuperação.
     * @param infectadosIniciais Infectados iniciais.
     * @param recuperadosIniciais Recuperados iniciais.
     * @param tempoMaximo Tempo máximo de simulação.
     * @param numeroPassos Número de passos de tempo.
     * @return Matriz 2D com a série temporal [tempo][S, I, R].
     * @throws RemoteException Se ocorrer um erro durante a comunicação RMI.
     */
    double[][] rungeKutka4(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao, double infectadosIniciais, double recuperadosIniciais, double tempoMaximo, int numeroPassos)
            throws RemoteException;

    /**
     * Retorna o tempo de processamento da última execução (em ms).
     * Usado para medir overhead de rede/serialização.
     */
    double getUltimoTempoProcessamento() throws RemoteException;

    /**
     * Função de derivada auxiliar do Modelo SIR. Embora seja um método
     * auxiliar, deve ser acessível se o método principal o chama.
     */
    double[] derivSir(double[] estadoAtual, double tempo, double populacaoTotal, double taxaTransmissao, double taxaRecuperacao) throws RemoteException;
}
