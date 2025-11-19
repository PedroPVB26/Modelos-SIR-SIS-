import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

// Interfaces RMI
interface ModeloRemotoSIR extends Remote {
    double[] simularSIR(double populacao, double infectadosIniciais, 
                       double recuperadosIniciais, double taxaTransmissao,
                       double taxaRecuperacao, int passos) throws RemoteException;
}

interface ModeloRemotoSIS extends Remote {
    double[] simularSIS(double populacao, double infectadosIniciais, 
                       double taxaTransmissao, double taxaRecuperacao, 
                       int passos) throws RemoteException;
}

/**
 * Benchmark para testar desempenho distribuído SIR e SIS variando número de hosts RMI.
 * Simula múltiplos servidores RMI em portas diferentes.
 */
public class BenchmarksDistribuidoCompleto {
    
    private static final int REPETICOES = 5;
    private static final String ARQUIVO_CSV = "resultados_benchmark_distribuido_completo.csv";
    
    // Configurações de teste
    private static final int[] NUMEROS_HOSTS = {1, 2, 4, 8};
    private static final int[] NUMEROS_CENARIOS = {100, 500, 1000};
    private static final int PORTA_BASE = 1099;
    
    // Parâmetros SIR
    private static final double POPULACAO_SIR = 1000000.0;
    private static final int PASSOS_SIR = 50000;
    private static final double TAXA_TRANSMISSAO_SIR = 0.2;
    private static final double TAXA_RECUPERACAO_SIR = 0.1;
    private static final double INFECTADOS_INICIAIS_SIR = 10.0;
    private static final double RECUPERADOS_INICIAIS_SIR = 0.0;
    
    // Parâmetros SIS
    private static final double POPULACAO_SIS = 1000.0;
    private static final int PASSOS_SIS = 50000;
    private static final double TAXA_TRANSMISSAO_SIS = 0.3;
    private static final double TAXA_RECUPERACAO_SIS = 0.1;
    private static final double INFECTADOS_INICIAIS_SIS = 1.0;
    
    private static PrintWriter csvWriter;
    private static List<Registry> registries = new ArrayList<>();
    private static List<Object> servidores = new ArrayList<>();
    
    public static void main(String[] args) {
        System.out.println("═".repeat(80));
        System.out.println("  BENCHMARKS DISTRIBUÍDOS COMPLETO - SIR e SIS");
        System.out.println("═".repeat(80));
        System.out.println("Configuração:");
        System.out.println("  Repetições por teste: " + REPETICOES);
        System.out.println("  Números de hosts: " + arrayToString(NUMEROS_HOSTS));
        System.out.println("  Números de cenários: " + arrayToString(NUMEROS_CENARIOS));
        System.out.println("\n  SIR - População: " + (int)POPULACAO_SIR + ", Passos: " + PASSOS_SIR);
        System.out.println("  SIS - População: " + (int)POPULACAO_SIS + ", Passos: " + PASSOS_SIS);
        System.out.println("═".repeat(80));
        System.out.println();
        
        try {
            inicializarCSV();
            
            // Testes SIR
            System.out.println("\n┌─ TESTES DISTRIBUÍDOS SIR ──────────────────────────────┐");
            testarVariacaoHostsSIR();
            System.out.println("└────────────────────────────────────────────────────────┘\n");
            
            // Testes SIS
            System.out.println("\n┌─ TESTES DISTRIBUÍDOS SIS ──────────────────────────────┐");
            testarVariacaoHostsSIS();
            System.out.println("└────────────────────────────────────────────────────────┘\n");
            
            csvWriter.close();
            
            System.out.println("═".repeat(80));
            System.out.println("  ✓ Benchmarks concluídos com sucesso!");
            System.out.println("  ✓ Resultados salvos em: " + ARQUIVO_CSV);
            System.out.println("═".repeat(80));
            
        } catch (Exception e) {
            System.err.println("Erro durante execução dos benchmarks:");
            e.printStackTrace();
        }
    }
    
