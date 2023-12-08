package nu.educom.MI6;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginAttempt
{
    private Database db = new Database();

    public void insertLoginAttempt(int dienstnummer, boolean loginSuccessful)
    {
        Connection conn = db.getConnection();
        String sqlInsert = "INSERT INTO login_attempts (dienstnummer, login_timestamp, login_successful) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert))
        {
            pstmt.setInt(1, dienstnummer);
            pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            pstmt.setBoolean(3, loginSuccessful);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0)
            {
                System.out.println("Login attempt recorded successfully.");
            }
            else
            {
                System.out.println("Login attempt failed to record.");
            }
        }
        catch (SQLException e)
        {
            System.out.println("SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<String> retrieveLoginAttempts(Timestamp currentLoginTimestamp)
    {
        List<String> failedAttempts = new ArrayList<>();
        Connection conn = Database.getConnection();
        String sqlQuery =
                "SELECT * FROM login_attempts " +
                        "WHERE login_successful = FALSE AND " +
                        "login_timestamp > (SELECT MAX(login_timestamp) FROM login_attempts WHERE login_successful = TRUE AND login_timestamp < ?) AND " +
                        "login_timestamp < ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sqlQuery))
        {
            pstmt.setTimestamp(1, currentLoginTimestamp);
            pstmt.setTimestamp(2, currentLoginTimestamp);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
            {
                int dienstnummer = rs.getInt("dienstnummer");
                Timestamp timestamp = rs.getTimestamp("login_timestamp");
                failedAttempts.add("Agent " + dienstnummer + " - Gefaalde login poging op: " + timestamp);
            }
        }
        catch (SQLException e)
        {
            System.out.println("SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return failedAttempts;
    }

    public void saveBlacklistToDatabase(HashMap<String, Long[]> blackList)
    {
        Connection conn = Database.getConnection();
        if(conn != null)
        {
            try
            {
                for(Map.Entry<String, Long[]> entry : blackList.entrySet())
                {
                    String serviceNumber = entry.getKey();
                    Long[] details = entry.getValue();
                    String query = "INSERT INTO blacklist (dienstnummer, blacklist_time) VALUES (?, ?) ON DUPLICATE KEY UPDATE blacklist_time = ?";
                    try(PreparedStatement preparedStatement = conn.prepareStatement(query))
                    {
                        preparedStatement.setString(1, serviceNumber);
                        preparedStatement.setLong(2, details[1]);
                        preparedStatement.setLong(3, details[1]);
                        preparedStatement.executeUpdate();
                    }
                }
            }
            catch(SQLException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public void updateDatabaseBlacklist()
    {
        Connection conn = Database.getConnection();
        if(conn != null)
        {
            try
            {
                long currentTime = System.currentTimeMillis();

                String deleteQuery =
                        "DELETE b FROM blacklist b " +
                                "JOIN (SELECT dienstnummer, MAX(login_timestamp) AS login_timestamp FROM login_attempts GROUP BY dienstnummer) la " +
                                "ON b.dienstnummer = la.dienstnummer " +
                                "WHERE (UNIX_TIMESTAMP(la.login_timestamp) * 1000 + b.blacklist_time) <= ?";

                try(PreparedStatement preparedStatement = conn.prepareStatement(deleteQuery))
                {
                    preparedStatement.setLong(1, currentTime);
                    preparedStatement.executeUpdate();
                }

                String updateQuery =
                        "UPDATE blacklist b " +
                                "JOIN (SELECT dienstnummer, MAX(login_timestamp) AS login_timestamp FROM login_attempts GROUP BY dienstnummer) la " +
                                "ON b.dienstnummer = la.dienstnummer " +
                                "SET b.blacklist_time = (UNIX_TIMESTAMP(la.login_timestamp) * 1000 + b.blacklist_time) - ? " +
                                "WHERE (UNIX_TIMESTAMP(la.login_timestamp) * 1000 + b.blacklist_time) > ?";

                try(PreparedStatement preparedStatement = conn.prepareStatement(updateQuery))
                {
                    preparedStatement.setLong(1, currentTime);
                    preparedStatement.setLong(2, currentTime);
                    preparedStatement.executeUpdate();
                }
            }
            catch(SQLException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public void loadBlacklistFromDatabase(HashMap<String, Long[]> databaseBlackList)
    {
        Connection conn = Database.getConnection();
        if(conn != null)
        {
            try
            {
                String query = "SELECT dienstnummer, blacklist_time FROM blacklist";
                try (Statement statement = conn.createStatement();
                     ResultSet resultSet = statement.executeQuery(query))
                {
                    while(resultSet.next())
                    {
                        int serviceNumberInt = resultSet.getInt("dienstnummer");
                        String serviceNumber = String.format("%03d", serviceNumberInt);
                        long blacklistTime = resultSet.getLong("blacklist_time");

                        databaseBlackList.put(serviceNumber, new Long[]{System.currentTimeMillis(), blacklistTime});
                    }
                }
            }
            catch(SQLException ex)
            {
                ex.printStackTrace();
            }
        }
    }
}
