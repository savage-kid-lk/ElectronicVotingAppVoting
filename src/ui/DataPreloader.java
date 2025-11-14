package ui;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import verification.VoterDatabaseLogic;

public class DataPreloader {

    private static Map<String, List<CandidateData>> ballotData = new HashMap<>();
    private static boolean isPreloaded = false;
    private static long lastPreloadTime = 0;

    // 5 minutes cache time
    private static final long PRELOAD_TIMEOUT = 300000;

    public static class CandidateData {
        public String partyName;
        public String candidateName;
        public BufferedImage candidateImage;
        public BufferedImage partyLogo;

        public CandidateData(String partyName, String candidateName,
                             BufferedImage candidateImage, BufferedImage partyLogo) {
            this.partyName = partyName;
            this.candidateName = candidateName;
            this.candidateImage = candidateImage;
            this.partyLogo = partyLogo;
        }
    }

    /**
     * BLOCKING VERSION — HomePage waits until this finishes.
     */
    public static synchronized void preloadAllData() {

        // If already loaded and not expired, skip reload
        if (isPreloaded && (System.currentTimeMillis() - lastPreloadTime) < PRELOAD_TIMEOUT) {
            return;
        }

        try {
            // IMPORTANT — blocking calls (no threads!!)
            preloadBallotData("NationalBallot");
            preloadBallotData("RegionalBallot");
            preloadBallotData("ProvincialBallot");

            isPreloaded = true;
            lastPreloadTime = System.currentTimeMillis();

        } catch (Exception e) {
            System.err.println("Error preloading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void preloadBallotData(String ballotType) {
        try {
            ResultSet rs = VoterDatabaseLogic.getCandidates(ballotType);
            List<CandidateData> candidates = new ArrayList<>();

            if (rs != null) {
                while (rs.next()) {
                    String partyName = rs.getString("party_name");
                    String candidateName = rs.getString("candidate_name");

                    byte[] candidateImageBytes = rs.getBytes("candidate_image");
                    byte[] partyLogoBytes = rs.getBytes("party_logo");

                    BufferedImage candidateImage = null;
                    BufferedImage partyLogo = null;

                    // candidate image
                    if (candidateImageBytes != null && candidateImageBytes.length > 10) {
                        try {
                            candidateImage = ImageIO.read(new ByteArrayInputStream(candidateImageBytes));
                        } catch (Exception ignore) {}
                    }

                    // party logo (skip for independents)
                    if (partyLogoBytes != null && partyLogoBytes.length > 10 &&
                            !partyName.equalsIgnoreCase("Independent")) {
                        try {
                            partyLogo = ImageIO.read(new ByteArrayInputStream(partyLogoBytes));
                        } catch (Exception ignore) {}
                    }

                    candidates.add(new CandidateData(
                            partyName, candidateName, candidateImage, partyLogo
                    ));
                }

                ballotData.put(ballotType, candidates);
            }

        } catch (SQLException e) {
            System.err.println("SQL Error preloading " + ballotType + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error preloading " + ballotType + ": " + e.getMessage());
        }
    }

    public static List<CandidateData> getPreloadedData(String ballotType) {
        return ballotData.get(ballotType);
    }

    public static boolean isDataPreloaded() {
        return isPreloaded;
    }

    public static void invalidateCache() {
        isPreloaded = false;
        ballotData.clear();
    }
}
