package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class RegionalBallot extends JFrame {

    private List<String> previousSelection; // carry over national choice
    private List<String> candidatesSelected = new ArrayList<>();
    private JPanel candidatesPanel;

    public RegionalBallot(String nationalChoice) {
        previousSelection = new ArrayList<>();
        previousSelection.add(nationalChoice);

        setTitle("Regional Ballot");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(0, 102, 204));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Regional Elections", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        candidatesPanel = new JPanel(new GridLayout(5, 4, 10, 10));
        candidatesPanel.setBackground(new Color(0, 102, 204));

        Object[][] candidates = {
            {"ANC", "Jacob Zuma", "images/anc.png"},
            {"DA", "Mmusi Maimane", "images/da.png"},
            {"EFF", "Mandla Mandela", "images/eff.png"},
            {"IFP", "Mangosuthu Buthelezi", "images/ifp.png"},
            {"FF Plus", "Pieter Groenewald", "images/ffplus.png"},
            {"Good", "Patricia de Lille", "images/good.png"},
            {"Independent", "Thabo Mokoena", "images/ind1.png"},
            {"Independent", "Sipho Dlamini", "images/ind2.png"},
            {"Independent", "Nomsa Khumalo", "images/ind3.png"},
            {"Independent", "Pieter van der Merwe", "images/ind4.png"},
            {"Independent", "Lerato Mthembu", "images/ind5.png"},
            {"Independent", "Themba Nkosi", "images/ind6.png"},
            {"Independent", "Zanele Ngcobo", "images/ind7.png"},
            {"Independent", "John Doe", "images/ind8.png"},
            {"Independent", "Jane Smith", "images/ind9.png"},
            {"Independent", "Peter Brown", "images/ind10.png"},
            {"Independent", "Mary Green", "images/ind11.png"},
            {"Independent", "Tom White", "images/ind12.png"},
            {"Independent", "Sarah Black", "images/ind13.png"},
            {"Independent", "Sipho Nkosi", "images/ind14.png"}
        };

        for (Object[] c : candidates) {
            JPanel panel = createCandidatePanel(c[0].toString(), c[1].toString(), c[2].toString());
            candidatesPanel.add(panel);
        }

        add(new JScrollPane(candidatesPanel), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(0, 102, 204));

        JButton backBtn = new JButton("<- Back National");
        backBtn.setBackground(Color.LIGHT_GRAY);
        backBtn.addActionListener(e -> {
            new NationalBallot().setVisible(true);
            this.dispose();
        });

        JButton nextBtn = new JButton("Next -> Provincial");
        nextBtn.setBackground(new Color(255, 204, 0));
        nextBtn.setForeground(Color.BLACK);
        nextBtn.addActionListener(e -> {
            if (!candidatesSelected.isEmpty()) {
                new ProvincialBallot(previousSelection.get(0), candidatesSelected.get(0)).setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Select a candidate first", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(backBtn);
        bottomPanel.add(nextBtn);
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
