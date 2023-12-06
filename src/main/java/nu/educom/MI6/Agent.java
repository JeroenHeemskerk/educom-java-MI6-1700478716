package nu.educom.MI6;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Agent
{
    private Database db = new Database();

    public class AgentAuthResult {
        public boolean isAuthenticated;
        public boolean licenceToKill;
        public String expirationDate;

        public AgentAuthResult(boolean isAuthenticated, boolean licenceToKill, String expirationDate) {
            this.isAuthenticated = isAuthenticated;
            this.licenceToKill = licenceToKill;
            this.expirationDate = expirationDate;
        }
    }

    public AgentAuthResult authenticateAgent(int serviceNumber, String secretCode)
    {
        boolean isAuthenticated = false;
        boolean licenceToKill = false;
        String expirationDate = null;
        db.connect();
        Connection conn = db.getConnection();
        String query = "SELECT licence_to_kill, expiration_date FROM agents WHERE dienstnummer = ? AND geheime_code = ? AND active = 1";

        try(PreparedStatement stmt = conn.prepareStatement(query))
        {
            stmt.setInt(1, serviceNumber);
            stmt.setString(2, secretCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                isAuthenticated = true;
                licenceToKill = rs.getBoolean("licence_to_kill");
                expirationDate = rs.getString("expiration_date");
            }
        }
        catch(SQLException e)
        {
            System.out.println("Query failed: " + e.getMessage());
        }
        finally
        {
            db.closeConnection();
        }
        return new AgentAuthResult(isAuthenticated, licenceToKill, expirationDate);
    }
}
