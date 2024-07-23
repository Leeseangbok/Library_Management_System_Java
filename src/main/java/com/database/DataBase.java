package src.main.java.com.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/library_system";
    private static final String USER = "root";
    private static final String PASSWORD = "OhWowBoomBoomPow";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
