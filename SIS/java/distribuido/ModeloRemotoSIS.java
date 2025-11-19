import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface remota para simulação SIS distribuída.
 */
public interface ModeloRemotoSIS extends Remote {
    /**
     * Executa simulação SIS com parâmetros especificados.
     * 
     * @param populacao População total
     * @param infectadosIniciais Número inicial de infectados
     * @param taxaTransmissao Taxa de transmissão (beta)
     * @param taxaRecuperacao Taxa de recuperação (gamma)
     * @param passos Número de passos de simulação
     * @return Array com [suscetíveis, infectados] finais
     */
    double[] simularSIS(double populacao, double infectadosIniciais,
                       double taxaTransmissao, double taxaRecuperacao, 
                       int passos) throws RemoteException;
}
