package nu.educom.MI6;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database
{
    private Connection conn = null;

    public void connect()
    {
        String url = "jdbc:mysql://localhost:3306/myDB";
        String user = "root";
        String password = "Tijdelijk12";

        try
        {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database");
        } catch (SQLException e)
        {
            System.out.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection()
    {
        return conn;
    }

    public void closeConnection()
    {
        if (conn != null)
        {
            try
            {
                conn.close();
                System.out.println("Disconnected from database");
            } catch (SQLException e)
            {
                System.out.println("Failed to close the connection: " + e.getMessage());
            }
        }
    }
}
