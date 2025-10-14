package school.sptech;

import java.util.List;
import com.github.britooo.looca.api.group.rede.Rede;
import com.github.britooo.looca.api.group.rede.RedeInterface;
import com.github.britooo.looca.api.group.rede.RedeInterfaceGroup;
import com.github.britooo.looca.api.core.Looca;
import org.springframework.jdbc.core.JdbcTemplate;

public class Main {

    public static void main(String[] args) {

        Conexao conexao = new Conexao();
        JdbcTemplate template = new JdbcTemplate(conexao.getConexao());


        Looca looca = new Looca();
        Rede rede = looca.getRede();
        RedeInterfaceGroup grupoInterfaces = rede.getGrupoDeInterfaces();
        List<RedeInterface> interfaces = grupoInterfaces.getInterfaces();

        while (true) {
            RedeInterface maiorInterface = null;
            long maiorBytes = 0;

            for (RedeInterface iface : interfaces) {
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
                    String sql = "INSERT INTO rede (nome_interface, megabytes_recebidos, momento_registro) VALUES (?, ?, NOW())";
                    template.update(sql, maiorInterface.getNome(), bytesRecebidosMB);

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