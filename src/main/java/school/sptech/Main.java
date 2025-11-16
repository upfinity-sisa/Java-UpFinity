package school.sptech;

import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.rede.Rede;
import com.github.britooo.looca.api.group.rede.RedeInterface;
import com.github.britooo.looca.api.group.rede.RedeInterfaceGroup;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Conexao conexao = new Conexao();
        JdbcTemplate template = new JdbcTemplate(conexao.getConexao());
        Looca looca = new Looca();

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
                    break; // Para de procurar em outras interfaces
                }
            }

        } catch (IOException e) {
            System.err.println("Erro ao obter o IP da máquina: " + e.getMessage());
            return; // Encerra
        }

        if (ipAtm == null) {
            System.err.println("ERRO CRÍTICO: Não foi possível determinar o IP deste ATM.");
            return; // Encerra
        }

        System.out.println("✅ IP do ATM identificado: " + ipAtm);

        Integer idAtm; // Esta variável virá do banco
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
            System.err.println("Verifique o cadastro no banco. O programa será encerrado.");
            return; // Encerra
        } catch (Exception e) {
            System.err.println("Erro ao buscar ATM no banco: " + e.getMessage());
            return; // Encerra
        }

        Integer idTipoComponenteRede = 4; // O "Tipo" de componente Rede
        Integer idComponenteEncontrado;   // A PK da tabela 'Componente'

        try {
            String sqlVerificacao = "SELECT idComponente FROM Componente WHERE fkAtm = ? AND fkTipoComponente = ?";
            idComponenteEncontrado = template.queryForObject(
                    sqlVerificacao,
                    new Object[]{idAtm, idTipoComponenteRede}, // Usa o idAtm encontrado
                    Integer.class
            );

            System.out.println(String.format(
                    "✅ Verificação bem-sucedida. Capturando dados para o ATM %d (Tipo %d) usando o idComponente %d.",
                    idAtm, idTipoComponenteRede, idComponenteEncontrado
            ));

        } catch (EmptyResultDataAccessException e) {
            System.err.println(String.format(
                    "ERRO CRÍTICO: O Tipo de Componente 'Rede' (ID %d) NÃO está associado ao ATM (ID %d) na tabela 'Componente'.",
                    idTipoComponenteRede, idAtm // Usa o idAtm na msg de erro
            ));
            System.err.println("Verifique o cadastro no banco. O programa será encerrado.");
            return;
        } catch (Exception e) {
            System.err.println("Erro ao verificar componente no banco: " + e.getMessage());
            return;
        }

        while (true) {
            RedeInterface maiorInterface = null;
            long maiorBytes = 0;

            // --- Alteração: Recarrega os dados das interfaces A CADA loop ---
            List<RedeInterface> interfacesAtualizadas = looca.getRede().getGrupoDeInterfaces().getInterfaces();


            for (RedeInterface iface : interfacesAtualizadas) { // Usa a lista atualizada
                long bytesRecebidos = iface.getBytesRecebidos();
                if (bytesRecebidos > maiorBytes) {
                    maiorBytes = bytesRecebidos;
                    maiorInterface = iface;
                }
            }

            if (maiorInterface != null) {
                double bytesRecebidosMB = maiorInterface.getBytesRecebidos() / (1024.0 * 1024.0);

                System.out.println("Interface com mais bytes recebidos:");
                System.out.println("Nome: " + maiorInterface.getNome());
                System.out.printf("MegaBytes Recebidos: %.2f %n", bytesRecebidosMB);
                System.out.println("-----------------------------------------");

                try {
                    // O INSERT agora usa as duas FKs dinâmicas
                    String sql = "INSERT INTO Captura (fkComponente, fkAtm, valor, horario) VALUES (?, ?, ?, NOW())";
                    template.update(sql, idComponenteEncontrado, idAtm, bytesRecebidosMB);

                    System.out.println("✅ Dados inseridos no banco com sucesso!");
                } catch (Exception e) {
                    System.err.println("Erro ao inserir no banco: " + e.getMessage());
                }

            } else {
                System.out.println("Nenhuma interface encontrada.");
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                System.err.println("Thread interrompida: " + e.getMessage());
                break;
            }
        }
    }
}