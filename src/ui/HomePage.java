package ui;

import javax.swing.*;
import java.awt.*;
import com.digitalpersona.uareu.*;
import verification.Verification;
import ui.NationalBallot;

public class HomePage extends JFrame {

    public HomePage() {
        setTitle("Electronic Voting System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(0, 87, 183));

        // ---------- MAIN CONTAINER ----------
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(0, 87, 183));
        mainPanel.setLayout(new GridBagLayout()); // centers everything
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        // ---------- WELCOME LABEL ----------
        JLabel welcomeLabel = new JLabel("<html><center>Welcome to the<br>Electronic Voting System</center></html>", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);
        gbc.gridy = 0;
        mainPanel.add(welcomeLabel, gbc);

        // ---------- VOTE BUTTON ----------
        JButton voteButton = new JButton("VOTE");
        voteButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        voteButton.setBackground(new Color(255, 209, 0)); // IEC yellow
        voteButton.setForeground(Color.BLACK);
        voteButton.setFocusPainted(false);
        voteButton.setPreferredSize(new Dimension(200, 55));
        voteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        gbc.gridy = 1;
        mainPanel.add(voteButton, gbc);

        // ---------- BUTTON ACTION ----------
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

                    // ✅ Use the callback directly
                    verification.startVerification(verified -> {
                        SwingUtilities.invokeLater(() -> {
                            scanDialog.dispose();
                            voteButton.setEnabled(true);

                            if (verified) {
                                new NationalBallot().setVisible(true);
                                this.dispose();
                            } else {
                                JOptionPane.showMessageDialog(this,
                                        "Verification Failed",
                                        "Access Denied",
                                        JOptionPane.ERROR_MESSAGE);
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

        // ---------- ADD MAIN PANEL ----------
        add(mainPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HomePage().setVisible(true));
    }
}
