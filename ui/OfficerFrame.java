package ui;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;


public class OfficerFrame extends JFrame {

    public OfficerFrame() {
        setTitle("PRMS — Placement Officer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(e -> {
            DBConnection.close();
            dispose();
            new LoginFrame().setVisible(true);
        });
        file.add(logout);
        mb.add(file);
        setJMenuBar(mb);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("👤  Students", buildStudentsTab());
        tabs.addTab("🏢  Companies", buildCompaniesTab());
        tabs.addTab("📅  Drives", buildDrivesTab());
        tabs.addTab("📝  Applications", buildApplicationsTab());
        tabs.addTab("🔄  Rounds", buildRoundsTab());
        tabs.addTab("✅  Eligibility", buildEligibilityTab());
        add(tabs);
    }

    // =========================================================================
    // HELPER — standard tab layout: toolbar + scrollable table
    // =========================================================================
    private static final String STUDENTS_VIEW = "SELECT s.student_id AS ID, s.name AS Name, " +
            "       dept.section AS Section, dept.program AS Program, " +
            "       s.batch_year AS Batch, s.cgpa AS CGPA, " +
            "       s.active_backlogs AS Backlogs, s.placement_tier AS Tier, " +
            "       s.email AS Email, s.phone AS Phone " +
            "FROM Students s JOIN Departments dept ON s.dept_id = dept.dept_id " +
            "ORDER BY s.student_id";

    private static final String COMPANIES_VIEW = "SELECT company_id AS ID, name AS Company, sector AS Sector, " +
            "       tier AS Tier, website AS Website " +
            "FROM Companies ORDER BY company_id";

    private static final String DRIVES_VIEW = "SELECT d.drive_id AS ID, c.name AS Company, d.drive_date AS Date, " +
            "       d.role_offered AS Role, d.package_lpa AS Package_LPA, " +
            "       d.drive_type AS Type, d.status AS Status, " +
            "       h.name AS HR_Contact " +
            "FROM Drives d " +
            "JOIN Companies c ON d.company_id = c.company_id " +
            "LEFT JOIN HR_Contacts h ON d.hr_id = h.hr_id " +
            "ORDER BY d.drive_date DESC";

    private static final String APPS_VIEW = "SELECT a.application_id AS ID, s.name AS Student, " +
            "       c.name AS Company, d.role_offered AS Role, " +
            "       d.package_lpa AS Package_LPA, " +
            "       a.apply_date AS Apply_Date, a.result AS Result, " +
            "       a.offer_letter_date AS Offer_Date " +
            "FROM Applications a " +
            "JOIN Students  s ON a.student_id = s.student_id " +
            "JOIN Drives    d ON a.drive_id   = d.drive_id " +
            "JOIN Companies c ON d.company_id = c.company_id " +
            "ORDER BY a.application_id DESC";

    // =========================================================================
    // TAB 1 — STUDENTS
    // =========================================================================
    private JPanel buildStudentsTab() {
        JTable table = new JTable();
        JScrollPane pane = UIUtils.tablePane(table, "Students");

        // search bar
        JTextField search = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        JButton clearBtn = new JButton("Clear");
        JButton addBtn = new JButton("+ Add Student");
        JButton editBtn = new JButton("✎ Update");
        JButton refreshBtn = new JButton("↺ Refresh");

        searchBtn.addActionListener(e -> {
            String q = search.getText().trim().replace("'", "''");
            UIUtils.runQuery(
                    "SELECT s.student_id AS ID, s.name AS Name, dept.section AS Section, " +
                            "dept.program AS Program, s.batch_year AS Batch, s.cgpa AS CGPA, " +
                            "s.active_backlogs AS Backlogs, s.placement_tier AS Tier, " +
                            "s.email AS Email, s.phone AS Phone " +
                            "FROM Students s JOIN Departments dept ON s.dept_id = dept.dept_id " +
                            "WHERE s.name LIKE '%" + q + "%' OR s.email LIKE '%" + q + "%' " +
                            "ORDER BY s.student_id",
                    table, pane, "Search: " + q);
        });
        clearBtn.addActionListener(e -> {
            search.setText("");
            UIUtils.runQuery(STUDENTS_VIEW, table, pane, "Students");
        });
        refreshBtn.addActionListener(e -> UIUtils.runQuery(STUDENTS_VIEW, table, pane, "Students"));

        addBtn.addActionListener(e -> addStudent(table, pane));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a row first.");
                return;
            }
            int id = (Integer) table.getValueAt(row, 0);
            updateStudent(id, table, pane);
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        toolbar.add(new JLabel("Search:"));
        toolbar.add(search);
        toolbar.add(searchBtn);
        toolbar.add(clearBtn);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(refreshBtn);

        UIUtils.runQuery(STUDENTS_VIEW, table, pane, "Students");

        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        p.add(toolbar, BorderLayout.NORTH);
        p.add(pane, BorderLayout.CENTER);
        return p;
    }

    private void addStudent(JTable table, JScrollPane pane) {
        // load departments
        JComboBox<String> deptBox = new JComboBox<>();
        int[] deptIds;
        try {
            deptIds = UIUtils.loadCombo(deptBox, DBConnection.getConnection(),
                    "SELECT dept_id, CONCAT(section,' - ',program,' ',branch) AS lbl " +
                            "FROM Departments ORDER BY dept_id",
                    "dept_id", "lbl");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Cannot load departments: " + ex.getMessage());
            return;
        }

        JTextField nameF = new JTextField(20);
        JTextField batchF = new JTextField("2025", 6);
        JTextField cgpaF = new JTextField("8.00", 6);
        JTextField backlogF = new JTextField("0", 4);
        JTextField phoneF = new JTextField(14);
        JTextField emailF = new JTextField(24);

        JPanel form = UIUtils.buildForm(
                new String[] { "Name *:", "Department *:", "Batch Year *:", "CGPA *:",
                        "Active Backlogs:", "Phone:", "Email *:" },
                new JComponent[] { nameF, deptBox, batchF, cgpaF, backlogF, phoneF, emailF });

        int res = JOptionPane.showConfirmDialog(this, form, "Add New Student",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION)
            return;

        try {
            int deptId = deptIds[deptBox.getSelectedIndex()];
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                    "INSERT INTO Students (name,dept_id,batch_year,cgpa,active_backlogs,phone,email) " +
                            "VALUES (?,?,?,?,?,?,?)");
            ps.setString(1, nameF.getText().trim());
            ps.setInt(2, deptId);
            ps.setInt(3, Integer.parseInt(batchF.getText().trim()));
            ps.setDouble(4, Double.parseDouble(cgpaF.getText().trim()));
            ps.setInt(5, Integer.parseInt(backlogF.getText().trim().isEmpty() ? "0" : backlogF.getText().trim()));
            ps.setString(6, phoneF.getText().trim().isEmpty() ? null : phoneF.getText().trim());
            ps.setString(7, emailF.getText().trim());
            ps.executeUpdate();
            ps.close();
            UIUtils.runQuery(STUDENTS_VIEW, table, pane, "Students");
            JOptionPane.showMessageDialog(this, "Student added successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Insert Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStudent(int studentId, JTable table, JScrollPane pane) {
        JTextField cgpaF = new JTextField(6);
        JTextField phoneF = new JTextField(14);
        JComboBox<String> tierBox = new JComboBox<>(
                new String[] { "Unplaced", "Normal", "Dream", "Super Dream" });

        // pre-fill current values
        try {
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                    "SELECT cgpa, phone, placement_tier FROM Students WHERE student_id=?");
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                cgpaF.setText(rs.getString("cgpa"));
                phoneF.setText(rs.getString("phone") != null ? rs.getString("phone") : "");
                tierBox.setSelectedItem(rs.getString("placement_tier"));
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JPanel form = UIUtils.buildForm(
                new String[] { "CGPA:", "Phone:", "Placement Tier:" },
                new JComponent[] { cgpaF, phoneF, tierBox });

        int res = JOptionPane.showConfirmDialog(this, form,
                "Update Student ID " + studentId,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION)
            return;

        try {
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                    "UPDATE Students SET cgpa=?, phone=?, placement_tier=? WHERE student_id=?");
            ps.setDouble(1, Double.parseDouble(cgpaF.getText().trim()));
            ps.setString(2, phoneF.getText().trim().isEmpty() ? null : phoneF.getText().trim());
            ps.setString(3, (String) tierBox.getSelectedItem());
            ps.setInt(4, studentId);
            ps.executeUpdate();
            ps.close();
            UIUtils.runQuery(STUDENTS_VIEW, table, pane, "Students");
            JOptionPane.showMessageDialog(this, "Student updated.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // TAB 2 — COMPANIES
    // =========================================================================
    private JPanel buildCompaniesTab() {
        JTable table = new JTable();
        JScrollPane pane = UIUtils.tablePane(table, "Companies");

        JButton addBtn = new JButton("+ Add Company");
        JButton refreshBtn = new JButton("↺ Refresh");

        addBtn.addActionListener(e -> addCompany(table, pane));
        refreshBtn.addActionListener(e -> UIUtils.runQuery(COMPANIES_VIEW, table, pane, "Companies"));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        toolbar.add(addBtn);
        toolbar.add(refreshBtn);

        UIUtils.runQuery(COMPANIES_VIEW, table, pane, "Companies");

        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        p.add(toolbar, BorderLayout.NORTH);
        p.add(pane, BorderLayout.CENTER);
        return p;
    }

    private void addCompany(JTable table, JScrollPane pane) {
        JTextField nameF = new JTextField(22);
        JTextField sectorF = new JTextField(22);
        JComboBox<String> tierBox = new JComboBox<>(new String[] { "Normal", "Dream", "Super Dream" });
        JTextField websiteF = new JTextField(30);

        JPanel form = UIUtils.buildForm(
                new String[] { "Company Name *:", "Sector:", "Tier *:", "Website:" },
                new JComponent[] { nameF, sectorF, tierBox, websiteF });

        int res = JOptionPane.showConfirmDialog(this, form, "Add Company",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION)
            return;

        try {
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                    "INSERT INTO Companies (name, sector, tier, website) VALUES (?,?,?,?)");
            ps.setString(1, nameF.getText().trim());
            ps.setString(2, sectorF.getText().trim().isEmpty() ? null : sectorF.getText().trim());
            ps.setString(3, (String) tierBox.getSelectedItem());
            ps.setString(4, websiteF.getText().trim().isEmpty() ? null : websiteF.getText().trim());
            ps.executeUpdate();
            ps.close();
            UIUtils.runQuery(COMPANIES_VIEW, table, pane, "Companies");
            JOptionPane.showMessageDialog(this, "Company added.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Insert Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // TAB 3 — DRIVES
    // =========================================================================
    private JPanel buildDrivesTab() {
        JTable table = new JTable();
        JScrollPane pane = UIUtils.tablePane(table, "Drives");

        JButton addBtn = new JButton("+ Add Drive");
        JButton statusBtn = new JButton("✎ Update Status");
        JButton refreshBtn = new JButton("↺ Refresh");

        addBtn.addActionListener(e -> addDrive(table, pane));
        statusBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a drive row.");
                return;
            }
            int id = (Integer) table.getValueAt(row, 0);
            updateDriveStatus(id, table, pane);
        });
        refreshBtn.addActionListener(e -> UIUtils.runQuery(DRIVES_VIEW, table, pane, "Drives"));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        toolbar.add(addBtn);
        toolbar.add(statusBtn);
        toolbar.add(refreshBtn);

        UIUtils.runQuery(DRIVES_VIEW, table, pane, "Drives");

        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        p.add(toolbar, BorderLayout.NORTH);
        p.add(pane, BorderLayout.CENTER);
        return p;
    }

    private void addDrive(JTable table, JScrollPane pane) {
        JComboBox<String> compBox = new JComboBox<>();
        int[] compIds;
        try {
            compIds = UIUtils.loadCombo(compBox, DBConnection.getConnection(),
                    "SELECT company_id, CONCAT(name,' (',tier,')') AS lbl FROM Companies ORDER BY name",
                    "company_id", "lbl");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Cannot load companies: " + ex.getMessage());
            return;
        }

        JTextField dateF = new JTextField("2025-09-01", 12);
        JTextField roleF = new JTextField(22);
        JTextField pkgF = new JTextField("10.00", 8);
        JComboBox<String> typeBox = new JComboBox<>(new String[] { "FTE", "Internship" });
        JComboBox<String> statusBox = new JComboBox<>(new String[] { "Upcoming", "Ongoing", "Completed" });

        JPanel form = UIUtils.buildForm(
                new String[] { "Company *:", "Drive Date * (YYYY-MM-DD):", "Role Offered:",
                        "Package (LPA):", "Drive Type *:", "Status *:" },
                new JComponent[] { compBox, dateF, roleF, pkgF, typeBox, statusBox });

        int res = JOptionPane.showConfirmDialog(this, form, "Add Drive",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION)
            return;

        try {
            int compId = compIds[compBox.getSelectedIndex()];
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                    "INSERT INTO Drives (company_id, drive_date, role_offered, package_lpa, drive_type, status) " +
                            "VALUES (?,?,?,?,?,?)");
            ps.setInt(1, compId);
            ps.setString(2, dateF.getText().trim());
            ps.setString(3, roleF.getText().trim().isEmpty() ? null : roleF.getText().trim());
            ps.setDouble(4, Double.parseDouble(pkgF.getText().trim()));
            ps.setString(5, (String) typeBox.getSelectedItem());
            ps.setString(6, (String) statusBox.getSelectedItem());
            ps.executeUpdate();
            ps.close();
            UIUtils.runQuery(DRIVES_VIEW, table, pane, "Drives");
            JOptionPane.showMessageDialog(this, "Drive added.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Insert Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDriveStatus(int driveId, JTable table, JScrollPane pane) {
        JComboBox<String> statusBox = new JComboBox<>(new String[] { "Upcoming", "Ongoing", "Completed" });
        int res = JOptionPane.showConfirmDialog(this,
                UIUtils.buildForm(new String[] { "New Status:" }, new JComponent[] { statusBox }),
                "Update Drive " + driveId + " Status",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION)
            return;
        try {
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                    "UPDATE Drives SET status=? WHERE drive_id=?");
            ps.setString(1, (String) statusBox.getSelectedItem());
            ps.setInt(2, driveId);
            ps.executeUpdate();
            ps.close();
            UIUtils.runQuery(DRIVES_VIEW, table, pane, "Drives");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // TAB 4 — APPLICATIONS
    // =========================================================================
    private JPanel buildApplicationsTab() {
        JTable table = new JTable();
        JScrollPane pane = UIUtils.tablePane(table, "Applications");

        JButton addBtn = new JButton("+ Add Application");
        JButton resBtn = new JButton("✎ Update Result");
        JButton refreshBtn = new JButton("↺ Refresh");

        addBtn.addActionListener(e -> addApplication(table, pane));
        resBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a row.");
                return;
            }
            int id = (Integer) table.getValueAt(row, 0);
            updateApplicationResult(id, table, pane);
        });
        refreshBtn.addActionListener(e -> UIUtils.runQuery(APPS_VIEW, table, pane, "Applications"));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        toolbar.add(addBtn);
        toolbar.add(resBtn);
        toolbar.add(refreshBtn);

        UIUtils.runQuery(APPS_VIEW, table, pane, "Applications");

        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        p.add(toolbar, BorderLayout.NORTH);
        p.add(pane, BorderLayout.CENTER);
        return p;
    }

    private void addApplication(JTable table, JScrollPane pane) {
        JComboBox<String> stuBox = new JComboBox<>();
        JComboBox<String> drvBox = new JComboBox<>();
        int[] stuIds, drvIds;
        try {
            stuIds = UIUtils.loadCombo(stuBox, DBConnection.getConnection(),
                    "SELECT student_id, CONCAT(student_id,' - ',name) AS lbl FROM Students ORDER BY name",
                    "student_id", "lbl");
            drvIds = UIUtils.loadCombo(drvBox, DBConnection.getConnection(),
                    "SELECT d.drive_id, CONCAT(d.drive_id,' - ',c.name,' ',d.drive_date) AS lbl " +
                            "FROM Drives d JOIN Companies c ON d.company_id=c.company_id ORDER BY d.drive_date DESC",
                    "drive_id", "lbl");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Cannot load data: " + ex.getMessage());
            return;
        }

        JTextField dateF = new JTextField(java.time.LocalDate.now().toString(), 12);

        JPanel form = UIUtils.buildForm(
                new String[] { "Student *:", "Drive *:", "Apply Date (YYYY-MM-DD):" },
                new JComponent[] { stuBox, drvBox, dateF });

        int res = JOptionPane.showConfirmDialog(this, form, "Add Application",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION)
            return;

        try {
            int stuId = stuIds[stuBox.getSelectedIndex()];
            int drvId = drvIds[drvBox.getSelectedIndex()];
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                    "INSERT INTO Applications (student_id, drive_id, apply_date) VALUES (?,?,?)");
            ps.setInt(1, stuId);
            ps.setInt(2, drvId);
            ps.setString(3, dateF.getText().trim());
            ps.executeUpdate();
            ps.close();
            UIUtils.runQuery(APPS_VIEW, table, pane, "Applications");
            JOptionPane.showMessageDialog(this, "Application added.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Insert Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateApplicationResult(int appId, JTable table, JScrollPane pane) {
        JComboBox<String> resBox = new JComboBox<>(new String[] { "Pending", "Selected", "Rejected" });
        JTextField offerDateF = new JTextField(12);
        JPanel form = UIUtils.buildForm(
                new String[] { "Result:", "Offer Letter Date (YYYY-MM-DD or blank):" },
                new JComponent[] { resBox, offerDateF });
        int res = JOptionPane.showConfirmDialog(this, form,
                "Update Application " + appId,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION)
            return;
        try {
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                    "UPDATE Applications SET result=?, offer_letter_date=? WHERE application_id=?");
            ps.setString(1, (String) resBox.getSelectedItem());
            String od = offerDateF.getText().trim();
            if (od.isEmpty())
                ps.setNull(2, Types.DATE);
            else
                ps.setString(2, od);
            ps.setInt(3, appId);
            ps.executeUpdate();
            ps.close();
            UIUtils.runQuery(APPS_VIEW, table, pane, "Applications");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // TAB 5 — ROUNDS
    // =========================================================================
    private JPanel buildRoundsTab() {
        JTable table = new JTable();
        JScrollPane pane = UIUtils.tablePane(table, "Rounds");

        JTextField appIdF = new JTextField("", 8);
        JButton loadBtn = new JButton("Load Rounds");
        JButton addBtn = new JButton("+ Add Round");

        loadBtn.addActionListener(e -> {
            String id = appIdF.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter Application ID.");
                return;
            }
            UIUtils.runQuery(
                    "SELECT r.round_id AS Round_ID, r.round_number AS Round_No, " +
                            "r.round_type AS Type, r.score AS Score, r.status AS Status, r.remarks AS Remarks " +
                            "FROM Rounds r WHERE r.application_id=" + id + " ORDER BY r.round_number",
                    table, pane, "Rounds for Application " + id);
        });

        addBtn.addActionListener(e -> {
            String id = appIdF.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter Application ID first.");
                return;
            }
            addRound(Integer.parseInt(id), table, pane);
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        toolbar.add(new JLabel("Application ID:"));
        toolbar.add(appIdF);
        toolbar.add(loadBtn);
        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(addBtn);

        JLabel hint = new JLabel("  Tip: find the Application ID from the Applications tab.");
        hint.setFont(new Font("Arial", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);

        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        p.add(toolbar, BorderLayout.NORTH);
        p.add(pane, BorderLayout.CENTER);
        p.add(hint, BorderLayout.SOUTH);
        return p;
    }

    private void addRound(int appId, JTable table, JScrollPane pane) {
        JTextField roundNoF = new JTextField("1", 4);
        JComboBox<String> typeBox = new JComboBox<>(
                new String[] { "Aptitude", "Coding", "Technical", "HR", "GD" });
        JTextField scoreF = new JTextField("", 8);
        JComboBox<String> statusBox = new JComboBox<>(new String[] { "Pending", "Pass", "Fail" });
        JTextField remarksF = new JTextField(30);

        JPanel form = UIUtils.buildForm(
                new String[] { "Round Number:", "Round Type:", "Score:", "Status:", "Remarks:" },
                new JComponent[] { roundNoF, typeBox, scoreF, statusBox, remarksF });

        int res = JOptionPane.showConfirmDialog(this, form,
                "Add Round to Application " + appId,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION)
            return;

        try {
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                    "INSERT INTO Rounds (application_id, round_number, round_type, score, status, remarks) " +
                            "VALUES (?,?,?,?,?,?)");
            ps.setInt(1, appId);
            ps.setInt(2, Integer.parseInt(roundNoF.getText().trim()));
            ps.setString(3, (String) typeBox.getSelectedItem());
            String sc = scoreF.getText().trim();
            if (sc.isEmpty())
                ps.setNull(4, Types.DECIMAL);
            else
                ps.setDouble(4, Double.parseDouble(sc));
            ps.setString(5, (String) statusBox.getSelectedItem());
            ps.setString(6, remarksF.getText().trim().isEmpty() ? null : remarksF.getText().trim());
            ps.executeUpdate();
            ps.close();
            // reload
            UIUtils.runQuery(
                    "SELECT round_id AS Round_ID, round_number AS Round_No, round_type AS Type, " +
                            "score AS Score, status AS Status, remarks AS Remarks " +
                            "FROM Rounds WHERE application_id=" + appId + " ORDER BY round_number",
                    table, pane, "Rounds for Application " + appId);
            JOptionPane.showMessageDialog(this, "Round added.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Insert Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // TAB 6 — ELIGIBILITY CHECK
    // =========================================================================
    private JPanel buildEligibilityTab() {
        JComboBox<String> driveBox = new JComboBox<>();
        int[] driveIds = new int[0];

        JTable table = new JTable();
        JScrollPane pane = UIUtils.tablePane(table, "Eligible Students");

        JButton loadDrivesBtn = new JButton("Load Drives");
        JButton checkBtn = new JButton("▶ Check Eligible Students");
        checkBtn.setBackground(new Color(30, 90, 160));
        checkBtn.setForeground(Color.WHITE);
        checkBtn.setFocusPainted(false);

        // Load eligibility info text
        JTextArea infoArea = new JTextArea(3, 60);
        infoArea.setEditable(false);
        infoArea.setBackground(new Color(255, 255, 240));
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoScroll.setBorder(BorderFactory.createTitledBorder("Eligibility Criteria for Selected Drive"));

        final int[] driveIdsRef = { 0 };

        loadDrivesBtn.addActionListener(e -> {
            try {
                int[] ids = UIUtils.loadCombo(driveBox, DBConnection.getConnection(),
                        "SELECT d.drive_id, CONCAT(d.drive_id,' | ',c.name,' | ',d.drive_date," +
                                "' | ',d.role_offered,' | ',d.package_lpa,' LPA') AS lbl " +
                                "FROM Drives d JOIN Companies c ON d.company_id=c.company_id " +
                                "ORDER BY d.drive_date DESC",
                        "drive_id", "lbl");
                System.arraycopy(ids, 0, driveIdsRef, 0, 0);
                driveIdsRef[0] = ids.length > 0 ? ids[0] : 0;
                // store all ids in a shared mutable
                if (ids.length > 0) {
                    driveBox.putClientProperty("ids", ids);
                }
                JOptionPane.showMessageDialog(this, ids.length + " drives loaded.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        checkBtn.addActionListener(e -> {
            Object idsObj = driveBox.getClientProperty("ids");
            if (idsObj == null || driveBox.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "Load drives first.");
                return;
            }
            int[] ids = (int[]) idsObj;
            int idx = driveBox.getSelectedIndex();
            if (idx < 0 || idx >= ids.length)
                return;
            int driveId = ids[idx];

            // show eligibility criteria
            try {
                PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                        "SELECT dept.section, dept.program, dept.branch, de.min_cgpa, de.max_backlogs " +
                                "FROM DriveEligibility de JOIN Departments dept ON de.dept_id=dept.dept_id " +
                                "WHERE de.drive_id=?");
                ps.setInt(1, driveId);
                ResultSet rs = ps.executeQuery();
                StringBuilder sb = new StringBuilder("Eligible Departments:\n");
                while (rs.next()) {
                    sb.append(String.format("  %-10s %s %-4s  min CGPA: %.2f  max Backlogs: %d\n",
                            rs.getString("section"),
                            rs.getString("program"),
                            rs.getString("branch"),
                            rs.getDouble("min_cgpa"),
                            rs.getInt("max_backlogs")));
                }
                infoArea.setText(sb.toString());
                rs.close();
                ps.close();
            } catch (SQLException ex) {
                infoArea.setText("Error: " + ex.getMessage());
            }

            // eligible students
            String eligSql = "SELECT DISTINCT s.student_id AS ID, s.name AS Name, " +
                    "       dept.section AS Section, s.cgpa AS CGPA, " +
                    "       s.active_backlogs AS Backlogs, s.placement_tier AS Current_Tier, " +
                    "       CASE WHEN a.application_id IS NOT NULL THEN 'YES' ELSE 'NO' END AS Already_Applied " +
                    "FROM Students s " +
                    "JOIN Departments    dept ON s.dept_id  = dept.dept_id " +
                    "JOIN DriveEligibility de ON de.dept_id = dept.dept_id " +
                    "                        AND de.drive_id = " + driveId +
                    "                        AND s.cgpa >= de.min_cgpa " +
                    "                        AND s.active_backlogs <= de.max_backlogs " +
                    "LEFT JOIN Applications a ON a.student_id = s.student_id " +
                    "                         AND a.drive_id  = " + driveId + " " +
                    "ORDER BY s.cgpa DESC";

            UIUtils.runQuery(eligSql, table, pane, "Eligible Students for Drive " + driveId);
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        toolbar.add(loadDrivesBtn);
        toolbar.add(new JLabel("Drive:"));
        toolbar.add(driveBox);
        toolbar.add(checkBtn);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, infoScroll, pane);
        split.setResizeWeight(0.25);

        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        p.add(toolbar, BorderLayout.NORTH);
        p.add(split, BorderLayout.CENTER);
        return p;
    }
}
