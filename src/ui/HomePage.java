package ui;

import javax.swing.*;
import java.awt.*;
import com.digitalpersona.uareu.*;
import javax.swing.border.LineBorder;
import verification.Verification;
import verification.VoterDatabaseConnectivity;
import verification.VoterDatabaseLogic;

public class HomePage extends JFrame {

    private static int failedVerificationAttempts = 0;
    private JButton voteButton;

    public HomePage() {
        VoterDatabaseConnectivity.initialize();

        ImageIcon icon = new ImageIcon(getClass().getResource("/appLogo.png"));
        setIconImage(icon.getImage());
        setTitle("Electronic Voting System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(new Color(0, 87, 183));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(0, 87, 183));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridx = 0;

        ImageIcon logo = new ImageIcon(getClass().getResource("/appLogo.png"));
        Image scaledImage = logo.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
        ImageIcon scaledLogo = new ImageIcon(scaledImage);
        JLabel logoLabel = new JLabel(scaledLogo);

        gbc.gridy = 0;
        mainPanel.add(logoLabel, gbc);

        JLabel welcomeLabel = new JLabel(
                "<html><center>Welcome to the<br>Electronic Voting System</center></html>",
                SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        mainPanel.add(welcomeLabel, gbc);

        voteButton = new JButton("VOTE");
        voteButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        voteButton.setBorder(new LineBorder(new Color(255, 209, 0)));
        voteButton.setPreferredSize(new Dimension(200, 55));
        voteButton.setEnabled(true);
        gbc.gridy = 2;
        mainPanel.add(voteButton, gbc);

        voteButton.addActionListener(e -> {
            voteButton.setEnabled(false);
            JDialog scanDialog = createScanDialog();

            new Thread(() -> {

                SwingUtilities.invokeLater(() -> scanDialog.setVisible(true));

                try {
                    ReaderCollection readers = UareUGlobal.GetReaderCollection();
                    readers.GetReaders();

                    if (readers.size() == 0) {
                        SwingUtilities.invokeLater(() -> {
                            scanDialog.dispose();
                            JOptionPane.showMessageDialog(HomePage.this,
                                    "No fingerprint reader detected.\n\nPlease ensure your fingerprint scanner is connected and drivers are installed.",
                                    "Hardware Not Found",
                                    JOptionPane.ERROR_MESSAGE);
                            voteButton.setEnabled(true);
                        });
                        return;
                    }

                    Reader reader = readers.get(0);
                    Verification verification = new Verification(reader);

                    verification.startVerification((verified, voterId, message) -> {

                        SwingUtilities.invokeLater(() -> scanDialog.dispose());

                        if (verified) {
                            failedVerificationAttempts = 0;

                            // Show loading dialog while waiting for ALL data to load
                            JDialog loadingDialog = createLoadingDialog();
                            SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));

                            new Thread(() -> {
                                // FORCE the app to wait until data finishes loading
                                DataPreloader.preloadAllData();

                                SwingUtilities.invokeLater(() -> {
                                    loadingDialog.dispose();
                                    new NationalBallot(voterId).setVisible(true);
                                    dispose();
                                });

                            }).start();

                        } else {
                            SwingUtilities.invokeLater(() -> {
                                voteButton.setEnabled(true);

                                if (message.contains("already voted")) {
                                    JOptionPane.showMessageDialog(HomePage.this,
                                            message + "\n\nDuplicate voting attempts are recorded and may result in legal action.",
                                            "Access Denied - Already Voted",
                                            JOptionPane.WARNING_MESSAGE);
                                } else {
                                    failedVerificationAttempts++;

                                    if (failedVerificationAttempts >= 3) {
                                        VoterDatabaseLogic.recordFraudAttempt("UNKNOWN",
                                                "UNREGISTERED_VOTER", null,
                                                "3 consecutive failed fingerprint verifications. Possible unregistered voter attempt.");
                                        JOptionPane.showMessageDialog(HomePage.this,
                                                "Multiple verification failures detected.\n\n"
                                                        + "This incident has been recorded for security review.\n"
                                                        + "Please contact election officials for assistance.",
                                                "Security Alert",
                                                JOptionPane.ERROR_MESSAGE);
                                        failedVerificationAttempts = 0;
                                    } else {
                                        JOptionPane.showMessageDialog(HomePage.this,
                                                message + "\n\nAttempts: " + failedVerificationAttempts + "/3",
                                                "Verification Failed",
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            });
                        }
                    });

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
        startDataPreloading();
    }

    private void startDataPreloading() {
        new Thread(DataPreloader::preloadAllData).start();
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

        JLabel textLabel = new JLabel(
                "<html><center>Please place your finger<br>on the fingerprint scanner</center></html>",
                SwingConstants.CENTER);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        contentPanel.add(textLabel, BorderLayout.CENTER);
        contentPanel.add(progressBar, BorderLayout.SOUTH);

        scanDialog.add(contentPanel);
        return scanDialog;
    }

    // NEW — Loading dialog that shows while waiting for data
    private JDialog createLoadingDialog() {
        JDialog loadingDialog = new JDialog(this, "Loading Data", true);
        loadingDialog.setLayout(new BorderLayout());
        loadingDialog.setSize(350, 150);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Loading voting data...\nPlease wait.", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);

        panel.add(label, BorderLayout.CENTER);
        panel.add(bar, BorderLayout.SOUTH);

        loadingDialog.add(panel);
        return loadingDialog;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HomePage().setVisible(true));
    }
}
