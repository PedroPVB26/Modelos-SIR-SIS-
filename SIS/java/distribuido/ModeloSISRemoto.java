

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ModeloSISRemoto extends Remote {

    /**
     * Resolve a simulação do Modelo SIS (Suscetível-Infectado-Suscetível)
     * usando RK4 com os parâmetros fornecidos.
     *
     * * @param populacaoTotal População total.
     * @param taxaTransmissao Taxa de transmissão.
     * @param taxaRecuperacao Taxa de recuperação.
     * @param infectadosIniciais Infectados iniciais.
     * @param tempoMaximo Tempo máximo de simulação.
     * @param numeroPassos Número de passos de tempo.
     * @return Matriz 2D com a série temporal [tempo][S, I].
     * @throws RemoteException Se ocorrer um erro durante a comunicação RMI.
     */
    double[][] rungeKutka4(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao, double infectadosIniciais, double tempoMaximo, int numeroPassos)
            throws RemoteException;

    /**
     * Retorna o tempo de processamento da última execução (em ms).
     * Usado para medir overhead de rede/serialização.
     */
    double getUltimoTempoProcessamento() throws RemoteException;

    /**
     * Função de derivada auxiliar do Modelo SIS.
     */
    double[] derivSis(double[] estadoAtual, double tempo, double populacaoTotal, double taxaTransmissao, double taxaRecuperacao) throws RemoteException;
}
