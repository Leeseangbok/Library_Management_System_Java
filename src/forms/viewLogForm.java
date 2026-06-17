package forms;

import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class viewLogForm extends JPanel {

    private final int idWidth = 10;
    private JPanel userPanel;
    private JPanel tablePanel;
    private JPanel listPanel;
    private JTable userTable;

    public viewLogForm() {
        createUIComponents(); // Initialize components
        setLayout(new BorderLayout()); // Set layout for the JPanel
        add(userPanel, BorderLayout.CENTER); // Add userPanel to the form
        createBookTable(); // Populate the table
    }

    private List<String[]> fetchLogIn() {
        List<String[]> userList = new ArrayList<>();
        String query = "SELECT l.id, u.username, r.role_name, l.log_in_date " +
                "FROM login l " +
                "JOIN user u ON l.username_id = u.id " +
                "JOIN role r ON l.role_id = r.id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String[] row = {
                        resultSet.getString("id"),
                        resultSet.getString("username"),
                        resultSet.getString("role_name"),
                        resultSet.getString("log_in_date")
                };
                userList.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    private void createBookTable() {
        List<String[]> userList = fetchLogIn();
        String[] columnNames = {"ID", "Username", "Role Name", "Log In Date"};

        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Set to false for all columns
            }
        };

        for (String[] user : userList) {
            tableModel.addRow(user);
        }

        userTable.setModel(tableModel);
        userTable.setFillsViewportHeight(true);

        setColumnWidths(userTable);

        JScrollPane scrollPane = new JScrollPane(userTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void setColumnWidths(JTable table) {
        TableColumn idCol = table.getColumnModel().getColumn(0);
        idCol.setPreferredWidth(idWidth * 10);

        TableColumn usernameCol = table.getColumnModel().getColumn(1);
        usernameCol.setPreferredWidth(idWidth * 30);

        TableColumn roleCol = table.getColumnModel().getColumn(2);
        roleCol.setPreferredWidth(idWidth * 15);

        TableColumn dateCol = table.getColumnModel().getColumn(3);
        dateCol.setPreferredWidth(idWidth * 30);
    }

    private void createUIComponents() {
        userPanel = new JPanel(new BorderLayout());
        tablePanel = new JPanel(new BorderLayout());
        listPanel = new JPanel(); // Initialize listPanel if needed
        userTable = new JTable(); // Initialize userTable
        userTable.setBackground(Color.getHSBColor(251,238,235));
        JLabel userLabel = new JLabel("USER LOGIN LIST");
        userPanel.add(userLabel, BorderLayout.NORTH);
        userPanel.add(tablePanel, BorderLayout.CENTER);
    }
}
