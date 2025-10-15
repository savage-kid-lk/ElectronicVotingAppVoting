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

    public ProvincialBallot(String nationalChoice, String regionalChoice) {
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

        candidatesPanel = new JPanel(new GridLayout(5, 4, 20, 20));
        candidatesPanel.setBackground(new Color(0, 102, 204));
        candidatesPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        loadCandidatesFromDatabase();

        add(new JScrollPane(candidatesPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(0, 102, 204));

        JButton backBtn = new JButton("<- Back Regional");
        backBtn.setBackground(Color.LIGHT_GRAY);
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        backBtn.addActionListener(e -> {
            new RegionalBallot(previousSelection.get(0)).setVisible(true);
            this.dispose();
        });

        JButton reviewBtn = new JButton("Review & Confirm");
        reviewBtn.setBackground(new Color(255, 204, 0));
        reviewBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        reviewBtn.setForeground(Color.BLACK);
        reviewBtn.addActionListener(e -> {
            if (!candidatesSelected.isEmpty()) {
                new ReviewConfirm(previousSelection.get(0), previousSelection.get(1), candidatesSelected.get(0)).setVisible(true);
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
                byte[] imageBytes = rs.getBytes("image");

                ImageIcon icon = null;
                if (imageBytes != null) {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                    icon = new ImageIcon(img.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
                }

                JPanel panel = createCandidatePanel(partyName, candidateName, icon);
                candidatesPanel.add(panel);
            }
        } catch (SQLException | java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private JPanel createCandidatePanel(String partyOrInd, String name, ImageIcon icon) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(0, 102, 204));
        panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        JLabel imgLabel = (icon != null) ? new JLabel(icon) : new JLabel("No Image");
        if (icon == null) imgLabel.setForeground(Color.LIGHT_GRAY);
        imgLabel.setPreferredSize(new Dimension(80, 80));
        panel.add(imgLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(2,1));
        textPanel.setOpaque(false);
        JLabel nameLabel = new JLabel(name, SwingConstants.LEFT);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        JLabel partyLabel = new JLabel(partyOrInd.equalsIgnoreCase("Independent") ? "Independent" : partyOrInd, SwingConstants.LEFT);
        partyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        partyLabel.setForeground(Color.LIGHT_GRAY);

        textPanel.add(nameLabel);
        textPanel.add(partyLabel);
        panel.add(textPanel, BorderLayout.CENTER);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                candidatesSelected.clear();
                candidatesSelected.add(partyOrInd + " - " + name);
                for (Component comp : candidatesPanel.getComponents()) {
                    comp.setBackground(new Color(0, 102, 204));
                }
                panel.setBackground(new Color(255, 204, 0));
            }
        });

        return panel;
    }
}
