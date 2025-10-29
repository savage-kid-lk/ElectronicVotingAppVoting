package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import verification.VoterDatabaseLogic;
import javax.imageio.ImageIO;

public class ReviewConfirm extends JFrame {

    private String nationalChoice, regionalChoice, provincialChoice;
    private String voterId;

    public ReviewConfirm(String nationalChoice, String regionalChoice, String provincialChoice, String voterId) {
        this.nationalChoice = nationalChoice;
        this.regionalChoice = regionalChoice;
        this.provincialChoice = provincialChoice;
        this.voterId = voterId;

        setTitle("Review & Confirm Your Vote");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(0, 87, 183));
        setLayout(new BorderLayout(20, 20));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevent accidental closure

        // Header with gradient background
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);

        // Footer with action buttons
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 87, 183));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel title = new JLabel("Review Your Ballot Selections", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Please verify your choices before confirming", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(200, 220, 255));

        header.add(title, BorderLayout.CENTER);
        header.add(subtitle, BorderLayout.SOUTH);

        return header;
    }

    private JPanel createContentPanel() {
        JPanel content = new JPanel(new GridLayout(1, 3, 20, 20));
        content.setBackground(new Color(0, 87, 183));
        content.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        content.add(createBallotCard("National Ballot", nationalChoice, "NationalBallot", new Color(41, 128, 185)));
        content.add(createBallotCard("Regional Ballot", regionalChoice, "RegionalBallot", new Color(39, 174, 96)));
        content.add(createBallotCard("Provincial Ballot", provincialChoice, "ProvincialBallot", new Color(142, 68, 173)));

        return content;
    }

    private JPanel createBallotCard(String ballotType, String candidateChoice, String tableName, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Header
        JLabel typeLabel = new JLabel(ballotType, SwingConstants.CENTER);
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        typeLabel.setForeground(accentColor);
        card.add(typeLabel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(Color.WHITE);

        // Images
        JPanel imagePanel = createImagePanel(candidateChoice, tableName);
        contentPanel.add(imagePanel, BorderLayout.CENTER);

        // Candidate info
        JTextArea infoArea = new JTextArea(candidateChoice);
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoArea.setBackground(Color.WHITE);
        infoArea.setForeground(Color.DARK_GRAY);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setEditable(false);
        infoArea.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        contentPanel.add(infoArea, BorderLayout.SOUTH);
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createImagePanel(String candidateChoice, String tableName) {
        JPanel imagePanel = new JPanel(new GridLayout(1, 2, 10, 10));
        imagePanel.setBackground(Color.WHITE);

        JLabel partyLogoLabel = createImageLabel("Party Logo");
        JLabel candidateImageLabel = createImageLabel("Candidate Photo");

        // Fetch and set images
        try {
            ResultSet rs = VoterDatabaseLogic.getCandidates(tableName);
            while (rs != null && rs.next()) {
                String partyName = rs.getString("party_name");
                String candidateName = rs.getString("candidate_name");
                byte[] candidateImageBytes = rs.getBytes("candidate_image");
                byte[] partyLogoBytes = rs.getBytes("party_logo");

                String fullName = partyName + " - " + candidateName;
                if (fullName.equals(candidateChoice)) {
                    if (candidateImageBytes != null) {
                        setImageOnLabel(candidateImageLabel, candidateImageBytes, 150, 150);
                    }
                    if (partyLogoBytes != null) {
                        setImageOnLabel(partyLogoLabel, partyLogoBytes, 150, 150);
                    }
                    break;
                }
            }
        } catch (SQLException | java.io.IOException e) {
            System.err.println("Error loading images: " + e.getMessage());
        }

        imagePanel.add(partyLogoLabel);
        imagePanel.add(candidateImageLabel);

        return imagePanel;
    }

    private JLabel createImageLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(Color.GRAY);
        label.setOpaque(true);
        label.setBackground(new Color(245, 245, 245));
        label.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        label.setPreferredSize(new Dimension(150, 150));
        return label;
    }

    private void setImageOnLabel(JLabel label, byte[] imageBytes, int width, int height) throws java.io.IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
        ImageIcon icon = new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        label.setIcon(icon);
        label.setText(null);
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        footer.setBackground(new Color(0, 87, 183));
        footer.setBorder(BorderFactory.createEmptyBorder(20, 0, 40, 0));

        JButton confirmBtn = createStyledButton("CONFIRM VOTE ✅", new Color(46, 204, 113), new Color(39, 174, 96));
        confirmBtn.addActionListener(e -> confirmVote());

        JButton reselectBtn = createStyledButton("RESELECT VOTES 🔁", new Color(241, 196, 15), new Color(243, 156, 18));
        reselectBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to reselect your votes?\nAll current selections will be lost.",
                    "Confirm Reselection",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                new NationalBallot(voterId).setVisible(true);
                dispose();
            }
        });

        footer.add(reselectBtn);
        footer.add(confirmBtn);

        return footer;
    }

    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private void confirmVote() {
        int confirmation = JOptionPane.showConfirmDialog(this,
                "<html><center><b>Final Confirmation</b><br><br>"
                + "You are about to submit your vote. This action cannot be undone.<br><br>"
                + "National: " + nationalChoice + "<br>"
                + "Regional: " + regionalChoice + "<br>"
                + "Provincial: " + provincialChoice + "<br><br>"
                + "Are you sure you want to submit your vote?</center></html>",
                "Confirm Your Vote",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            // Extract party names from choices
            String nationalParty = extractPartyName(nationalChoice);
            String regionalParty = extractPartyName(regionalChoice);
            String provincialParty = extractPartyName(provincialChoice);

            // Insert ALL THREE votes at once in a single transaction
            boolean allVotesSuccessful = VoterDatabaseLogic.insertAllVotes(
                    nationalParty, regionalParty, provincialParty, voterId);

            if (allVotesSuccessful) {
                JOptionPane.showMessageDialog(this,
                        "<html><center><b>✅ Vote Submitted Successfully!</b><br><br>"
                        + "Thank you for exercising your democratic right.<br>"
                        + "Your votes have been securely recorded for all three ballots.</center></html>",
                        "Vote Confirmed",
                        JOptionPane.INFORMATION_MESSAGE);

                new HomePage().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "<html><center><b>❌ Vote Submission Failed</b><br><br>"
                        + "There was an issue recording your votes.<br>"
                        + "This may be because you have already voted.<br>"
                        + "Please contact election officials if this is an error.</center></html>",
                        "Submission Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String extractPartyName(String choice) {
        String[] parts = choice.split(" - ", 2);
        return parts.length >= 1 ? parts[0] : choice;
    }

    private boolean insertVote(String category, String choice) {
        String[] parts = choice.split(" - ", 2);
        if (parts.length >= 1) {
            String partyName = parts[0];
            boolean success = VoterDatabaseLogic.insertVote(category, partyName, voterId);

            if (!success) {
                System.err.println("❌ Failed to insert " + category + " vote for " + voterId + " - Choice: " + choice);
                // Check if it's because the voter has already voted
                if (VoterDatabaseLogic.hasVoterVoted(voterId)) {
                    System.err.println("🚨 Voter " + voterId + " has already voted - blocking duplicate vote");
                    // Record the fraud attempt
                    VoterDatabaseLogic.recordFraudAttempt(voterId, "DUPLICATE_VOTE", null,
                            "Attempted to vote in " + category + " election after already completing voting process");
                }
            } else {
                System.out.println("✅ Successfully inserted " + category + " vote for " + voterId);
            }
            return success;
        }
        System.err.println("❌ Invalid choice format: " + choice);
        return false;
    }
}
