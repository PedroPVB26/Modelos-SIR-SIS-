import java.rmi.Naming;

public class ClienteModeloSIS {

    // Parâmetros da simulação
    private final double populacaoTotal;
    private final double taxaTransmissao;
    private final double taxaRecuperacao;
    private final double infectadosIniciais;
    private final double tempoMaximo;
    private final int numeroPassos;

    // Construtor
    public ClienteModeloSIS(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao,
                            double infectadosIniciais, double tempoMaximo, int numeroPassos) {
        this.populacaoTotal = populacaoTotal;
        this.taxaTransmissao = taxaTransmissao;
        this.taxaRecuperacao = taxaRecuperacao;
        this.infectadosIniciais = infectadosIniciais;
        this.tempoMaximo = tempoMaximo;
        this.numeroPassos = numeroPassos;
    }

    // Método público para executar simulação
    public double executarSimulacao() throws Exception {
        long tempoInicio = System.nanoTime();

        // Localiza o objeto remoto (o Servidor)
        String url = "rmi://localhost/ServicoModeloSIS";
        ModeloSISRemoto servicoModelo = (ModeloSISRemoto) Naming.lookup(url);

        // Chama o método remoto (a execução REAL ocorre no Servidor)
        servicoModelo.rungeKutka4(
            populacaoTotal, taxaTransmissao, taxaRecuperacao,
            infectadosIniciais, tempoMaximo, numeroPassos
        );

        long tempoFim = System.nanoTime();
        return (tempoFim - tempoInicio) / 1_000_000.0;
    }

    public static void main(String[] args) {
        try {
            ClienteModeloSIS cliente = new ClienteModeloSIS(
                1000.0, 0.3, 0.1, 1.0, 100.0, 101
            );

            System.out.println("[CLIENTE] Conectado ao serviço remoto");
            System.out.println("[CLIENTE] Enviando requisição para cálculo do RK4 (SIS)...");

            double tempoExecucao = cliente.executarSimulacao();

            System.out.println("[CLIENTE] Resultados recebidos com sucesso.");
            System.out.printf("[CLIENTE] Tempo total (incluindo RMI e cálculo): %.4f ms\n", tempoExecucao);

        } catch (Exception e) {
            System.err.println("Erro no Cliente RMI (SIS): Certifique-se de que o Servidor RMI está rodando.");
            System.err.println("Erro: " + e.getMessage());
        }
    }
}
