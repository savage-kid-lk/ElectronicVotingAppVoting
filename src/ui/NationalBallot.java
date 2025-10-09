package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

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
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        candidatesPanel = new JPanel(new GridLayout(5, 4, 10, 10));
        candidatesPanel.setBackground(new Color(0, 102, 204));

        // Full candidate list
        Object[][] candidates = {
            {"ANC", "Cyril Ramaphosa", "images/anc.png"},
            {"DA", "John Steenhuisen", "images/da.png"},
            {"EFF", "Julius Malema", "images/eff.png"},
            {"IFP", "Velenkosini Hlabisa", "images/ifp.png"},
            {"FF Plus", "Pieter Groenewald", "images/ffplus.png"},
            {"Cope", "Mosiuoa Lekota", "images/cope.png"},
            {"ATM", "Themba Godi", "images/atm.png"},
            {"Al Jama-ah", "Ganief Hendricks", "images/aljamaah.png"},
            {"Good", "Patricia de Lille", "images/good.png"},
            {"UDM", "Bantu Holomisa", "images/udm.png"},
            {"ACDP", "Kenneth Meshoe", "images/acdp.png"},
            {"PA", "Narius Moloto", "images/pa.png"},
            {"COPE", "Mvuzo Dlamini", "images/cope2.png"},
            {"Independent", "Thabo Mokoena", "images/ind1.png"},
            {"Independent", "Sipho Dlamini", "images/ind2.png"},
            {"Independent", "Nomsa Khumalo", "images/ind3.png"},
            {"Independent", "Pieter van der Merwe", "images/ind4.png"},
            {"Independent", "Lerato Mthembu", "images/ind5.png"},
            {"Independent", "Themba Nkosi", "images/ind6.png"},
            {"Independent", "Zanele Ngcobo", "images/ind7.png"}
        };

        for (Object[] c : candidates) {
            JPanel panel = createCandidatePanel(c[0].toString(), c[1].toString(), c[2].toString());
            candidatesPanel.add(panel);
        }

        add(new JScrollPane(candidatesPanel), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(0, 102, 204));
        JButton nextBtn = new JButton("Next -> Regional");
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
                // Highlight selected
                for (Component comp : candidatesPanel.getComponents()) {
                    comp.setBackground(new Color(0, 102, 204));
                }
                panel.setBackground(new Color(255, 204, 0));
            }
        });

        return panel;
    }
}
