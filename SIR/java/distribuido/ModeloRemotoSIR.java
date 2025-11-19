import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface remota para simulação SIR distribuída.
 */
public interface ModeloRemotoSIR extends Remote {
    /**
     * Executa simulação SIR com parâmetros especificados.
     * 
     * @param populacao População total
     * @param infectadosIniciais Número inicial de infectados
     * @param recuperadosIniciais Número inicial de recuperados
     * @param taxaTransmissao Taxa de transmissão (beta)
     * @param taxaRecuperacao Taxa de recuperação (gamma)
     * @param passos Número de passos de simulação
     * @return Array com [suscetíveis, infectados, recuperados] finais
     */
    double[] simularSIR(double populacao, double infectadosIniciais, 
                       double recuperadosIniciais, double taxaTransmissao,
                       double taxaRecuperacao, int passos) throws RemoteException;
}
