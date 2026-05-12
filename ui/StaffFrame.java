package ui;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

/**
 * Role: Administrative Staff
 * ───────────────────────────
 * Tab 1 — Dashboard          : summary KPI cards
 * Tab 2 — Dept-wise Report   : placement % per department/section
 * Tab 3 — Company Performance: offers & selections per company/drive
 * Tab 4 — Tier Distribution  : Normal / Dream / Super Dream breakdown
 * Tab 5 — Monthly Trend      : drives, applications, selections per month
 * Tab 6 — Top Placed Students: students placed sorted by package
 */
public class StaffFrame extends JFrame {

    public StaffFrame() {
        setTitle("PRMS — Administrative Staff");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JMenuBar mb = new JMenuBar();
        JMenu file  = new JMenu("File");
        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(e -> { DBConnection.close(); dispose(); new LoginFrame().setVisible(true); });
        file.add(logout);
        mb.add(file);
        setJMenuBar(mb);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📊  Dashboard",           buildDashboard());
        tabs.addTab("🏫  Dept-wise Report",    buildReportTab(DEPT_SQL,    "Department-wise Placement Statistics"));
        tabs.addTab("🏢  Company Performance", buildReportTab(COMPANY_SQL, "Company / Drive Performance"));
        tabs.addTab("⭐  Tier Distribution",   buildReportTab(TIER_SQL,    "Placement Tier Distribution"));
        tabs.addTab("📅  Monthly Trend",       buildReportTab(MONTHLY_SQL, "Monthly Placement Trend"));
        tabs.addTab("🏆  Top Placed Students", buildReportTab(TOP_SQL,     "Top Placed Students (by Package)"));
        add(tabs);
    }



    private static final String DEPT_SQL =
        "SELECT d.section AS Section, d.program AS Program, d.branch AS Branch, " +
        "       COUNT(s.student_id)                                         AS Total_Students, " +
        "       SUM(CASE WHEN s.placement_tier <> 'Unplaced' THEN 1 ELSE 0 END) AS Placed, " +
        "       SUM(CASE WHEN s.placement_tier = 'Unplaced'  THEN 1 ELSE 0 END) AS Unplaced, " +
        "       ROUND(AVG(s.cgpa), 2)                                       AS Avg_CGPA, " +
        "       ROUND(SUM(CASE WHEN s.placement_tier <> 'Unplaced' THEN 1 ELSE 0 END) " +
        "             * 100.0 / NULLIF(COUNT(s.student_id),0), 1)           AS Placement_Pct " +
        "FROM Departments d " +
        "LEFT JOIN Students s ON d.dept_id = s.dept_id " +
        "GROUP BY d.dept_id, d.section, d.program, d.branch " +
        "ORDER BY d.program, d.branch, d.section";

    private static final String COMPANY_SQL =
        "SELECT c.name AS Company, c.tier AS Tier, c.sector AS Sector, " +
        "       d.role_offered AS Role, d.drive_type AS Type, " +
        "       d.package_lpa AS Package_LPA, d.status AS Drive_Status, " +
        "       COUNT(a.application_id)                                      AS Applications, " +
        "       SUM(CASE WHEN a.result = 'Selected' THEN 1 ELSE 0 END)       AS Selected, " +
        "       SUM(CASE WHEN a.result = 'Rejected' THEN 1 ELSE 0 END)       AS Rejected, " +
        "       SUM(CASE WHEN a.result = 'Pending'  THEN 1 ELSE 0 END)       AS Pending " +
        "FROM Companies c " +
        "LEFT JOIN Drives      d ON c.company_id = d.company_id " +
        "LEFT JOIN Applications a ON d.drive_id  = a.drive_id " +
        "GROUP BY c.company_id, c.name, c.tier, c.sector, " +
        "         d.drive_id, d.role_offered, d.drive_type, d.package_lpa, d.status " +
        "ORDER BY d.package_lpa IS NULL, d.package_lpa DESC";

    private static final String TIER_SQL =
        "SELECT s.placement_tier AS Tier, " +
        "       COUNT(*) AS Student_Count, " +
        "       ROUND(AVG(s.cgpa), 2) AS Avg_CGPA, " +
        "       MIN(s.cgpa) AS Min_CGPA, " +
        "       MAX(s.cgpa) AS Max_CGPA " +
        "FROM Students s " +
        "GROUP BY s.placement_tier " +
        "ORDER BY FIELD(s.placement_tier,'Super Dream','Dream','Normal','Unplaced')";

    private static final String MONTHLY_SQL =
        "SELECT DATE_FORMAT(d.drive_date,'%Y-%m') AS Month, " +
        "       COUNT(DISTINCT d.drive_id)                                       AS Drives_Held, " +
        "       COUNT(a.application_id)                                          AS Applications, " +
        "       SUM(CASE WHEN a.result='Selected' THEN 1 ELSE 0 END)             AS Offers_Made, " +
        "       ROUND(SUM(CASE WHEN a.result='Selected' THEN 1 ELSE 0 END)" +
        "             * 100.0 / NULLIF(COUNT(a.application_id),0), 1)            AS Selection_Rate_Pct, " +
        "       ROUND(AVG(CASE WHEN a.result='Selected' THEN d.package_lpa END), 2) AS Avg_Package_LPA " +
        "FROM Drives d " +
        "LEFT JOIN Applications a ON d.drive_id = a.drive_id " +
        "GROUP BY DATE_FORMAT(d.drive_date,'%Y-%m') " +
        "ORDER BY Month";

