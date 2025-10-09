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

    public Verification(Reader reader) {
        this.m_reader = reader;
    }

    // Start verification asynchronously with callback
    public void startVerification(VerificationCallback callback) {
        new Thread(() -> {
            try {
                m_reader.Open(Reader.Priority.COOPERATIVE);
                startCaptureThread(callback);
            } catch (UareUException e) {
                System.err.println("❌ Error opening reader: " + e.getMessage());
                callback.onVerificationComplete(false);
                return;
            }

            System.out.println("🔍 Waiting for fingerprint...");
            waitForCaptureThread();

            try { 
                m_reader.Close(); 
            } catch (UareUException ignored) {}
        }).start();
    }

    private void startCaptureThread(VerificationCallback callback) {
        m_capture = new CaptureThread(m_reader, false, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT);
        m_capture.start(evt -> {
            if (evt.getActionCommand().equals(CaptureThread.ACT_CAPTURE)) {
                CaptureThread.CaptureEvent ce = (CaptureThread.CaptureEvent) evt;
                if (ce.capture_result != null && ce.capture_result.quality == Reader.CaptureQuality.GOOD) {
                    try {
                        processCapturedFingerprint(ce.capture_result.image, callback);
                    } catch (SQLException ex) {
                        Logger.getLogger(Verification.class.getName()).log(Level.SEVERE, null, ex);
                        callback.onVerificationComplete(false);
                    }
                }
            }
        });
    }

    private void stopCaptureThread() {
        if (m_capture != null) m_capture.cancel();
    }

    private void waitForCaptureThread() {
        if (m_capture != null) {
            try {
                m_capture.join(); // Wait until capture finishes
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void processCapturedFingerprint(Fid fid, VerificationCallback callback) throws SQLException {
        Engine engine = UareUGlobal.GetEngine();
        try {
            Fmd capturedFmd = engine.CreateFmd(fid, Fmd.Format.ANSI_378_2004);
            ResultSet rs = FingerprintDAO.getAllFingerprints();
            boolean matched = false;

            if (rs != null) {
                while (rs.next()) {
                    byte[] storedData = rs.getBytes("TEMPLATE");
                    int width = fid.getViews()[0].getWidth();
                    int height = fid.getViews()[0].getHeight();
                    int resolution = fid.getImageResolution();
                    int finger_position = fid.getViews()[0].getFingerPosition();
                    int cbeff_id = fid.getCbeffId();

                    Fmd storedFmd = engine.CreateFmd(storedData, width, height, resolution, finger_position, cbeff_id, Fmd.Format.ANSI_378_2004);
                    int score = engine.Compare(capturedFmd, 0, storedFmd, 0);

                    if (score < Engine.PROBABILITY_ONE / 100000) {
                        matched = true;
                        break;
                    }
                }
            }

            verified = matched;
            stopCaptureThread();
            System.out.println(matched ? "✅ Fingerprint verified successfully!" : "❌ Verification failed.");
            callback.onVerificationComplete(matched);

        } catch (UareUException e) {
            System.err.println("❌ Error processing fingerprint: " + e.getMessage());
            callback.onVerificationComplete(false);
        }
    }

    // Callback interface
    public interface VerificationCallback {
        void onVerificationComplete(boolean verified);
    }
}
