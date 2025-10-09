package verification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FingerprintDAO {

    // Save raw Fid data to the database
    public static void saveFingerprint(byte[] fidData) {
        String sql = "INSERT INTO FINGERPRINTTEMPLATES (TEMPLATE) VALUES (?)";

        Connection conn = Database.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, fidData);
            stmt.executeUpdate();
            System.out.println("🧩 Fingerprint saved to database.");
        } catch (SQLException e) {
            System.out.println("❌ Error saving fingerprint: " + e.getMessage());
        }
    }

    // Retrieve all raw Fid data from database
    public static ResultSet getAllFingerprints() {
        String sql = "SELECT TEMPLATE FROM FINGERPRINTTEMPLATES";

        Connection conn = Database.getConnection();
        try {
            Statement stmt = conn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println("❌ Error retrieving fingerprints: " + e.getMessage());
            return null;
        }
    }
}