    private static final String TOP_SQL =
        "SELECT s.name AS Student, dept.section AS Section, s.cgpa AS CGPA, " +
        "       s.placement_tier AS Tier, " +
        "       c.name AS Company, c.tier AS Company_Tier, " +
        "       d.role_offered AS Role, d.package_lpa AS Package_LPA, " +
        "       d.drive_type AS Type, a.offer_letter_date AS Offer_Date " +
        "FROM Students     s " +
        "JOIN Departments  dept ON s.dept_id    = dept.dept_id " +
        "JOIN Applications a    ON s.student_id = a.student_id " +
        "JOIN Drives       d    ON a.drive_id   = d.drive_id " +
        "JOIN Companies    c    ON d.company_id = c.company_id " +
        "WHERE a.result = 'Selected' " +
        "ORDER BY d.package_lpa DESC";

    // =========================================================================
    // TAB 1 — DASHBOARD
    // =========================================================================
    private JPanel buildDashboard() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("Placement Summary Dashboard", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(30, 90, 160));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JPanel grid = new JPanel(new GridLayout(2, 4, 16, 16));

        String[][] cards = {
            { "SELECT COUNT(*) FROM Students",                                            "Total Students",   "#4472C4" },
            { "SELECT COUNT(*) FROM Students WHERE placement_tier <> 'Unplaced'",        "Placed Students",  "#70AD47" },
            { "SELECT COUNT(*) FROM Students WHERE placement_tier = 'Unplaced'",         "Unplaced Students","#FF0000" },
            { "SELECT COUNT(*) FROM Companies",                                           "Companies",        "#ED7D31" },
            { "SELECT COUNT(*) FROM Drives WHERE status='Completed'",                    "Completed Drives", "#5B9BD5" },
            { "SELECT COUNT(*) FROM Drives WHERE status='Upcoming'",                     "Upcoming Drives",  "#FFC000" },
            { "SELECT IFNULL(ROUND(AVG(package_lpa),2),'—') FROM Drives WHERE status='Completed'", "Avg Package (LPA)", "#70AD47" },
            { "SELECT IFNULL(MAX(package_lpa),'—') FROM Drives WHERE status='Completed'","Highest Package (LPA)", "#C00000" },
        };

        for (String[] card : cards) {
            grid.add(makeKpiCard(card[0], card[1], Color.decode(card[2])));
        }

        JButton refreshBtn = new JButton("↺  Refresh Dashboard");
        refreshBtn.addActionListener(e -> {
            grid.removeAll();
            for (String[] card : cards) grid.add(makeKpiCard(card[0], card[1], Color.decode(card[2])));
            grid.revalidate(); grid.repaint();
        });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.add(refreshBtn);

        panel.add(title,     BorderLayout.NORTH);
        panel.add(grid,      BorderLayout.CENTER);
        panel.add(south,     BorderLayout.SOUTH);
        return panel;
    }

    private JPanel makeKpiCard(String sql, String label, Color accent) {
        String val = "—";
        try {
            Statement st = DBConnection.getConnection().createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) val = rs.getString(1) != null ? rs.getString(1) : "0";
            rs.close(); st.close();
        } catch (SQLException ex) { val = "ERR"; }

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220)),
                        BorderFactory.createEmptyBorder(12, 14, 12, 14))));

        JLabel numLbl = new JLabel(val, JLabel.CENTER);
        numLbl.setFont(new Font("Arial", Font.BOLD, 30));
        numLbl.setForeground(accent);

        JLabel lblLbl = new JLabel(label, JLabel.CENTER);
        lblLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        lblLbl.setForeground(new Color(80, 80, 80));

        card.add(numLbl, BorderLayout.CENTER);
        card.add(lblLbl, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================================
    // GENERIC REPORT TAB (used by tabs 2-6)
    // =========================================================================
    private JPanel buildReportTab(String sql, String heading) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel title = UIUtils.sectionTitle(heading);

        JTable  table = new JTable();
        JScrollPane pane = UIUtils.tablePane(table, heading);

        JButton genBtn   = new JButton("▶  Generate Report");
        JButton exportBtn = new JButton("Copy SQL");
        genBtn.setBackground(new Color(30, 90, 160));
        genBtn.setForeground(Color.WHITE);
        genBtn.setFocusPainted(false);

        JLabel rowLbl = new JLabel("  Click 'Generate' to load data.");
        rowLbl.setFont(new Font("Arial", Font.ITALIC, 11));
        rowLbl.setForeground(Color.GRAY);

        genBtn.addActionListener(e -> {
            UIUtils.runQuery(sql, table, pane, heading);
            rowLbl.setText("  " + table.getRowCount() + " rows.");
        });

        exportBtn.addActionListener(e -> {
            java.awt.datatransfer.StringSelection sel =
                    new java.awt.datatransfer.StringSelection(sql);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
            JOptionPane.showMessageDialog(this, "SQL copied to clipboard.",
                    "Copied", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.WEST);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.add(exportBtn);
        btns.add(genBtn);
        top.add(btns, BorderLayout.EAST);

        panel.add(top,    BorderLayout.NORTH);
        panel.add(pane,   BorderLayout.CENTER);
        panel.add(rowLbl, BorderLayout.SOUTH);
        return panel;
    }
}
