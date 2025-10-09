package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ProvincialBallot extends JFrame {

    private List<String> previousSelection = new ArrayList<>(); // national + regional
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
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        candidatesPanel = new JPanel(new GridLayout(5, 4, 10, 10));
        candidatesPanel.setBackground(new Color(0, 102, 204));

        Object[][] candidates = {
            {"ANC", "Candidate1", "images/anc.png"}, {"DA", "Candidate2", "images/da.png"},
            {"EFF", "Candidate3", "images/eff.png"}, {"IFP", "Candidate4", "images/ifp.png"},
            {"FF Plus", "Candidate5", "images/ffplus.png"}, {"Good", "Candidate6", "images/good.png"},
            {"Independent", "Candidate7", "images/ind1.png"}, {"Independent", "Candidate8", "images/ind2.png"},
            {"Independent", "Candidate9", "images/ind3.png"}, {"Independent", "Candidate10", "images/ind4.png"},
            {"Independent", "Candidate11", "images/ind5.png"}, {"Independent", "Candidate12", "images/ind6.png"},
            {"Independent", "Candidate13", "images/ind7.png"}, {"Independent", "Candidate14", "images/ind8.png"},
            {"Independent", "Candidate15", "images/ind9.png"}, {"Independent", "Candidate16", "images/ind10.png"},
            {"Independent", "Candidate17", "images/ind11.png"}, {"Independent", "Candidate18", "images/ind12.png"},
            {"Independent", "Candidate19", "images/ind13.png"}, {"Independent", "Candidate20", "images/ind14.png"}
        };

        for (Object[] c : candidates) {
            JPanel panel = createCandidatePanel(c[0].toString(), c[1].toString(), c[2].toString());
            candidatesPanel.add(panel);
        }

        add(new JScrollPane(candidatesPanel), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(0, 102, 204));

        JButton backBtn = new JButton("<- Back Regional");
        backBtn.setBackground(Color.LIGHT_GRAY);
        backBtn.addActionListener(e -> {
            new RegionalBallot(previousSelection.get(0)).setVisible(true);
            this.dispose();
        });

        JButton reviewBtn = new JButton("Review & Confirm");
        reviewBtn.setBackground(new Color(255, 204, 0));
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

    private JPanel createCandidatePanel(String party, String name, String imagePath) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(0, 102, 204));
        panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        JLabel partyLabel = new JLabel(party);
        partyLabel.setForeground(Color.WHITE);
        partyLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        ImageIcon icon = new ImageIcon(new ImageIcon(imagePath).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
        JLabel imgLabel = new JLabel(icon);

        panel.add(partyLabel);
        panel.add(nameLabel);
        panel.add(imgLabel);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                candidatesSelected.clear();
                candidatesSelected.add(party + " - " + name);
                for (Component comp : candidatesPanel.getComponents()) {
                    comp.setBackground(new Color(0, 102, 204));
                }
                panel.setBackground(new Color(255, 204, 0));
            }
        });

        return panel;
    }
}
