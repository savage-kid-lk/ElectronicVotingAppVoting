package ui;

import javax.swing.*;
import java.awt.*;
import com.digitalpersona.uareu.*;
import verification.Verification;

public class HomePage extends JFrame {

    public HomePage() {
        setTitle("Electronic Voting System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(0, 87, 183));
        setResizable(rootPaneCheckingEnabled);

        JLabel welcomeLabel = new JLabel("Welcome to the Electronic Voting App", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);

        JButton voteButton = new JButton("VOTE");
        voteButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        voteButton.setBackground(new Color(255, 209, 0)); // IEC yellow
        voteButton.setForeground(Color.BLACK);
        voteButton.setFocusPainted(false);
        voteButton.setPreferredSize(new Dimension(150, 45));

        voteButton.addActionListener(e -> {
            voteButton.setEnabled(false);

            JLabel scanLabel = new JLabel("Place your finger on the scanner...", SwingConstants.CENTER);
            scanLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            JDialog scanDialog = new JDialog(this, "Fingerprint Scan", false);
            scanDialog.setLayout(new BorderLayout());
            scanDialog.add(scanLabel, BorderLayout.CENTER);
            scanDialog.setSize(350, 150);
            scanDialog.setLocationRelativeTo(this);
            scanDialog.setVisible(true);

            try {
                ReaderCollection readers = UareUGlobal.GetReaderCollection();
                readers.GetReaders();
                if (readers.size() > 0) {
                    Reader reader = readers.get(0);
                    Verification verification = new Verification(reader);

                    verification.startVerification(verified -> {
                        SwingUtilities.invokeLater(() -> {
                            voteButton.setEnabled(true);
                            scanDialog.dispose();

                            if (verified) {
                                new NationalBallot().setVisible(true);
                                this.dispose();
                            } else {
                                JOptionPane.showMessageDialog(this, "Verification Failed", "Access Denied", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    });
                } else {
                    scanDialog.dispose();
                    JOptionPane.showMessageDialog(this, "No fingerprint reader found.", "Error", JOptionPane.ERROR_MESSAGE);
                    voteButton.setEnabled(true);
                }
            } catch (UareUException ex) {
                scanDialog.dispose();
                JOptionPane.showMessageDialog(this, "Error accessing fingerprint reader: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                voteButton.setEnabled(true);
            }
        });

        setLayout(new BorderLayout());
        add(welcomeLabel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(0, 87, 183));
        bottomPanel.add(voteButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HomePage().setVisible(true));
    }
}
