package verification;

import java.sql.*;

public class VoterDatabaseLogic {

    // Retrieve all voters
    public static ResultSet getAllVoters() {
        String sql = "SELECT NAME, SURNAME, ID_NUMBER, FINGERPRINT FROM VOTERS";
        try {
            Statement stmt = Database.getConnection().createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving voters: " + e.getMessage());
            return null;
        }
    }

    /**
     * Fetch all candidates from a specified ballot table, including
     * region/province if applicable.
     */
    public static ResultSet getCandidates(String tableName) {
        String sql;

        // Determine columns dynamically based on updated schema
        switch (tableName) {
            case "RegionalBallot":
                sql = "SELECT id, party_name, candidate_name, candidate_image, party_logo, region FROM " + tableName;
                break;
            case "ProvincialBallot":
                sql = "SELECT id, party_name, candidate_name, candidate_image, party_logo, province FROM " + tableName;
                break;
            default: // NationalBallot or others
                sql = "SELECT id, party_name, candidate_name, candidate_image, party_logo FROM " + tableName;
                break;
        }

        try {
            Connection conn = Database.getConnection();
            if (conn == null || conn.isClosed()) {
                System.err.println("❌ Cannot retrieve candidates — database connection is closed.");
                return null;
            }
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving candidates from " + tableName + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Insert a vote record into the Vote table (no voter info, timestamp automatic).
     *
     * @param category Ballot type: National, Regional, or Provincial
     * @param partyName Party selected
     */
    public static void insertVote(String category, String partyName) {
        String sql = "INSERT INTO Vote (party_name, category) VALUES (?, ?)";
        try {
            Connection conn = Database.getConnection();
            if (conn == null || conn.isClosed()) {
                System.err.println("❌ Cannot insert vote — database connection is closed.");
                return;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, partyName);
                ps.setString(2, category);
                ps.executeUpdate();
                System.out.println("✅ Vote recorded for " + partyName + " (" + category + ")");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error inserting vote for " + partyName + " (" + category + "): " + e.getMessage());
        }
    }
}
