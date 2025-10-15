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

    public ReviewConfirm(String nationalChoice, String regionalChoice, String provincialChoice) {
        this.nationalChoice = nationalChoice;
        this.regionalChoice = regionalChoice;
        this.provincialChoice = provincialChoice;

        setTitle("Review & Confirm");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(0, 102, 204));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Review Your Vote", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        JPanel summaryPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        summaryPanel.setBackground(new Color(0, 102, 204));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(40, 200, 40, 200));

        summaryPanel.add(createCandidateBlock("National Ballot", nationalChoice, "NationalBallot"));
        summaryPanel.add(createCandidateBlock("Regional Ballot", regionalChoice, "RegionalBallot"));
        summaryPanel.add(createCandidateBlock("Provincial Ballot", provincialChoice, "ProvincialBallot"));

        add(summaryPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(0, 102, 204));

        JButton confirmBtn = new JButton("Confirm Vote ✅");
        confirmBtn.setBackground(new Color(255, 204, 0));
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        confirmBtn.addActionListener(e -> {
            insertVote("National", nationalChoice);
            insertVote("Regional", regionalChoice);
            insertVote("Provincial", provincialChoice);

            JOptionPane.showMessageDialog(this, "✅ Your vote has been submitted successfully!");
            new HomePage().setVisible(true);
            this.dispose();
        });

        JButton reselectBtn = new JButton("Reselect Votes 🔁");
        reselectBtn.setBackground(Color.LIGHT_GRAY);
        reselectBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        reselectBtn.addActionListener(e -> {
            new ui.NationalBallot().setVisible(true);
            this.dispose();
        });

        bottomPanel.add(confirmBtn);
        bottomPanel.add(reselectBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Converts "Party - Candidate" string to just party name for database insertion.
     */
    private void insertVote(String ballotType, String choice) {
        String[] parts = choice.split(" - ", 2);
        if (parts.length >= 1) {
            String party = parts[0]; // Only party name is stored
            VoterDatabaseLogic.insertVote(ballotType, party);
        }
    }

    private JPanel createCandidateBlock(String type, String candidateChoice, String tableName) {
        JPanel block = new JPanel(new BorderLayout(10, 10));
        block.setBackground(new Color(0, 87, 183));
        block.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        JLabel imgLabel = new JLabel("No Image");
        imgLabel.setPreferredSize(new Dimension(80, 80));
        imgLabel.setForeground(Color.LIGHT_GRAY);

        // Fetch candidate image from database
        try {
            ResultSet rs = VoterDatabaseLogic.getCandidates(tableName);
            while (rs != null && rs.next()) {
                String partyName = rs.getString("party_name");
                String candidateName = rs.getString("candidate_name");
                byte[] imageBytes = rs.getBytes("image");

                String fullName = partyName + " - " + candidateName;
                if (fullName.equals(candidateChoice) && imageBytes != null) {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                    imgLabel.setIcon(new ImageIcon(img.getScaledInstance(80, 80, Image.SCALE_SMOOTH)));
                    imgLabel.setText(null);
                    break;
                }
            }
        } catch (SQLException | java.io.IOException e) {
            e.printStackTrace();
        }

        JLabel nameLabel = new JLabel("<html><center><b>" + type + "</b><br>" + candidateChoice + "</center></html>", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        nameLabel.setForeground(Color.WHITE);

        block.add(imgLabel, BorderLayout.WEST);
        block.add(nameLabel, BorderLayout.CENTER);

        return block;
    }
}
