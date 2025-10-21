package school.sptech;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class Conexao {

    public DriverManagerDataSource getConexao() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://3.212.222.224:3306/upfinity?useSSL=false&serverTimezone=UTC");
        dataSource.setUsername("root");
        dataSource.setPassword("1234"); // ajuste conforme seu MySQL
        return dataSource;
    }
}
