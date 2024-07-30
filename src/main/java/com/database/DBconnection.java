package src.main.java.com.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase {
    private static final String URL = "jdbc:mysql://Localhost:3306/majorassignment";
    private static final String USER = "root";
    private static final String PASSWORD = "tong@123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
