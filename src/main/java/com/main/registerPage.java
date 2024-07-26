package src.main.java.com.mainInterface;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import src.main.java.com.database.DataBase;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class registerPage extends JPanel {
    private JTextField customerNameField;
    private JTextField customerPhoneNumberField;
    private JTextField borrowDateField;
    private JTextField returnDateField;
    private JButton searchButton;
    private JButton registerButton;
    private JTable selectedBooksTable;
    private DefaultTableModel tableModel;
    private List<String[]> selectedBooks;

    public registerPage() {
        setLayout(new BorderLayout());

        // Create a panel for the input fields
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5); // Add padding around components

        // Add input fields and labels
        addInputFields(inputPanel, constraints);

        // Create and add the table to display selected books
        String[] columnNames = {"ID", "Name", "Author", "Price"};
        tableModel = new DefaultTableModel(columnNames, 0);
        selectedBooksTable = new JTable(tableModel);
        selectedBooksTable.setFillsViewportHeight(true);
        selectedBooksTable.setRowHeight(25);
        JScrollPane tableScrollPane = new JScrollPane(selectedBooksTable);
        tableScrollPane.setPreferredSize(new Dimension(400, 250));
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.weighty = 1.0; // Allow table to expand vertically
        constraints.fill = GridBagConstraints.BOTH;
        inputPanel.add(tableScrollPane, constraints);

        // Add buttons to the input panel
        searchButton = new JButton("Search Book");
        searchButton.addActionListener(new SearchButtonActionListener());
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.weighty = 0; // Reset weight for buttons
        constraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(searchButton, constraints);

        registerButton = new JButton("Register");
        registerButton.addActionListener(new RegisterButtonActionListener());
        constraints.gridx = 1;
        inputPanel.add(registerButton, constraints);

        // Add the input panel to the register page
        add(inputPanel, BorderLayout.CENTER);
    }

    private void addInputFields(JPanel panel, GridBagConstraints constraints) {
        // Customer Name
        JLabel customerNameLabel = new JLabel("Customer Name:");
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        panel.add(customerNameLabel, constraints);
        customerNameField = new JTextField(20);
        constraints.gridx = 1;
        panel.add(customerNameField, constraints);

        // Customer Phone Number
        JLabel customerPhoneNumberLabel = new JLabel("Customer Phone Number:");
        constraints.gridx = 0;
        constraints.gridy = 3;
        panel.add(customerPhoneNumberLabel, constraints);
        customerPhoneNumberField = new JTextField(20);
        constraints.gridx = 1;
        panel.add(customerPhoneNumberField, constraints);

        // Borrow Date
        JLabel borrowDateLabel = new JLabel("Borrow Date (YYYY-MM-DD):");
        constraints.gridx = 0;
        constraints.gridy = 4;
        panel.add(borrowDateLabel, constraints);
        borrowDateField = new JTextField(20);
        constraints.gridx = 1;
        panel.add(borrowDateField, constraints);

        // Return Date
        JLabel returnDateLabel = new JLabel("Return Date (YYYY-MM-DD):");
        constraints.gridx = 0;
        constraints.gridy = 5;
        panel.add(returnDateLabel, constraints);
        returnDateField = new JTextField(20);
        constraints.gridx = 1;
        panel.add(returnDateField, constraints);
    }

    private class SearchButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            bookPage bookDialog = new bookPage((JFrame) SwingUtilities.getWindowAncestor(registerPage.this));
            bookDialog.setVisible(true);
            selectedBooks = bookDialog.getSelectedBooks();
            updateSelectedBooksTable();
        }
    }

    private class RegisterButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String customerName = customerNameField.getText();
            String customerPhoneNumber = customerPhoneNumberField.getText();
            String borrowDate = borrowDateField.getText();
            String returnDate = returnDateField.getText();

            // Validate the input fields
            if (customerName.isEmpty() || customerPhoneNumber.isEmpty() || borrowDate.isEmpty() || returnDate.isEmpty() || selectedBooks == null || selectedBooks.isEmpty()) {
                JOptionPane.showMessageDialog(registerPage.this, "Please fill in all fields and select at least one book.");
                return;
            }

            // Validate phone number (simple check)
            if (!customerPhoneNumber.matches("\\d+")) {
                JOptionPane.showMessageDialog(registerPage.this, "Phone number should contain only digits.");
                return;
            }

            // Validate dates (simple format check)
            if (!borrowDate.matches("\\d{4}-\\d{2}-\\d{2}") || !returnDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(registerPage.this, "Dates should be in the format YYYY-MM-DD.");
                return;
            }

            // Handle registration logic
            try {
                saveCustomerToDatabase(customerName, customerPhoneNumber);
                JOptionPane.showMessageDialog(registerPage.this, "Customer registered successfully.");
                // Optionally clear selected books and input fields here
                selectedBooks.clear();
                updateSelectedBooksTable();
                customerNameField.setText("");
                customerPhoneNumberField.setText("");
                borrowDateField.setText("");
                returnDateField.setText("");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(registerPage.this, "An error occurred while saving the registration.");
                ex.printStackTrace();
            }
        }
    }

    private void updateSelectedBooksTable() {
        tableModel.setRowCount(0); // Clear the existing rows
        if (selectedBooks != null) {
            for (String[] book : selectedBooks) {
                tableModel.addRow(book);
            }
        }
    }

    private void saveCustomerToDatabase(String customerName, String customerPhoneNumber) throws SQLException {
        String query = "INSERT INTO customer (customer_name, customer_phone_number) VALUES (?, ?)";
        try (Connection connection = DataBase.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, customerName);
            statement.setString(2, customerPhoneNumber);
            statement.executeUpdate();
        }
    }
}