    private static void testarVariacaoHostsSIR() {
        for (int numCenarios : NUMEROS_CENARIOS) {
            System.out.println("\n  Testando: Cenários=" + numCenarios);
            
            for (int numHosts : NUMEROS_HOSTS) {
                System.out.print("    ○ Hosts=" + numHosts + "...");
                
                try {
                    inicializarServidoresSIR(numHosts);
                    
                    for (int rep = 1; rep <= REPETICOES; rep++) {
                        long inicio = System.nanoTime();
                        executarSimulacaoDistribuidaSIR(numHosts, numCenarios);
                        long fim = System.nanoTime();
                        double tempo = (fim - inicio) / 1_000_000.0;
                        
                        gravarResultado("SIR", "Distribuido", numHosts, numCenarios, rep, tempo);
                    }
                    
                    limparServidores();
                    System.out.println(" ✓");
                    
                } catch (Exception e) {
                    System.out.println(" ✗ Erro: " + e.getMessage());
                }
            }
        }
    }
    
    private static void testarVariacaoHostsSIS() {
        for (int numCenarios : NUMEROS_CENARIOS) {
            System.out.println("\n  Testando: Cenários=" + numCenarios);
            
            for (int numHosts : NUMEROS_HOSTS) {
                System.out.print("    ○ Hosts=" + numHosts + "...");
                
                try {
                    inicializarServidoresSIS(numHosts);
                    
                    for (int rep = 1; rep <= REPETICOES; rep++) {
                        long inicio = System.nanoTime();
                        executarSimulacaoDistribuidaSIS(numHosts, numCenarios);
                        long fim = System.nanoTime();
                        double tempo = (fim - inicio) / 1_000_000.0;
                        
                        gravarResultado("SIS", "Distribuido", numHosts, numCenarios, rep, tempo);
                    }
                    
                    limparServidores();
                    System.out.println(" ✓");
                    
                } catch (Exception e) {
                    System.out.println(" ✗ Erro: " + e.getMessage());
                }
            }
        }
    }
    
    private static void inicializarServidoresSIR(int numHosts) throws Exception {
        registries.clear();
        servidores.clear();
        
        for (int i = 0; i < numHosts; i++) {
            int porta = PORTA_BASE + i;
            Registry registry = LocateRegistry.createRegistry(porta);
            registries.add(registry);
            
            ServidorModeloSIRImpl servidor = new ServidorModeloSIRImpl();
            ModeloRemotoSIR stub = (ModeloRemotoSIR) UnicastRemoteObject.exportObject(servidor, 0);
            registry.rebind("ModeloSIR_Host" + i, stub);
            servidores.add(servidor);
        }
        
        Thread.sleep(100);
    }
    
    private static void inicializarServidoresSIS(int numHosts) throws Exception {
        registries.clear();
        servidores.clear();
        
        for (int i = 0; i < numHosts; i++) {
            int porta = PORTA_BASE + i;
            Registry registry = LocateRegistry.createRegistry(porta);
            registries.add(registry);
            
            ServidorModeloSISImpl servidor = new ServidorModeloSISImpl();
            ModeloRemotoSIS stub = (ModeloRemotoSIS) UnicastRemoteObject.exportObject(servidor, 0);
            registry.rebind("ModeloSIS_Host" + i, stub);
            servidores.add(servidor);
        }
        
        Thread.sleep(100);
    }
    
    private static void limparServidores() {
        for (int i = 0; i < servidores.size(); i++) {
            try {
                if (servidores.get(i) instanceof ServidorModeloSIRImpl) {
                    UnicastRemoteObject.unexportObject((ServidorModeloSIRImpl)servidores.get(i), true);
                } else if (servidores.get(i) instanceof ServidorModeloSISImpl) {
                    UnicastRemoteObject.unexportObject((ServidorModeloSISImpl)servidores.get(i), true);
                }
                
                Registry registry = registries.get(i);
                UnicastRemoteObject.unexportObject(registry, true);
            } catch (Exception e) {
                // Ignorar erros
            }
        }
        registries.clear();
        servidores.clear();
    }
    
