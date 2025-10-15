package ui;

import javax.swing.*;
import java.awt.*;
import com.digitalpersona.uareu.*;
import verification.Verification;
import verification.Database;

public class HomePage extends JFrame {

    public HomePage() {
        // Initialize DB connection once
        Database.initialize();

        setTitle("Electronic Voting System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(0, 87, 183));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(0, 87, 183));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JLabel welcomeLabel = new JLabel("<html><center>Welcome to the<br>Electronic Voting System</center></html>", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);
        mainPanel.add(welcomeLabel, gbc);

        JButton voteButton = new JButton("VOTE");
        voteButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        voteButton.setBackground(new Color(255, 209, 0));
        voteButton.setPreferredSize(new Dimension(200, 55));

        gbc.gridy = 1;
        mainPanel.add(voteButton, gbc);

        voteButton.addActionListener(e -> {
            voteButton.setEnabled(false);
            JDialog scanDialog = new JDialog(this, "Fingerprint Scan", false);
            scanDialog.setLayout(new BorderLayout());
            scanDialog.add(new JLabel("Place your finger on the scanner...", SwingConstants.CENTER), BorderLayout.CENTER);
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
                            scanDialog.dispose();
                            voteButton.setEnabled(true);
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

        add(mainPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HomePage().setVisible(true));
    }
}
