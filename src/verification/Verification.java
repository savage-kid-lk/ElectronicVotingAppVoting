package verification;

import com.digitalpersona.uareu.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Verification {

    private Reader m_reader;
    private CaptureThread m_capture;
    private boolean verified = false;
    private String verifiedVoterId = null;

    public Verification(Reader reader) {
        this.m_reader = reader;
    }

    // Start verification asynchronously with callback
    public void startVerification(VerificationCallback callback) {
        try {
            m_reader.Open(Reader.Priority.COOPERATIVE);
            startCaptureThread(callback);
            System.out.println("🔍 Waiting for fingerprint...");
        } catch (UareUException e) {
            System.err.println("❌ Error opening reader: " + e.getMessage());
            callback.onVerificationComplete(false, null, "Reader error: " + e.getMessage());
        }
    }

    private void startCaptureThread(VerificationCallback callback) {
        m_capture = new CaptureThread(
                m_reader,
                false,
                Fid.Format.ANSI_381_2004,
                Reader.ImageProcessing.IMG_PROC_DEFAULT
        );

        m_capture.start(evt -> {
            if (evt.getActionCommand().equals(CaptureThread.ACT_CAPTURE)) {
                CaptureThread.CaptureEvent ce = (CaptureThread.CaptureEvent) evt;
                if (ce.capture_result != null && ce.capture_result.quality == Reader.CaptureQuality.GOOD) {
                    try {
                        processCapturedFingerprint(ce.capture_result.image, callback);
                    } catch (SQLException ex) {
                        Logger.getLogger(Verification.class.getName()).log(Level.SEVERE, null, ex);
                        callback.onVerificationComplete(false, null, "Database error: " + ex.getMessage());
                    }
                } else if (ce.capture_result != null) {
                    callback.onVerificationComplete(false, null, "Poor fingerprint quality. Please try again.");
                }
            }
        });
    }

    private void stopCaptureThread() {
        if (m_capture != null) {
            try {
                m_capture.cancel();
                m_capture.join(); // ✅ wait until the capture thread fully stops
                System.out.println("🧩 Capture thread stopped.");
            } catch (InterruptedException e) {
                System.err.println("⚠️ Interrupted while stopping capture thread: " + e.getMessage());
            }
        }

        // ✅ Now safely close the reader
        if (m_reader != null) {
            try {
                m_reader.Close();
                System.out.println("🟢 Reader closed successfully.");
            } catch (UareUException e) {
                System.err.println("⚠️ Failed to close reader: " + e.getMessage());
            }
        }
    }

    private void processCapturedFingerprint(Fid fid, VerificationCallback callback) throws SQLException {
        Engine engine = UareUGlobal.GetEngine();

        try {
            // Create an FMD (template) from the captured fingerprint
            Fmd capturedFmd = engine.CreateFmd(fid, Fmd.Format.ANSI_378_2004);
            ResultSet rs = VoterDatabaseLogic.getAllVoters();
            boolean matched = false;
            String matchedVoterId = null;
            String voterName = null;

            if (rs != null) {
                while (rs.next()) {
                    // ✅ Corrected: use FINGERPRINT column from VOTERS table
                    byte[] storedData = rs.getBytes("FINGERPRINT");
                    if (storedData == null || storedData.length == 0) {
                        continue;
                    }

                    int width = fid.getViews()[0].getWidth();
                    int height = fid.getViews()[0].getHeight();
                    int resolution = fid.getImageResolution();
                    int finger_position = fid.getViews()[0].getFingerPosition();
                    int cbeff_id = fid.getCbeffId();

                    // Create stored FMD from byte[] data
                    Fmd storedFmd = engine.CreateFmd(
                            storedData,
                            width,
                            height,
                            resolution,
                            finger_position,
                            cbeff_id,
                            Fmd.Format.ANSI_378_2004
                    );

                    // Compare fingerprints
                    int score = engine.Compare(capturedFmd, 0, storedFmd, 0);

                    if (score < Engine.PROBABILITY_ONE / 100000) {
                        matched = true;
                        matchedVoterId = rs.getString("ID_NUMBER");
                        voterName = rs.getString("NAME") + " " + rs.getString("SURNAME");
                        
                        // Check if voter has already voted using the enhanced method
                        boolean hasVoted = VoterDatabaseLogic.hasVoterVoted(matchedVoterId);
                        if (hasVoted) {
                            // ✅ FIXED: Only record fraud attempt here, NOT in VoterDatabaseLogic
                            // This prevents double recording of fraud attempts
                            VoterDatabaseLogic.recordFraudAttempt(matchedVoterId, "DUPLICATE_VOTE", 
                                capturedFmd.getData().toString(), 
                                "Fingerprint verification detected already voted voter attempting to access system");
                            
                            System.out.println("🚨 Duplicate vote attempt detected for: " + voterName + " (ID: " + matchedVoterId + ")");
                            stopCaptureThread();
                            callback.onVerificationComplete(false, matchedVoterId, 
                                "You have already voted. Duplicate voting is not allowed.");
                            return;
                        }
                        
                        // Check for suspicious activity patterns
                        if (VoterDatabaseLogic.hasSuspiciousActivity(matchedVoterId)) {
                            VoterDatabaseLogic.recordFraudAttempt(matchedVoterId, "SUSPICIOUS_ACTIVITY", null,
                                "Multiple fraud attempts detected within short timeframe");
                            stopCaptureThread();
                            callback.onVerificationComplete(false, matchedVoterId,
                                "Suspicious activity detected. Please contact election officials.");
                            return;
                        }
                        
                        System.out.println("✅ Match found for voter: " + voterName + " (ID: " + matchedVoterId + ")");
                        break;
                    }
                }
            }

            verified = matched;
            verifiedVoterId = matchedVoterId;
            stopCaptureThread();
            
            if (matched) {
                System.out.println("✅ Fingerprint verified successfully for: " + voterName);
                callback.onVerificationComplete(true, matchedVoterId, "Verification successful!");
            } else {
                System.out.println("❌ Verification failed - no matching fingerprint found.");
                callback.onVerificationComplete(false, null, "Fingerprint not recognized. Please ensure you are registered.");
            }

        } catch (UareUException e) {
            System.err.println("❌ Error processing fingerprint: " + e.getMessage());
            stopCaptureThread();
            callback.onVerificationComplete(false, null, "Fingerprint processing error: " + e.getMessage());
        }
    }

    public String getVerifiedVoterId() {
        return verifiedVoterId;
    }

    // Updated callback interface
    public interface VerificationCallback {
        void onVerificationComplete(boolean verified, String voterId, String message);
    }
}