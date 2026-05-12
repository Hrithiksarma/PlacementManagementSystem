package ui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

/**
 * Shared Swing + JDBC helper methods used by all frames.
 */
public class UIUtils {

    /** Convert a ResultSet into a non-editable DefaultTableModel. */
    public static DefaultTableModel resultSetToModel(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        Vector<String> colNames = new Vector<>();
        for (int i = 1; i <= cols; i++) colNames.add(meta.getColumnLabel(i));
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int i = 1; i <= cols; i++) row.add(rs.getObject(i));
            data.add(row);
        }
        return new DefaultTableModel(data, colNames) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    /** Auto-size each column to fit the widest cell (max 300 px). */
    public static void autoResize(JTable table) {
        for (int col = 0; col < table.getColumnCount(); col++) {
            TableColumn tc = table.getColumnModel().getColumn(col);
            int max = 50;
            // header
            TableCellRenderer hr = table.getTableHeader().getDefaultRenderer();
            Component hc = hr.getTableCellRendererComponent(
                    table, tc.getHeaderValue(), false, false, 0, col);
            max = Math.max(hc.getPreferredSize().width + 12, max);
            // rows (sample first 100)
            for (int row = 0; row < Math.min(table.getRowCount(), 100); row++) {
                TableCellRenderer cr = table.getCellRenderer(row, col);
                Component cc = table.prepareRenderer(cr, row, col);
                max = Math.max(cc.getPreferredSize().width + 12, max);
            }
            tc.setPreferredWidth(Math.min(max, 300));
        }
    }

    /** Build a GridLayout form panel from parallel label/component arrays. */
    public static JPanel buildForm(String[] labels, JComponent[] fields) {
        JPanel p = new JPanel(new GridLayout(labels.length, 2, 8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        for (int i = 0; i < labels.length; i++) {
            p.add(new JLabel(labels[i]));
            p.add(fields[i]);
        }
        return p;
    }

    /** Fill a JComboBox with rows from a query; returns int[] of IDs in same order. */
    public static int[] loadCombo(JComboBox<String> box, Connection conn,
                                   String query, String idCol, String labelCol)
            throws SQLException {
        box.removeAllItems();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(query);
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        while (rs.next()) {
            ids.add(rs.getInt(idCol));
            box.addItem(rs.getString(labelCol));
        }
        rs.close(); st.close();
        return ids.stream().mapToInt(Integer::intValue).toArray();
    }

    /** Standard blue header label. */
    public static JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 15));
        lbl.setForeground(new Color(30, 90, 160));
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        return lbl;
    }

    /** Create a styled JTable wrapped in a JScrollPane with a titled border. */
    public static JScrollPane tablePane(JTable table, String title) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(22);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        JScrollPane sp = new JScrollPane(table);
        if (title != null) sp.setBorder(BorderFactory.createTitledBorder(title));
        return sp;
    }

    /** Convenience: run a SELECT, update table model and resize. */
    public static void runQuery(String sql, JTable table, JScrollPane pane, String borderTitle) {
        try {
            Statement st = db.DBConnection.getConnection().createStatement();
            ResultSet rs = st.executeQuery(sql);
            DefaultTableModel m = resultSetToModel(rs);
            table.setModel(m);
            autoResize(table);
            if (pane != null && borderTitle != null)
                pane.setBorder(BorderFactory.createTitledBorder(
                        borderTitle + "  (" + m.getRowCount() + " rows)"));
            rs.close(); st.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Query Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