    private static void executarSimulacaoDistribuidaSIR(int numHosts, int numCenarios) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(numHosts);
        List<Future<Void>> futures = new ArrayList<>();
        
        int cenariosPerHost = numCenarios / numHosts;
        int cenariosRestantes = numCenarios % numHosts;
        
        int cenarioInicio = 0;
        for (int hostId = 0; hostId < numHosts; hostId++) {
            int cenariosEsteHost = cenariosPerHost + (hostId < cenariosRestantes ? 1 : 0);
            int cenarioFim = cenarioInicio + cenariosEsteHost;
            
            final int porta = PORTA_BASE + hostId;
            final int inicio = cenarioInicio;
            final int fim = cenarioFim;
            final int hostIdFinal = hostId;
            
            Future<Void> future = executor.submit(() -> {
                try {
                    Registry registry = LocateRegistry.getRegistry("localhost", porta);
                    ModeloRemotoSIR modelo = (ModeloRemotoSIR) registry.lookup("ModeloSIR_Host" + hostIdFinal);
                    
                    for (int cenario = inicio; cenario < fim; cenario++) {
                        double taxaTransmissao = TAXA_TRANSMISSAO_SIR + (cenario * 0.001);
                        modelo.simularSIR(POPULACAO_SIR, INFECTADOS_INICIAIS_SIR, RECUPERADOS_INICIAIS_SIR,
                                         taxaTransmissao, TAXA_RECUPERACAO_SIR, PASSOS_SIR);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            
            futures.add(future);
            cenarioInicio = cenarioFim;
        }
        
        for (Future<Void> future : futures) {
            future.get();
        }
        
        executor.shutdown();
    }
    
    private static void executarSimulacaoDistribuidaSIS(int numHosts, int numCenarios) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(numHosts);
        List<Future<Void>> futures = new ArrayList<>();
        
        int cenariosPerHost = numCenarios / numHosts;
        int cenariosRestantes = numCenarios % numHosts;
        
        int cenarioInicio = 0;
        for (int hostId = 0; hostId < numHosts; hostId++) {
            int cenariosEsteHost = cenariosPerHost + (hostId < cenariosRestantes ? 1 : 0);
            int cenarioFim = cenarioInicio + cenariosEsteHost;
            
            final int porta = PORTA_BASE + hostId;
            final int inicio = cenarioInicio;
            final int fim = cenarioFim;
            final int hostIdFinal = hostId;
            
            Future<Void> future = executor.submit(() -> {
                try {
                    Registry registry = LocateRegistry.getRegistry("localhost", porta);
                    ModeloRemotoSIS modelo = (ModeloRemotoSIS) registry.lookup("ModeloSIS_Host" + hostIdFinal);
                    
                    for (int cenario = inicio; cenario < fim; cenario++) {
                        double taxaTransmissao = TAXA_TRANSMISSAO_SIS + (cenario * 0.001);
                        modelo.simularSIS(POPULACAO_SIS, INFECTADOS_INICIAIS_SIS,
                                         taxaTransmissao, TAXA_RECUPERACAO_SIS, PASSOS_SIS);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            
            futures.add(future);
            cenarioInicio = cenarioFim;
        }
        
        for (Future<Void> future : futures) {
            future.get();
        }
        
        executor.shutdown();
    }
    
    private static void inicializarCSV() throws IOException {
        csvWriter = new PrintWriter(new FileWriter(ARQUIVO_CSV));
        csvWriter.println("Timestamp,Modelo,Tipo,Hosts,Cenarios,Repeticao,Tempo_ms");
        csvWriter.flush();
    }
    
    private static void gravarResultado(String modelo, String tipo, int hosts, 
                                       int cenarios, int repeticao, double tempo) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        csvWriter.printf(Locale.US, "%s,%s,%s,%d,%d,%d,%.4f\n",
            timestamp, modelo, tipo, hosts, cenarios, repeticao, tempo);
        csvWriter.flush();
    }
    
    private static String arrayToString(int[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
    
    // =========================================================================
    // Implementações dos Servidores RMI
    // =========================================================================
    
    static class ServidorModeloSIRImpl implements ModeloRemotoSIR {
        @Override
        public double[] simularSIR(double populacao, double infectadosIniciais, 
                                   double recuperadosIniciais, double taxaTransmissao,
                                   double taxaRecuperacao, int passos) throws RemoteException {
            
            double suscetíveis = populacao - infectadosIniciais - recuperadosIniciais;
            double infectados = infectadosIniciais;
            double recuperados = recuperadosIniciais;
            double dt = 0.1;
            
            for (int i = 0; i < passos; i++) {
                double[] k1 = calcularDerivadasSIR(suscetíveis, infectados, recuperados, 
                                                    populacao, taxaTransmissao, taxaRecuperacao);
                double[] k2 = calcularDerivadasSIR(
                    suscetíveis + k1[0] * dt / 2, infectados + k1[1] * dt / 2,
                    recuperados + k1[2] * dt / 2, populacao, taxaTransmissao, taxaRecuperacao);
                double[] k3 = calcularDerivadasSIR(
                    suscetíveis + k2[0] * dt / 2, infectados + k2[1] * dt / 2,
                    recuperados + k2[2] * dt / 2, populacao, taxaTransmissao, taxaRecuperacao);
                double[] k4 = calcularDerivadasSIR(
                    suscetíveis + k3[0] * dt, infectados + k3[1] * dt,
                    recuperados + k3[2] * dt, populacao, taxaTransmissao, taxaRecuperacao);
                
                suscetíveis += (k1[0] + 2*k2[0] + 2*k3[0] + k4[0]) * dt / 6;
                infectados += (k1[1] + 2*k2[1] + 2*k3[1] + k4[1]) * dt / 6;
                recuperados += (k1[2] + 2*k2[2] + 2*k3[2] + k4[2]) * dt / 6;
            }
            
            return new double[]{suscetíveis, infectados, recuperados};
        }
        
        private double[] calcularDerivadasSIR(double s, double i, double r, double n,
                                              double beta, double gamma) {
            double dS = -beta * s * i / n;
            double dI = beta * s * i / n - gamma * i;
            double dR = gamma * i;
            return new double[]{dS, dI, dR};
        }
    }
    
    static class ServidorModeloSISImpl implements ModeloRemotoSIS {
        @Override
        public double[] simularSIS(double populacao, double infectadosIniciais,
                                   double taxaTransmissao, double taxaRecuperacao,
                                   int passos) throws RemoteException {
            
            double suscetíveis = populacao - infectadosIniciais;
            double infectados = infectadosIniciais;
            double dt = 0.1;
            
            for (int i = 0; i < passos; i++) {
                double[] k1 = calcularDerivadasSIS(suscetíveis, infectados, 
                                                    populacao, taxaTransmissao, taxaRecuperacao);
                double[] k2 = calcularDerivadasSIS(
                    suscetíveis + k1[0] * dt / 2, infectados + k1[1] * dt / 2,
                    populacao, taxaTransmissao, taxaRecuperacao);
                double[] k3 = calcularDerivadasSIS(
                    suscetíveis + k2[0] * dt / 2, infectados + k2[1] * dt / 2,
                    populacao, taxaTransmissao, taxaRecuperacao);
                double[] k4 = calcularDerivadasSIS(
                    suscetíveis + k3[0] * dt, infectados + k3[1] * dt,
                    populacao, taxaTransmissao, taxaRecuperacao);
                
                suscetíveis += (k1[0] + 2*k2[0] + 2*k3[0] + k4[0]) * dt / 6;
                infectados += (k1[1] + 2*k2[1] + 2*k3[1] + k4[1]) * dt / 6;
            }
            
            return new double[]{suscetíveis, infectados};
        }
        
        private double[] calcularDerivadasSIS(double s, double i, double n,
                                              double beta, double gamma) {
            double dS = -beta * s * i / n + gamma * i;
            double dI = beta * s * i / n - gamma * i;
            return new double[]{dS, dI};
        }
    }
}
