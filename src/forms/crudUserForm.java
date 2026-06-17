package forms;

import cls.KeyValue;
import db.DBConnection;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class crudUserForm extends JDialog {
    private JPanel userDialog;
    private JTextField nameField;
    private JTextField passwordField;
    private JButton cancelButton;
    private JButton confirmButton;
    private JComboBox<KeyValue> roleCombo;
    private final String action;
    private final String[] userData;

    public crudUserForm(String action, String[] userData) {
        this.action = action;
        this.userData = userData;

        setTitle("User Form");
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 600, 300);
        confirmButton.addActionListener(new ConfirmButtonListener());
        cancelButton.addActionListener(new CancelButtonListener());
        setContentPane(userDialog);
        initializeComboBox();

        if (action.equals("edit") && userData != null) {
            nameField.setText(userData[1]);
            passwordField.setText(userData[2]);
            roleCombo.setSelectedItem(userData[3]);
        }
    }

    private class ConfirmButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText();
            String password = passwordField.getText();
            KeyValue roleKey = (KeyValue) roleCombo.getSelectedItem();
            String roleId = roleKey != null ? String.valueOf(roleKey.getKey()) : "";

            // Input validation
            if (name.isEmpty() || password.isEmpty() || roleId.isEmpty()) {
                JOptionPane.showMessageDialog(userDialog, "Please fill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection connection = DBConnection.getConnection()) {
                String query;
                if (action.equals("add")) {
                    query = "INSERT INTO user(username, password, role_id) VALUES(?, ?, ?)";
                } else {
                    query = "UPDATE user SET username = ?, password = ?, role_id = ? WHERE id = ?";
                }

                try (PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, name);
                    statement.setString(2, password);
                    statement.setString(3, roleId);
                    if (action.equals("edit")) {
                        statement.setString(4, userData[0]);
                    }
                    statement.executeUpdate();

                    if (action.equals("add")) {
                        ResultSet rs = statement.getGeneratedKeys();
                        if (rs.next()) {
                            int id = rs.getInt(1);
                            JOptionPane.showMessageDialog(userDialog, "Book added with ID: " + id, "Success", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(userDialog, "Book updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }

                    dispose(); // Close the form after successful insertion or update
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(userDialog, "Error saving book information", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class CancelButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            nameField.setText("");
            passwordField.setText("");
            roleCombo.setSelectedIndex(0);
        }
    }

    private void initializeComboBox() {
        KeyValue[] items = {
                new KeyValue(0, "SELECT ROLE"),
                new KeyValue(1, "Admin"),
                new KeyValue(2, "User"),
                new KeyValue(3, "Guest"),
        };
        for(KeyValue item : items) {
            roleCombo.addItem(item);
        }
    }
}
