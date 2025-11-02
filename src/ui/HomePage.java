package ui;

import javax.swing.*;
import java.awt.*;
import com.digitalpersona.uareu.*;
import verification.Verification;
import verification.VoterDatabaseConnectivity;
import verification.VoterDatabaseLogic;

public class HomePage extends JFrame {

    private static int failedVerificationAttempts = 0;

    public HomePage() {
        VoterDatabaseConnectivity.initialize();

        setTitle("Electronic Voting System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(0, 87, 183));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(0, 87, 183));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridx = 0;

// === LOGO AT TOP ===
        ImageIcon logo = new ImageIcon(getClass().getResource("/IEC LOGO.png"));
        Image scaledImage = logo.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
        ImageIcon scaledLogo = new ImageIcon(scaledImage);
        JLabel logoLabel = new JLabel(scaledLogo);

        gbc.gridy = 0;
        mainPanel.add(logoLabel, gbc);

// === WELCOME TEXT ===
        gbc.gridy = 1;
        JLabel welcomeLabel = new JLabel(
                "<html><center>Welcome to the<br>Electronic Voting System</center></html>",
                SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);
        mainPanel.add(welcomeLabel, gbc);

// === VOTE BUTTON ===
        gbc.gridy = 2;
        JButton voteButton = new JButton("VOTE");
        voteButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        voteButton.setBackground(new Color(255, 209, 0));
        voteButton.setPreferredSize(new Dimension(200, 55));
        mainPanel.add(voteButton, gbc);

        voteButton.addActionListener(e -> {
            voteButton.setEnabled(false);
            JDialog scanDialog = createScanDialog();

            // Start verification in a separate thread to prevent UI blocking
            new Thread(() -> {
                SwingUtilities.invokeLater(() -> {
                    scanDialog.setVisible(true);
                });

                try {
                    ReaderCollection readers = UareUGlobal.GetReaderCollection();
                    readers.GetReaders();
                    if (readers.size() > 0) {
                        Reader reader = readers.get(0);
                        Verification verification = new Verification(reader);

                        verification.startVerification((verified, voterId, message) -> {
                            SwingUtilities.invokeLater(() -> {
                                scanDialog.dispose();
                                voteButton.setEnabled(true);

                                if (verified) {
                                    failedVerificationAttempts = 0; // Reset counter on success
                                    new NationalBallot(voterId).setVisible(true); // ✅ Pass voterId
                                    dispose();
                                } else {
                                    if (message.contains("already voted")) {
                                        JOptionPane.showMessageDialog(HomePage.this,
                                                message + "\n\nDuplicate voting attempts are recorded and may result in legal action.",
                                                "Access Denied - Already Voted",
                                                JOptionPane.WARNING_MESSAGE);
                                    } else {
                                        failedVerificationAttempts++;

                                        if (failedVerificationAttempts >= 3) {
                                            // Record unregistered voter attempt after 3 failures
                                            VoterDatabaseLogic.recordFraudAttempt("UNKNOWN", "UNREGISTERED_VOTER", null,
                                                    "3 consecutive failed fingerprint verifications. Possible unregistered voter attempt.");
                                            JOptionPane.showMessageDialog(HomePage.this,
                                                    "Multiple verification failures detected.\n\n"
                                                    + "This incident has been recorded for security review.\n"
                                                    + "Please contact election officials for assistance.",
                                                    "Security Alert",
                                                    JOptionPane.ERROR_MESSAGE);
                                            failedVerificationAttempts = 0; // Reset after recording
                                        } else {
                                            JOptionPane.showMessageDialog(HomePage.this,
                                                    message + "\n\nAttempts: " + failedVerificationAttempts + "/3",
                                                    "Verification Failed",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                    }
                                }
                            });
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            scanDialog.dispose();
                            JOptionPane.showMessageDialog(HomePage.this,
                                    "No fingerprint reader detected.\n\nPlease ensure your fingerprint scanner is connected and drivers are installed.",
                                    "Hardware Not Found",
                                    JOptionPane.ERROR_MESSAGE);
                            voteButton.setEnabled(true);
                        });
                    }
                } catch (UareUException ex) {
                    SwingUtilities.invokeLater(() -> {
                        scanDialog.dispose();
                        JOptionPane.showMessageDialog(HomePage.this,
                                "Error accessing fingerprint reader: " + ex.getMessage()
                                + "\n\nPlease check device connection and try again.",
                                "Hardware Error",
                                JOptionPane.ERROR_MESSAGE);
                        voteButton.setEnabled(true);
                    });
                }
            }).start();
        });

        add(mainPanel);
    }

    private JDialog createScanDialog() {
        JDialog scanDialog = new JDialog(this, "Fingerprint Verification", true);
        scanDialog.setLayout(new BorderLayout());
        scanDialog.setSize(400, 200);
        scanDialog.setLocationRelativeTo(this);
        scanDialog.setResizable(false);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);

        JLabel iconLabel = new JLabel("🔍", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 36));

        JLabel textLabel = new JLabel("<html><center>Please place your finger<br>on the fingerprint scanner</center></html>", SwingConstants.CENTER);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        contentPanel.add(iconLabel, BorderLayout.NORTH);
        contentPanel.add(textLabel, BorderLayout.CENTER);
        contentPanel.add(progressBar, BorderLayout.SOUTH);

        scanDialog.add(contentPanel);
        return scanDialog;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HomePage().setVisible(true);
        });
    }
}
