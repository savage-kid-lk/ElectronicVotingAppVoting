package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class RegionalBallot extends JFrame {

    private List<String> previousSelection;
    private List<String> candidatesSelected = new ArrayList<>();
    private JPanel candidatesPanel;
    private String voterId;

    public RegionalBallot(String nationalChoice, String voterId) {
        this.voterId = voterId;
        previousSelection = new ArrayList<>();
        previousSelection.add(nationalChoice);

        ImageIcon icon = new ImageIcon(getClass().getResource("/appLogo.png"));
        setIconImage(icon.getImage());
        setTitle("Regional Ballot");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(0, 102, 204));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Regional Elections", SwingConstants.CENTER);
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

        loadCandidatesFromPreloadedData();

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(0, 102, 204));

        JButton backBtn = new JButton("<- Back National");
        backBtn.setBackground(Color.LIGHT_GRAY);
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        backBtn.addActionListener(e -> {
            new NationalBallot(voterId).setVisible(true);
            this.dispose();
        });

        JButton nextBtn = new JButton("Next → Provincial");
        nextBtn.setBackground(new Color(255, 204, 0));
        nextBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nextBtn.setForeground(Color.BLACK);
        nextBtn.addActionListener(e -> {
            if (!candidatesSelected.isEmpty()) {
                new ProvincialBallot(previousSelection.get(0), candidatesSelected.get(0), voterId).setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Select a candidate first", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(backBtn);
        bottomPanel.add(nextBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadCandidatesFromPreloadedData() {
        List<DataPreloader.CandidateData> candidates = DataPreloader.getPreloadedData("RegionalBallot");
        
        if (candidates == null || candidates.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load candidates.\n\nPlease try again or contact election officials.",
                    "Data Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        for (DataPreloader.CandidateData candidate : candidates) {
            try {
                ImageIcon candidateIcon = null;
                ImageIcon partyIcon = null;

                if (candidate.candidateImage != null) {
                    candidateIcon = new ImageIcon(candidate.candidateImage.getScaledInstance(120, 120, Image.SCALE_SMOOTH));
                }

                if (!candidate.partyName.equalsIgnoreCase("Independent") && candidate.partyLogo != null) {
                    partyIcon = new ImageIcon(candidate.partyLogo.getScaledInstance(120, 120, Image.SCALE_SMOOTH));
                }

                JPanel panel = createCandidatePanel(candidate.partyName, candidate.candidateName, partyIcon, candidateIcon);
                candidatesPanel.add(panel);

            } catch (Exception candidateEx) {
                System.err.println("Error processing candidate: " + candidateEx.getMessage());
            }
        }

        candidatesPanel.revalidate();
        candidatesPanel.repaint();
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