package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import verification.VoterDatabaseLogic;

public class ProvincialBallot extends JFrame {

    private List<String> previousSelection = new ArrayList<>();
    private List<String> candidatesSelected = new ArrayList<>();
    private JPanel candidatesPanel;
    private String voterId;

    // Default constructor for backward compatibility
    public ProvincialBallot(String nationalChoice, String regionalChoice) {
        this(nationalChoice, regionalChoice, null);
    }

    public ProvincialBallot(String nationalChoice, String regionalChoice, String voterId) {
        this.voterId = voterId;
        previousSelection.add(nationalChoice);
        previousSelection.add(regionalChoice);

        setTitle("Provincial Ballot");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(0, 102, 204));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Provincial Elections", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        candidatesPanel = new JPanel();
        candidatesPanel.setLayout(new GridLayout(0, 5, 15, 15));
        candidatesPanel.setBackground(new Color(0, 102, 204));
        candidatesPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(candidatesPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        loadCandidatesFromDatabase();

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(0, 102, 204));

        JButton backBtn = new JButton("<- Back Regional");
        backBtn.setBackground(Color.LIGHT_GRAY);
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        backBtn.addActionListener(e -> {
            new RegionalBallot(previousSelection.get(0), voterId).setVisible(true);
            this.dispose();
        });

        JButton reviewBtn = new JButton("Review & Confirm");
        reviewBtn.setBackground(new Color(255, 204, 0));
        reviewBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        reviewBtn.setForeground(Color.BLACK);
        reviewBtn.addActionListener(e -> {
            if (!candidatesSelected.isEmpty()) {
                new ReviewConfirm(previousSelection.get(0), previousSelection.get(1), candidatesSelected.get(0), voterId).setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Select a candidate first", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(backBtn);
        bottomPanel.add(reviewBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadCandidatesFromDatabase() {
        try {
            ResultSet rs = VoterDatabaseLogic.getCandidates("ProvincialBallot");
            while (rs != null && rs.next()) {
                String partyName = rs.getString("party_name");
                String candidateName = rs.getString("candidate_name");
                byte[] candidateImageBytes = rs.getBytes("candidate_image");
                byte[] partyLogoBytes = rs.getBytes("party_logo");

                ImageIcon candidateIcon = null;
                ImageIcon partyIcon = null;

                if (candidateImageBytes != null && candidateImageBytes.length > 0) {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(candidateImageBytes));
                    candidateIcon = new ImageIcon(img.getScaledInstance(120, 120, Image.SCALE_SMOOTH));
                }

                if (!partyName.equalsIgnoreCase("Independent") && partyLogoBytes != null && partyLogoBytes.length > 0) {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(partyLogoBytes));
                    partyIcon = new ImageIcon(img.getScaledInstance(120, 120, Image.SCALE_SMOOTH));
                }

                JPanel panel = createCandidatePanel(partyName, candidateName, partyIcon, candidateIcon);
                candidatesPanel.add(panel);
            }
        } catch (SQLException | java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private JPanel createCandidatePanel(String partyOrInd, String candidateName, ImageIcon partyIcon, ImageIcon candidateIcon) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0, 102, 204));
        panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        panel.setPreferredSize(new Dimension(200, 200));
        panel.setMaximumSize(new Dimension(200, 200));
        panel.setMinimumSize(new Dimension(200, 200));

        JPanel imagesPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        imagesPanel.setOpaque(false);

        JLabel partyLabel = (partyIcon != null) ? new JLabel(partyIcon) :
                new JLabel("Independent", SwingConstants.CENTER);
        partyLabel.setPreferredSize(new Dimension(100, 120));
        partyLabel.setOpaque(partyIcon == null);
        partyLabel.setBackground(Color.GRAY);
        partyLabel.setForeground(Color.WHITE);
        partyLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel candidateLabel = (candidateIcon != null) ? new JLabel(candidateIcon) :
                new JLabel("No Image", SwingConstants.CENTER);
        candidateLabel.setPreferredSize(new Dimension(100, 120));
        candidateLabel.setForeground(candidateIcon == null ? Color.LIGHT_GRAY : Color.BLACK);

        imagesPanel.add(partyLabel);
        imagesPanel.add(candidateLabel);
        panel.add(imagesPanel, BorderLayout.CENTER);

        JLabel textLabel = new JLabel(partyOrInd + " - " + candidateName, SwingConstants.CENTER);
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(textLabel, BorderLayout.SOUTH);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                candidatesSelected.clear();
                candidatesSelected.add(partyOrInd + " - " + candidateName);
                for (Component comp : candidatesPanel.getComponents()) {
                    comp.setBackground(new Color(0, 102, 204));
                }
                panel.setBackground(new Color(255, 204, 0));
            }
        });

        return panel;
    }
}