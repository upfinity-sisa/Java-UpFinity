package school.sptech;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class Conexao {

    public DriverManagerDataSource getConexao() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/upfinity?useSSL=false&serverTimezone=UTC");
        dataSource.setUsername("funcUpFinity");
        dataSource.setPassword("UpFinity123"); // ajuste conforme seu MySQL
        return dataSource;
    }

}
