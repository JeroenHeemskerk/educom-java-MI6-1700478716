package nu.educom.MI6;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static Connection conn = null;

    public static void openConnection()
    {
        String url = "jdbc:mysql://localhost:3306/myDB";
        String user = "root";
        String password = "Tijdelijk12";

        try
        {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database");
        }
        catch(SQLException e)
        {
            System.out.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection()
    {
        return conn;
    }

    public static void closeConnection()
    {
        if(conn != null)
        {
            try
            {
                conn.close();
                System.out.println("Disconnected from database");
            }
            catch (SQLException e) {
                System.out.println("Failed to close the connection: " + e.getMessage());
            }
        }
    }
}

