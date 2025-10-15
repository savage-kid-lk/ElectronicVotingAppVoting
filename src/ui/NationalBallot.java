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

public class NationalBallot extends JFrame {

    private List<String> candidatesSelected = new ArrayList<>();
    private JPanel candidatesPanel;

    public NationalBallot() {
        setTitle("National Ballot");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(0, 102, 204));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("National Elections", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        candidatesPanel = new JPanel(new GridLayout(0, 4, 20, 20));
        candidatesPanel.setBackground(new Color(0, 102, 204));
        candidatesPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        loadCandidatesFromDatabase();

        add(new JScrollPane(candidatesPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(0, 102, 204));
        JButton nextBtn = new JButton("Next → Regional");
        nextBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nextBtn.setBackground(new Color(255, 204, 0));
        nextBtn.setForeground(Color.BLACK);

        nextBtn.addActionListener(e -> {
            if (!candidatesSelected.isEmpty()) {
                new RegionalBallot(candidatesSelected.get(0)).setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Select a candidate first", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(nextBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadCandidatesFromDatabase() {
        try {
            ResultSet rs = VoterDatabaseLogic.getCandidates("NationalBallot");
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

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        JLabel topLabel = new JLabel(name, SwingConstants.LEFT);
        topLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topLabel.setForeground(Color.WHITE);
        JLabel bottomLabel = new JLabel(partyOrInd.equalsIgnoreCase("Independent") ? "Independent" : partyOrInd, SwingConstants.LEFT);
        bottomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bottomLabel.setForeground(Color.LIGHT_GRAY);

        textPanel.add(topLabel);
        textPanel.add(bottomLabel);
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
