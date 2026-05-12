package ui;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;


public class AdminFrame extends JFrame {

    public AdminFrame() {
        setTitle("PRMS — Database Administrator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // ── Menu bar ──────────────────────────────────────────────────────────
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(e -> logout());
        file.add(logout);
        mb.add(file);

        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "PRMS — Database Administrator Panel\n"
                + "CS621 Database Systems | IIIT Guwahati\n\n"
                + "• SQL Editor  : execute any SQL statement\n"
                + "• Table Browser: view table contents\n"
                + "• Schema Info  : describe tables, indexes, triggers",
                "About", JOptionPane.INFORMATION_MESSAGE));
        help.add(about);
        mb.add(help);
        setJMenuBar(mb);

        // ── Tabs ──────────────────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("⌨  SQL Editor",    buildSQLEditor());
        tabs.addTab("🗄  Table Browser", buildTableBrowser());
        tabs.addTab("📋  Schema Info",   buildSchemaInfo());
        add(tabs);
    }

    // =========================================================================
    // TAB 1 — SQL EDITOR
    // =========================================================================
    private JPanel buildSQLEditor() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // ── SQL input ─────────────────────────────────────────────────────────
        JTextArea sqlArea = new JTextArea(9, 80);
        sqlArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane sqlScroll = new JScrollPane(sqlArea);
        sqlScroll.setBorder(BorderFactory.createTitledBorder(
                "SQL Statement  (press F5 or click Execute)"));

        // ── Toolbar ───────────────────────────────────────────────────────────
        JButton execBtn  = new JButton("▶  Execute (F5)");
        JButton clearBtn = new JButton("Clear");
        JLabel  status   = new JLabel("Ready.");
        execBtn .setBackground(new Color(30, 140, 60));
        execBtn .setForeground(Color.WHITE);
        execBtn .setFocusPainted(false);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.add(execBtn);
        toolbar.add(clearBtn);
        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(status);

        // ── Results ───────────────────────────────────────────────────────────
        JTable  resultTable = new JTable();
        resultTable.setRowHeight(22);
        resultTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        JScrollPane resultPane = UIUtils.tablePane(resultTable, "Results");

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sqlScroll, resultPane);
        split.setResizeWeight(0.30);

        // ── Actions ───────────────────────────────────────────────────────────
        execBtn.addActionListener(e -> {
            String sql = sqlArea.getText().trim();
            if (sql.isEmpty()) return;
            try {
                Connection conn = DBConnection.getConnection();
                String upper = sql.toLowerCase();
                boolean isQuery = upper.startsWith("select")
                        || upper.startsWith("show")
                        || upper.startsWith("describe")
                        || upper.startsWith("explain");

                if (isQuery) {
                    Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery(sql);
                    DefaultTableModel model = UIUtils.resultSetToModel(rs);
                    resultTable.setModel(model);
                    UIUtils.autoResize(resultTable);
                    resultPane.setBorder(BorderFactory.createTitledBorder(
                            "Results  (" + model.getRowCount() + " rows)"));
                    status.setText("✔  " + model.getRowCount() + " row(s) returned.");
                    rs.close(); st.close();
                } else {
                    Statement st = conn.createStatement();
                    int affected = st.executeUpdate(sql);
                    resultTable.setModel(new DefaultTableModel());
                    resultPane.setBorder(BorderFactory.createTitledBorder("Results"));
                    status.setText("✔  " + affected + " row(s) affected.");
                    st.close();
                }
                status.setForeground(new Color(0, 120, 0));
            } catch (SQLException ex) {
                status.setText("✘  " + ex.getMessage());
                status.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        clearBtn.addActionListener(e -> {
            sqlArea.setText("");
            resultTable.setModel(new DefaultTableModel());
            status.setText("Ready.");
            status.setForeground(Color.BLACK);
        });

        // F5 shortcut
        sqlArea.getInputMap().put(KeyStroke.getKeyStroke("F5"), "exec");
        sqlArea.getActionMap().put("exec",
                new javax.swing.AbstractAction() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        execBtn.doClick();
                    }
                });

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(split,   BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // TAB 2 — TABLE BROWSER
    // =========================================================================
    private JPanel buildTableBrowser() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> tableList = new JList<>(listModel);
        tableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableList.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane listPane = new JScrollPane(tableList);
        listPane.setBorder(BorderFactory.createTitledBorder("Tables"));
        listPane.setPreferredSize(new Dimension(190, 0));

        JTable dataTable = new JTable();
        dataTable.setRowHeight(22);
        dataTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        JScrollPane dataPane = UIUtils.tablePane(dataTable, "Select a table →");

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPane, dataPane);
        split.setDividerLocation(200);

        JLabel hint = new JLabel("  Showing up to 500 rows. Use SQL Editor for custom queries.");
        hint.setFont(new Font("Arial", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);

        // Load table names
        try {
            DatabaseMetaData meta = DBConnection.getConnection().getMetaData();
            ResultSet rs = meta.getTables("placement_records_db", null, "%",
                    new String[]{"TABLE"});
            while (rs.next()) listModel.addElement(rs.getString("TABLE_NAME"));
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        tableList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            String tbl = tableList.getSelectedValue();
            if (tbl == null) return;
            UIUtils.runQuery("SELECT * FROM " + tbl + " LIMIT 500",
                    dataTable, dataPane, tbl);
        });

        panel.add(split, BorderLayout.CENTER);
        panel.add(hint,  BorderLayout.SOUTH);
        return panel;
    }

    // =========================================================================
    // TAB 3 — SCHEMA INFO
    // =========================================================================
    private JPanel buildSchemaInfo() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // ── Top controls ──────────────────────────────────────────────────────
        JComboBox<String> tableBox = new JComboBox<>();
        JButton descBtn  = new JButton("DESCRIBE");
        JButton idxBtn   = new JButton("SHOW INDEXES");
        JButton fkBtn    = new JButton("Foreign Keys");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        top.add(new JLabel("Table:"));
        top.add(tableBox);
        top.add(descBtn);
        top.add(idxBtn);
        top.add(fkBtn);

        // ── Schema table ──────────────────────────────────────────────────────
        JTable schemaTable = new JTable();
        schemaTable.setRowHeight(22);
        JScrollPane schemaPane = UIUtils.tablePane(schemaTable, "Table Details");

        // ── Triggers / constraints text ────────────────────────────────────────
        JTextArea infoArea = new JTextArea(10, 60);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        infoArea.setEditable(false);
        infoArea.setBackground(new Color(252, 252, 252));
        JScrollPane infoPane = new JScrollPane(infoArea);
        infoPane.setBorder(BorderFactory.createTitledBorder("Triggers in Database"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, schemaPane, infoPane);
        split.setResizeWeight(0.55);

        // Load table names
        try {
            DatabaseMetaData meta = DBConnection.getConnection().getMetaData();
            ResultSet rs = meta.getTables("placement_records_db", null, "%",
                    new String[]{"TABLE"});
            while (rs.next()) tableBox.addItem(rs.getString("TABLE_NAME"));
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Load triggers once
        loadTriggers(infoArea);

        // ── Button actions ────────────────────────────────────────────────────
        descBtn.addActionListener(e -> {
            String tbl = (String) tableBox.getSelectedItem();
            if (tbl == null) return;
            UIUtils.runQuery("DESCRIBE " + tbl, schemaTable, schemaPane, "DESCRIBE " + tbl);
        });

        idxBtn.addActionListener(e -> {
            String tbl = (String) tableBox.getSelectedItem();
            if (tbl == null) return;
            UIUtils.runQuery("SHOW INDEXES FROM " + tbl,
                    schemaTable, schemaPane, "Indexes: " + tbl);
        });

        fkBtn.addActionListener(e -> {
            String tbl = (String) tableBox.getSelectedItem();
            if (tbl == null) return;
            String sql =
                "SELECT kcu.CONSTRAINT_NAME, kcu.COLUMN_NAME, " +
                "       kcu.REFERENCED_TABLE_NAME, kcu.REFERENCED_COLUMN_NAME, " +
                "       rc.UPDATE_RULE, rc.DELETE_RULE " +
                "FROM information_schema.KEY_COLUMN_USAGE kcu " +
                "JOIN information_schema.REFERENTIAL_CONSTRAINTS rc " +
                "  ON rc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME " +
                "     AND rc.CONSTRAINT_SCHEMA = kcu.TABLE_SCHEMA " +
                "WHERE kcu.TABLE_SCHEMA = 'placement_records_db' " +
                "  AND kcu.TABLE_NAME   = '" + tbl + "' " +
                "  AND kcu.REFERENCED_TABLE_NAME IS NOT NULL";
            UIUtils.runQuery(sql, schemaTable, schemaPane, "Foreign Keys: " + tbl);
        });

        panel.add(top,   BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void loadTriggers(JTextArea area) {
        try {
            Statement st = DBConnection.getConnection().createStatement();
            ResultSet rs = st.executeQuery("SHOW TRIGGERS");
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(String.format("%-30s  %s %s  [%s]\n",
                        rs.getString("Trigger"),
                        rs.getString("Timing"),
                        rs.getString("Event"),
                        rs.getString("Table")));
                String stmt = rs.getString("Statement");
                // truncate for display
                if (stmt != null && stmt.length() > 120)
                    stmt = stmt.substring(0, 120) + "...";
                sb.append("    ").append(stmt).append("\n\n");
            }
            area.setText(sb.length() == 0 ? "(No triggers found)" : sb.toString());
            rs.close(); st.close();
        } catch (SQLException ex) {
            area.setText("Error loading triggers: " + ex.getMessage());
        }
    }

    private void logout() {
        DBConnection.close();
        dispose();
        new LoginFrame().setVisible(true);
    }
}
