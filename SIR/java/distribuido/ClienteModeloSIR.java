import java.rmi.Naming;

public class ClienteModeloSIR {

    // Parâmetros da simulação
    private final double populacaoTotal;
    private final double taxaTransmissao;
    private final double taxaRecuperacao;
    private final double infectadosIniciais;
    private final double recuperadosIniciais;
    private final double tempoMaximo;
    private final int numeroPassos;

    // Construtor
    public ClienteModeloSIR(double populacaoTotal, double taxaTransmissao, double taxaRecuperacao,
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

    // Método público para executar simulação
    public double executarSimulacao() throws Exception {
        long tempoInicio = System.nanoTime();

        // Localiza o objeto remoto (o Servidor)
        String url = "rmi://localhost/ServicoModeloSIR";
        ModeloSIRRemoto servicoModelo = (ModeloSIRRemoto) Naming.lookup(url);

        // Chama o método remoto (a execução REAL ocorre no Servidor)
        servicoModelo.rungeKutka4(
            populacaoTotal, taxaTransmissao, taxaRecuperacao,
            infectadosIniciais, recuperadosIniciais, tempoMaximo, numeroPassos
        );

        long tempoFim = System.nanoTime();
        return (tempoFim - tempoInicio) / 1_000_000.0;
    }

    public static void main(String[] args) {
        try {
            ClienteModeloSIR cliente = new ClienteModeloSIR(
                1000000.0, 0.2, 1.0 / 10.0, 10.0, 0.0, 500.0, 50000
            );

            System.out.println("[CLIENTE] Conectado ao serviço remoto");
            System.out.println("[CLIENTE] Enviando requisição para cálculo do RK4...");

            double tempoExecucao = cliente.executarSimulacao();

            System.out.println("[CLIENTE] Resultados recebidos com sucesso.");
            System.out.printf("[CLIENTE] Tempo total (incluindo RMI e cálculo): %.4f ms\n", tempoExecucao);

        } catch (Exception e) {
            System.err.println("Erro no Cliente RMI: Certifique-se de que o Servidor RMI está rodando.");
            System.err.println("Erro: " + e.getMessage());
        }
    }
}
