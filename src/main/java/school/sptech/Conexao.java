package school.sptech;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class Conexao {

    public DriverManagerDataSource getConexao() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://3.212.222.224:3306/upfinity?useSSL=false&serverTimezone=UTC");
        dataSource.setUsername("funcUpFinity");
<<<<<<< HEAD
        dataSource.setPassword("Upfinity123");
=======
        dataSource.setPassword("UpFinity123"); // ajuste conforme seu MySQL
>>>>>>> c49969b58a8e95ecb77a1c5a943d21856173e2fb
        return dataSource;
    }
}
