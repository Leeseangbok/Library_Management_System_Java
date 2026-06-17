package forms;

import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class viewUserForm extends JPanel {

    private final int idWidth = 10;
    private JPanel viewUserPanel;
    private JPanel userPanel;
    private JTextField searchField;
    private JTable table1;
    private JButton searchButton;
    private JButton deleteButton;
    private JButton updateButton;
    private JButton addButton;
    private JPanel tablePanel;
    private List<String[]> userList = fetchUsersFromDB();

    public viewUserForm() {
        add(viewUserPanel);
        createUserTable();
        searchButton.addActionListener(e -> setSearchButtonAction());
        deleteButton.addActionListener(e -> setDeleteButtonAction());
        updateButton.addActionListener(e -> setEditButtonAction());
        addButton.addActionListener(e -> setAddButtonAction());
    }

    private void setSearchButtonAction() {
        String searchText = searchField.getText().toLowerCase();
        List<String[]> filteredUsers = fetchFilteredUsers(searchText);

        DefaultTableModel tableModel = (DefaultTableModel) table1.getModel();
        tableModel.setRowCount(0); // Clear existing rows

        for (String[] user : filteredUsers) {
            tableModel.addRow(user);
        }
        setColumnWidths(table1);
    }

    private void setAddButtonAction() {
        crudUserForm addUserForm = new crudUserForm("add", null);
        addUserForm.setVisible(true);
        addUserForm.setLocationRelativeTo(null);
        addUserForm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                refreshUserList();
            }
        });
    }

    private void setEditButtonAction() {
        int selectedRow = table1.getSelectedRow();
        if (selectedRow != -1) {
            String[] userData = new String[table1.getColumnCount()];
            for (int i = 0; i < table1.getColumnCount(); i++) {
                userData[i] = table1.getValueAt(selectedRow, i).toString();
            }
            crudUserForm editUserForm = new crudUserForm("edit", userData);
            editUserForm.setLocationRelativeTo(null);
            editUserForm.setVisible(true);

            editUserForm.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    refreshUserList();
                }
            });
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to edit", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void setDeleteButtonAction() {
        int selectedRow = table1.getSelectedRow();
        if (selectedRow != -1) {
            String userId = table1.getValueAt(selectedRow, 0).toString(); // Assuming ID is the first column

            int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                try (Connection connection = DBConnection.getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM user WHERE id = ?")) {

                    preparedStatement.setString(1, userId);
                    preparedStatement.executeUpdate();

                    JOptionPane.showMessageDialog(this, "User deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshUserList();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting user from database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to delete", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshUserList() {
        userList = fetchUsersFromDB();
        DefaultTableModel tableModel = (DefaultTableModel) table1.getModel();
        tableModel.setRowCount(0); // Clear existing rows

        for (String[] user : userList) {
            tableModel.addRow(user);
        }
        setColumnWidths(table1);
    }

    private List<String[]> fetchUsersFromDB() {
        List<String[]> users = new ArrayList<>();
        String query = "SELECT u.id, u.username, u.password, r.role_name FROM user u JOIN role r ON u.role_id = r.id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String[] user = {
                        resultSet.getString("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("role_name"),
                };
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    private List<String[]> fetchFilteredUsers(String searchText) {
        List<String[]> users = new ArrayList<>();

        for (String[] user : fetchUsersFromDB()) {
            if (user[0].toLowerCase().contains(searchText)) {
                users.add(user);
            } else if (user[1].toLowerCase().contains(searchText)) {
                users.add(user);
            }
        }
        return users;
    }

    private void createUserTable() {
        String[] columnNames = {"ID", "Name", "Password", "Role"};

        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (String[] user : userList) {
            tableModel.addRow(user);
        }

        table1.setModel(tableModel);
        table1.setFillsViewportHeight(true);
        table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setColumnWidths(table1);

        JScrollPane scrollPane = new JScrollPane(table1);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void setColumnWidths(JTable table) {
        TableColumn idCol = table.getColumnModel().getColumn(0);
        idCol.setPreferredWidth(idWidth * 10);

        TableColumn nameCol = table.getColumnModel().getColumn(1);
        nameCol.setPreferredWidth(idWidth * 30);

        TableColumn passCol = table.getColumnModel().getColumn(2);
        passCol.setPreferredWidth(idWidth * 30);

        TableColumn roleCol = table.getColumnModel().getColumn(3);
        roleCol.setPreferredWidth(idWidth * 15);
    }
}
