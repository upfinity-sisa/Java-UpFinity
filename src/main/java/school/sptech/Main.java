package school.sptech;

import java.util.List;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException; // Usado para tratar erros de rede/IO
import com.github.britooo.looca.api.group.rede.Rede;
import com.github.britooo.looca.api.group.rede.RedeInterface;
import com.github.britooo.looca.api.group.rede.RedeInterfaceGroup;
import com.github.britooo.looca.api.core.Looca;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

public class Main {

    public static void main(String[] args) {

        Conexao conexao = new Conexao();
        JdbcTemplate template = new JdbcTemplate(conexao.getConexao());
        Looca looca = new Looca();

        // --- Bloco 1 - Identificar o IP local do ATM ---
        String ipAtm = null;
        try {
            Rede rede = looca.getRede();
            RedeInterfaceGroup grupoInterfaces = rede.getGrupoDeInterfaces();
            List<RedeInterface> interfaces = grupoInterfaces.getInterfaces();

            for (RedeInterface iface : interfaces) {

                List<String> enderecosIpv4 = iface.getEnderecoIpv4();
                if (enderecosIpv4.isEmpty()) {
                    continue;
                }

                for (String ip : enderecosIpv4) {
                    InetAddress addr = InetAddress.getByName(ip);
                    if (addr.isSiteLocalAddress()) {
                        ipAtm = ip;
                        break;
                    }
                }
                if (ipAtm != null) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao obter o IP da máquina: " + e.getMessage());
            return;
        }

        if (ipAtm == null) {
            System.err.println("ERRO CRÍTICO: Não foi possível determinar o IP deste ATM.");
            return;
        }

        System.out.println("✅ IP do ATM identificado: " + ipAtm);
        // --- Fim do Bloco 1 ---

        // --- Bloco 2 - Buscar o idAtm usando o IP ---
        Integer idAtm;
        try {
            String sqlBuscaAtm = "SELECT idAtm FROM Atm WHERE IP = ?";
            idAtm = template.queryForObject(
                    sqlBuscaAtm,
                    new Object[]{ipAtm},
                    Integer.class
            );
            System.out.println("✅ ATM identificado no banco. ID: " + idAtm);

        } catch (EmptyResultDataAccessException e) {
            System.err.println(String.format(
                    "ERRO CRÍTICO: Este ATM (IP: %s) NÃO está cadastrado na tabela 'Atm'.", ipAtm
            ));
            System.err.println("O programa será encerrado.");
            return;
        } catch (Exception e) {
            System.err.println("Erro ao buscar ATM no banco: " + e.getMessage());
            return;
        }
        // --- Fim do Bloco 2 ---

        // --- Bloco 3 - Verificação e Cadastro Automático do Componente ---
        Integer idTipoComponenteRede = 4; // ID fixo para o TipoComponente 'Rede'
        Integer idComponenteEncontrado;   // A PK da tabela 'Componente'

        // Query de SELECT usada tanto na tentativa inicial quanto após o cadastro
        String sqlVerificacao = "SELECT idComponente FROM Componente WHERE fkAtm = ? AND fkTipoComponente = ?";

        try {
            // Tenta obter o ID do componente existente
            idComponenteEncontrado = template.queryForObject(
                    sqlVerificacao,
                    new Object[]{idAtm, idTipoComponenteRede},
                    Integer.class
            );

            System.out.println("✅ Componente de Rede já cadastrado para este ATM.");

        } catch (EmptyResultDataAccessException e) {


            System.out.println("⚠️ Componente de Rede não encontrado para o ATM. Iniciando cadastro automático...");

            try {
                // 1. INSERT para criar o Componente
                String sqlInsert = "INSERT INTO Componente (IdComponente, fkAtm, fkTipoComponente) VALUES (4, ?, ?)";
                template.update(sqlInsert, idAtm, idTipoComponenteRede);

                System.out.println("✅ Componente de Rede cadastrado com sucesso!");

                // 2. Re-executar o SELECT para pegar o ID recém-criado
                idComponenteEncontrado = template.queryForObject(
                        sqlVerificacao,
                        new Object[]{idAtm, idTipoComponenteRede},
                        Integer.class
                );

                System.out.println(String.format(
                        "✅ idComponente %d obtido após o cadastro.", idComponenteEncontrado
                ));

            } catch (Exception cadastroE) {
                // Erro na tentativa de INSERT ou no SELECT seguinte
                System.err.println("ERRO CRÍTICO ao cadastrar o Componente: " + cadastroE.getMessage());
                System.err.println("O programa será encerrado.");
                return;
            }

        } catch (Exception e) {
            // Outros erros de banco de dados (fora a EmptyResultDataAccessException)
            System.err.println("Erro inesperado ao verificar/cadastrar componente: " + e.getMessage());
            return;
        }

        System.out.println(String.format(
                "Coletor pronto para iniciar com fkComponente = %d e fkAtm = %d.",
                idComponenteEncontrado, idAtm
        ));
        // --- Fim do Bloco 3 ---

        // --- Bloco 4 - Loop de Captura (Inserção em CapturaRede e Captura) ---
        while (true) {
            RedeInterface maiorInterface = null;
            long maiorBytes = 0;

            // Recarrega os dados das interfaces a CADA loop
            List<RedeInterface> interfacesAtualizadas = looca.getRede().getGrupoDeInterfaces().getInterfaces();

            for (RedeInterface iface : interfacesAtualizadas) {
                long bytesRecebidos = iface.getBytesRecebidos();
                if (bytesRecebidos > maiorBytes) {
                    maiorBytes = bytesRecebidos;
                    maiorInterface = iface;
                }
            }

            if (maiorInterface != null) {
                // 1. MB Recebidos (Métrica comum e Valor principal)
                double mbRecebidos = maiorInterface.getBytesRecebidos() / (1024.0 * 1024.0);

                // 2. Pacotes Recebidos (INT)
                long pacotesRecebidos = maiorInterface.getPacotesRecebidos().longValue();

                // 3. Pacotes Enviados (INT)
                long pacotesEnviados = maiorInterface.getPacotesEnviados().longValue();

                String nomeRede = maiorInterface.getNome();

                System.out.println("--- Captura de Rede ---");
                System.out.println("Interface: " + nomeRede);
                System.out.printf("MB Recebidos: %.2f%n", mbRecebidos);
                System.out.printf("Pacotes Recebidos: %d%n", pacotesRecebidos);
                System.out.printf("Pacotes Enviados: %d%n", pacotesEnviados);
                System.out.println("-----------------------");


                // === INSERT 1: Tabela CapturaRede (Dados detalhados) ===
                try {
                    String sqlCapturaRede = "INSERT INTO CapturaRede (fkComponente, fkAtm, nomeRede, MBRecebidos, pacotesRecebidos, pacotesEnviados, horario) VALUES (?, ?, ?, ?, ?, ?, NOW())";

                    template.update(
                            sqlCapturaRede,
                            idComponenteEncontrado,
                            idAtm,
                            nomeRede,
                            mbRecebidos,
                            pacotesRecebidos,
                            pacotesEnviados
                    );

                    System.out.println("✅ Dados detalhados inseridos em CapturaRede.");
                } catch (Exception e) {
                    System.err.println("❌ Erro ao inserir dados em CapturaRede: " + e.getMessage());
                }

                // === INSERT 2: Tabela Captura (Dados genéricos/Resumo) ===
                try {
                    // Inserimos o MB recebido como valor principal na tabela Captura
                    String sqlCaptura = "INSERT INTO Captura (fkComponente, fkAtm, valor, horario) VALUES (?, ?, ?, NOW())";

                    template.update(
                            sqlCaptura,
                            idComponenteEncontrado,
                            idAtm,
                            mbRecebidos // Valor principal para a tabela genérica
                    );

                    System.out.println("✅ Dados genéricos inseridos em Captura.");
                } catch (Exception e) {
                    System.err.println("❌ Erro ao inserir dados em Captura: " + e.getMessage());
                }

            } else {
                System.out.println("Nenhuma interface de rede significativa encontrada.");
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                System.err.println("Thread interrompida: " + e.getMessage());
                break;
            }
        }
        // --- Fim do Bloco 4 ---
    }
}