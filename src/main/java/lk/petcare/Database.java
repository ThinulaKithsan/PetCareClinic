package lk.petcare;
import java.sql.*;
public final class Database {
    private Database() {}
    public static Connection open() throws SQLException {
        String url = env("PETCARE_DB_URL", "jdbc:mysql://localhost:3306/petcare?connectionTimeZone=LOCAL&connectTimeout=5000&socketTimeout=15000");
        String user = env("PETCARE_DB_USER", "petcare_app");
        String password = System.getenv("PETCARE_DB_PASSWORD");
        if (password == null || password.isBlank())
            throw new SQLException("Set PETCARE_DB_PASSWORD before launching the application.");
        return DriverManager.getConnection(url, user, password);
    }
    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
