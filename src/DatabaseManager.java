//package db_proj;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
	private static final String URL = "jdbc:postgresql://localhost:5432/ExamGenerator";
	private static final String USER = "postgres";
	private static final String PASSWORD = "Tk207459009";

	public static Connection getConnection() throws SQLException {
	     return DriverManager.getConnection(URL, USER, PASSWORD);
	    }
}
