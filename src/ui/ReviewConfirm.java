package ui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ReviewConfirm extends JFrame {

    private String nationalChoice;
    private String regionalChoice;
    private String provincialChoice;

    private Map<String, ImageIcon> candidateImages = new HashMap<>();

    public ReviewConfirm(String nationalChoice, String regionalChoice, String provincialChoice) {
        this.nationalChoice = nationalChoice;
        this.regionalChoice = regionalChoice;
        this.provincialChoice = provincialChoice;

        setTitle("Review & Confirm");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(0, 102, 204)); // IEC blue
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Review Your Vote", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        // Load candidate images (must match image names used in ballots)
        loadCandidateImages();

        // Create review summary
        JPanel summaryPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        summaryPanel.setBackground(new Color(0, 102, 204));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(40, 200, 40, 200));

        summaryPanel.add(createCandidateBlock("National Ballot", nationalChoice));
        summaryPanel.add(createCandidateBlock("Regional Ballot", regionalChoice));
        summaryPanel.add(createCandidateBlock("Provincial Ballot", provincialChoice));

        add(summaryPanel, BorderLayout.CENTER);

        // Bottom navigation buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(0, 102, 204));

        JButton confirmBtn = new JButton("Confirm Vote ✅");
        confirmBtn.setBackground(new Color(255, 204, 0)); // IEC yellow
        confirmBtn.setForeground(Color.BLACK);
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        confirmBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Vote Submitted ✅", "Success", JOptionPane.INFORMATION_MESSAGE);
            new HomePage().setVisible(true);
            this.dispose();
        });

        JButton reselectBtn = new JButton("Reselect Votes 🔁");
        reselectBtn.setBackground(Color.LIGHT_GRAY);
        reselectBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        reselectBtn.addActionListener(e -> {
            new NationalBallot().setVisible(true);
            this.dispose();
        });

        bottomPanel.add(confirmBtn);
        bottomPanel.add(reselectBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createCandidateBlock(String type, String candidateChoice) {
        JPanel block = new JPanel(new BorderLayout());
        block.setBackground(new Color(0, 87, 183));
        block.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        // Candidate name
        JLabel nameLabel = new JLabel("<html><center><b>" + type + "</b><br>" + candidateChoice + "</center></html>", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        nameLabel.setForeground(Color.WHITE);

        // Load image based on candidateChoice
        JLabel imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);

        for (Map.Entry<String, ImageIcon> entry : candidateImages.entrySet()) {
            if (candidateChoice.toLowerCase().contains(entry.getKey().toLowerCase())) {
                imgLabel.setIcon(entry.getValue());
                break;
            }
        }

        block.add(imgLabel, BorderLayout.CENTER);
        block.add(nameLabel, BorderLayout.SOUTH);

        return block;
    }

    /** Load all images used in ballots */
    private void loadCandidateImages() {
        addCandidateImage("anc", "images/anc.png");
        addCandidateImage("da", "images/da.png");
        addCandidateImage("eff", "images/eff.png");
        addCandidateImage("ifp", "images/ifp.png");
        addCandidateImage("ffplus", "images/ffplus.png");
        addCandidateImage("cope", "images/cope.png");
        addCandidateImage("atm", "images/atm.png");
        addCandidateImage("aljamaah", "images/aljamaah.png");
        addCandidateImage("good", "images/good.png");
        addCandidateImage("udm", "images/udm.png");
        addCandidateImage("acdp", "images/acdp.png");
        addCandidateImage("pa", "images/pa.png");
        addCandidateImage("ind1", "images/ind1.png");
        addCandidateImage("ind2", "images/ind2.png");
        addCandidateImage("ind3", "images/ind3.png");
        addCandidateImage("ind4", "images/ind4.png");
        addCandidateImage("ind5", "images/ind5.png");
        addCandidateImage("ind6", "images/ind6.png");
        addCandidateImage("ind7", "images/ind7.png");
    }

    /** Helper to resize and store candidate images */
    public void addCandidateImage(String key, String imagePath) {
        try {
            ImageIcon icon = new ImageIcon(imagePath);
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            candidateImages.put(key, new ImageIcon(img));
        } catch (Exception e) {
            System.out.println("⚠️ Could not load image for " + key + " from " + imagePath);
        }
    }
}
