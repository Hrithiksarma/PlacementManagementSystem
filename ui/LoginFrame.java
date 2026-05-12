package ui;

import db.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Login screen — collects MySQL credentials and role, then opens the
 * appropriate role-specific frame.
 */
public class LoginFrame extends JFrame {

    private final JTextField hostField = new JTextField("localhost", 14);
    private final JTextField portField = new JTextField("3306", 6);
    private final JTextField userField = new JTextField("root", 14);
    private final JPasswordField passField = new JPasswordField(14);
    private final JComboBox<String> roleBox = new JComboBox<>(new String[] {
            "Database Administrator",
            "Administrative Staff",
            "Placement Officer"
    });

    public LoginFrame() {
        setTitle("PRMS — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // ── Title banner ─────────────────────────────────────────────────────
        JLabel banner = new JLabel(
                "<html><center>Placement Records<br>Management System</center></html>",
                JLabel.CENTER);
        banner.setFont(new Font("Arial", Font.BOLD, 18));
        banner.setForeground(new Color(30, 90, 160));
        banner.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        JLabel sub = new JLabel("CS621 — Database Systems  |  IIIT Guwahati", JLabel.CENTER);
        sub.setFont(new Font("Arial", Font.PLAIN, 11));
        sub.setForeground(Color.GRAY);
        sub.setBorder(BorderFactory.createEmptyBorder(0, 10, 18, 10));

        // ── Form ─────────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("MySQL Connection"),
                BorderFactory.createEmptyBorder(6, 12, 10, 12)));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.EAST;
        lc.insets = new Insets(5, 4, 5, 6);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1;
        fc.insets = new Insets(5, 0, 5, 4);

        addRow(form, lc, fc, 0, "Host:", hostField);
        addRow(form, lc, fc, 1, "Port:", portField);
        addRow(form, lc, fc, 2, "Username:", userField);
        addRow(form, lc, fc, 3, "Password:", passField);
        addRow(form, lc, fc, 4, "Role:", roleBox);

        // ── Connect button ────────────────────────────────────────────────────
        JButton connectBtn = new JButton("Connect  →");
        connectBtn.setFont(new Font("Arial", Font.BOLD, 13));
        connectBtn.setBackground(new Color(0, 60, 120)); // Darker, bold blue
        connectBtn.setForeground(Color.WHITE);
        connectBtn.setFocusPainted(false);
        connectBtn.setOpaque(true);
        connectBtn.setBorderPainted(false);
        connectBtn.addActionListener(e -> attemptLogin());
        getRootPane().setDefaultButton(connectBtn);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        btnRow.add(connectBtn);

        // ── Assemble ─────────────────────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(0, 18, 12, 18));
        root.add(banner, BorderLayout.NORTH);

        JPanel mid = new JPanel(new BorderLayout());
        mid.add(sub, BorderLayout.NORTH);
        mid.add(form, BorderLayout.CENTER);
        mid.add(btnRow, BorderLayout.SOUTH);
        root.add(mid, BorderLayout.CENTER);

        add(root);
        pack();
        setLocationRelativeTo(null);
    }

    private void addRow(JPanel p, GridBagConstraints lc, GridBagConstraints fc,
            int row, String label, JComponent field) {
        lc.gridy = fc.gridy = row;
        lc.gridx = 0;
        p.add(new JLabel(label), lc);
        fc.gridx = 1;
        p.add(field, fc);
    }

    private void attemptLogin() {
        String host = hostField.getText().trim();
        String port = portField.getText().trim();
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        String role = (String) roleBox.getSelectedItem();

        if (host.isEmpty() || port.isEmpty() || user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Host, port and username are required.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            DBConnection.connect(host, port, user, pass);
            dispose();
            switch (role) {
                case "Database Administrator":
                    new AdminFrame().setVisible(true);
                    break;
                case "Administrative Staff":
                    new StaffFrame().setVisible(true);
                    break;
                default:
                    new OfficerFrame().setVisible(true);
                    break;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Connection failed:\n" + ex.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
