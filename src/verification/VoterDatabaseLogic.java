package verification;

import java.sql.*;

public class VoterDatabaseLogic {

    // ✅ ENHANCED: Check if voter has already voted by checking both VOTERS.has_voted and Votes table
    public static boolean hasVoterVoted(String idNumber) {
        String sql = "SELECT v.has_voted, COUNT(vt.id) as vote_count " +
                    "FROM VOTERS v " +
                    "LEFT JOIN Votes vt ON v.ID_NUMBER = vt.voter_id_number " +
                    "WHERE v.ID_NUMBER = ? " +
                    "GROUP BY v.ID_NUMBER, v.has_voted";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = VoterDatabaseConnectivity.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, idNumber);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                boolean hasVotedFlag = rs.getBoolean("has_voted");
                int voteCount = rs.getInt("vote_count");
                boolean result = hasVotedFlag || voteCount > 0;
                System.out.println("🔍 Checking voter " + idNumber + " - has_voted: " + hasVotedFlag + ", vote_count: " + voteCount + ", result: " + result);
                return result;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking voter status: " + e.getMessage());
        } finally {
            // Close only the statement and result set, NOT the connection
            closeResources(rs, ps, null);
        }
        return false;
    }

    // ✅ ENHANCED: Record fraud attempt with detailed information
    public static void recordFraudAttempt(String idNumber, String attemptType, String fingerprintHash, String details) {
        String sql = "INSERT INTO FraudAttempts (voter_id_number, attempt_type, fingerprint_hash, details, attempt_count) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = VoterDatabaseConnectivity.getConnection();
            ps = conn.prepareStatement(sql);
            
            ps.setString(1, idNumber);
            ps.setString(2, attemptType);
            ps.setString(3, fingerprintHash);
            ps.setString(4, details);
            
            // Get current attempt count for this voter
            int currentCount = getFraudAttemptCount(idNumber, attemptType);
            ps.setInt(5, currentCount + 1);
            
            ps.executeUpdate();
            
            System.out.println("🚨 Fraud attempt recorded: " + attemptType + " for voter: " + idNumber);
        } catch (SQLException e) {
            System.err.println("❌ Error recording fraud attempt: " + e.getMessage());
        } finally {
            closeResources(null, ps, null);
        }
    }

    // ✅ NEW: Enhanced fraud attempt count with type filtering
    public static int getFraudAttemptCount(String idNumber, String attemptType) {
        String sql = "SELECT COUNT(*) as attempt_count FROM FraudAttempts " +
                    "WHERE voter_id_number = ? AND attempt_type = ? " +
                    "AND timestamp > DATE_SUB(NOW(), INTERVAL 24 HOUR)";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = VoterDatabaseConnectivity.getConnection();
            ps = conn.prepareStatement(sql);
            
            ps.setString(1, idNumber);
            ps.setString(2, attemptType);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("attempt_count");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking fraud attempts: " + e.getMessage());
        } finally {
            closeResources(rs, ps, null);
        }
        return 0;
    }

    // ✅ NEW: Check for suspicious activity patterns
    public static boolean hasSuspiciousActivity(String idNumber) {
        String sql = "SELECT COUNT(*) as suspicious_count FROM FraudAttempts " +
                    "WHERE voter_id_number = ? " +
                    "AND attempt_type IN ('DUPLICATE_VOTE', 'UNREGISTERED_VOTER') " +
                    "AND timestamp > DATE_SUB(NOW(), INTERVAL 1 HOUR) " +
                    "HAVING COUNT(*) >= 2";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = VoterDatabaseConnectivity.getConnection();
            ps = conn.prepareStatement(sql);
            
            ps.setString(1, idNumber);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("suspicious_count") >= 2;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking suspicious activity: " + e.getMessage());
        } finally {
            closeResources(rs, ps, null);
        }
        return false;
    }

    // ✅ NEW: Insert ALL THREE votes at once in a single transaction
    public static boolean insertAllVotes(String nationalParty, String regionalParty, String provincialParty, String voterIdNumber) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = VoterDatabaseConnectivity.getConnection();

            // Start transaction
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // Check if voter has already voted BEFORE starting
            if (hasVoterVoted(voterIdNumber)) {
                // ✅ FIXED: Removed duplicate fraud recording - this should never happen if verification works correctly
                System.err.println("❌ CRITICAL: Voter " + voterIdNumber + " passed verification but has already voted!");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return false;
            }

            // Check for suspicious activity
            if (hasSuspiciousActivity(voterIdNumber)) {
                recordFraudAttempt(voterIdNumber, "SUSPICIOUS_ACTIVITY", null,
                    "Multiple fraud attempts detected within short timeframe");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return false;
            }

            // Insert National vote
            String voteSql = "INSERT INTO Votes (voter_id_number, category, party_name) VALUES (?, ?, ?)";
            ps = conn.prepareStatement(voteSql);
            ps.setString(1, voterIdNumber);
            ps.setString(2, "National");
            ps.setString(3, nationalParty);
            ps.executeUpdate();
            ps.close();
            System.out.println("✅ National vote recorded for " + nationalParty + " by voter: " + voterIdNumber);

            // Insert Regional vote
            ps = conn.prepareStatement(voteSql);
            ps.setString(1, voterIdNumber);
            ps.setString(2, "Regional");
            ps.setString(3, regionalParty);
            ps.executeUpdate();
            ps.close();
            System.out.println("✅ Regional vote recorded for " + regionalParty + " by voter: " + voterIdNumber);

            // Insert Provincial vote
            ps = conn.prepareStatement(voteSql);
            ps.setString(1, voterIdNumber);
            ps.setString(2, "Provincial");
            ps.setString(3, provincialParty);
            ps.executeUpdate();
            ps.close();
            System.out.println("✅ Provincial vote recorded for " + provincialParty + " by voter: " + voterIdNumber);

            // Mark voter as voted ONLY after all three votes are successfully inserted
            if (!markVoterAsVoted(voterIdNumber)) {
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                System.err.println("❌ Failed to mark voter as voted: " + voterIdNumber);
                return false;
            }

            // Commit transaction
            conn.commit();
            conn.setAutoCommit(originalAutoCommit);
            System.out.println("🎉 All three votes successfully recorded for voter: " + voterIdNumber);
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                }
            } catch (SQLException rollbackEx) {
                System.err.println("❌ Error during rollback: " + rollbackEx.getMessage());
            }
            System.err.println("❌ Error inserting votes for voter " + voterIdNumber + ": " + e.getMessage());
            return false;
        } finally {
            closeResources(null, ps, null);
        }
    }

    // ✅ DEPRECATED: Single vote insertion (keep for backward compatibility but mark as deprecated)
    public static boolean insertVote(String category, String partyName, String voterIdNumber) {
        System.err.println("⚠️ WARNING: Single vote insertion is deprecated. Use insertAllVotes() instead.");
        // For single vote, we'll still allow it but with proper checks
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = VoterDatabaseConnectivity.getConnection();

            // Start transaction
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // Check if voter has already voted
            if (hasVoterVoted(voterIdNumber)) {
                // ✅ FIXED: Removed duplicate fraud recording - this should never happen if verification works correctly
                System.err.println("❌ CRITICAL: Voter " + voterIdNumber + " passed verification but has already voted!");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return false;
            }

            // Insert vote into Votes table
            String voteSql = "INSERT INTO Votes (voter_id_number, category, party_name) VALUES (?, ?, ?)";
            ps = conn.prepareStatement(voteSql);
            ps.setString(1, voterIdNumber);
            ps.setString(2, category);
            ps.setString(3, partyName);
            ps.executeUpdate();
            ps.close();

            // Mark voter as voted
            if (!markVoterAsVoted(voterIdNumber)) {
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                System.err.println("❌ Failed to mark voter as voted: " + voterIdNumber);
                return false;
            }

            // Commit transaction
            conn.commit();
            conn.setAutoCommit(originalAutoCommit);
            System.out.println("✅ Vote recorded for " + partyName + " (" + category + ") by voter: " + voterIdNumber);
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                }
            } catch (SQLException rollbackEx) {
                System.err.println("❌ Error during rollback: " + rollbackEx.getMessage());
            }
            System.err.println("❌ Error inserting vote for " + partyName + " (" + category + "): " + e.getMessage());
            return false;
        } finally {
            closeResources(null, ps, null);
        }
    }

    // ✅ EXISTING METHODS - FIXED to not use try-with-resources
    public static ResultSet getAllVoters() {
        String sql = "SELECT ID, NAME, SURNAME, ID_NUMBER, FINGERPRINT, has_voted FROM VOTERS";
        try {
            Connection conn = VoterDatabaseConnectivity.getConnection();
            Statement stmt = conn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving voters: " + e.getMessage());
            return null;
        }
    }

    public static boolean markVoterAsVoted(String idNumber) {
        String sql = "UPDATE VOTERS SET has_voted = TRUE WHERE ID_NUMBER = ?";
        
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = VoterDatabaseConnectivity.getConnection();
            ps = conn.prepareStatement(sql);
            
            ps.setString(1, idNumber);
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Marked voter " + idNumber + " as voted");
                return true;
            } else {
                System.err.println("❌ No voter found with ID: " + idNumber);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error marking voter as voted: " + e.getMessage());
            return false;
        } finally {
            closeResources(null, ps, null);
        }
    }

    public static ResultSet getCandidates(String tableName) {
        String sql;
        switch (tableName) {
            case "RegionalBallot":
                sql = "SELECT id, party_name, candidate_name, candidate_image, party_logo, region FROM " + tableName;
                break;
            case "ProvincialBallot":
                sql = "SELECT id, party_name, candidate_name, candidate_image, party_logo, province FROM " + tableName;
                break;
            default:
                sql = "SELECT id, party_name, candidate_name, candidate_image, party_logo FROM " + tableName;
                break;
        }
        try {
            Connection conn = VoterDatabaseConnectivity.getConnection();
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving candidates from " + tableName + ": " + e.getMessage());
            return null;
        }
    }

    public static boolean voterExists(String idNumber) {
        String sql = "SELECT COUNT(*) as count FROM VOTERS WHERE ID_NUMBER = ?";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = VoterDatabaseConnectivity.getConnection();
            ps = conn.prepareStatement(sql);
            
            ps.setString(1, idNumber);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking if voter exists: " + e.getMessage());
        } finally {
            closeResources(rs, ps, null);
        }
        return false;
    }

    // ✅ NEW: Reset voter voting status (for testing)
    public static boolean resetVoterVotingStatus(String idNumber) {
        String sql = "UPDATE VOTERS SET has_voted = FALSE WHERE ID_NUMBER = ?";
        
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = VoterDatabaseConnectivity.getConnection();
            ps = conn.prepareStatement(sql);
            
            ps.setString(1, idNumber);
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Reset voting status for voter: " + idNumber);
                return true;
            } else {
                System.err.println("❌ No voter found with ID: " + idNumber);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error resetting voter status: " + e.getMessage());
            return false;
        } finally {
            closeResources(null, ps, null);
        }
    }

    // ✅ NEW: Delete all votes for a voter (for testing)
    public static boolean deleteVoterVotes(String idNumber) {
        String sql = "DELETE FROM Votes WHERE voter_id_number = ?";
        
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = VoterDatabaseConnectivity.getConnection();
            ps = conn.prepareStatement(sql);
            
            ps.setString(1, idNumber);
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Deleted " + rowsAffected + " votes for voter: " + idNumber);
            }
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error deleting voter votes: " + e.getMessage());
            return false;
        } finally {
            closeResources(null, ps, null);
        }
    }

    // ✅ NEW: Helper method to close resources without closing connection
    private static void closeResources(ResultSet rs, PreparedStatement ps, Statement stmt) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            System.err.println("❌ Error closing ResultSet: " + e.getMessage());
        }
        try {
            if (ps != null) ps.close();
        } catch (SQLException e) {
            System.err.println("❌ Error closing PreparedStatement: " + e.getMessage());
        }
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Error closing Statement: " + e.getMessage());
        }
        // NOTE: Connection is NOT closed here - it stays open for the entire app lifecycle
    }
}