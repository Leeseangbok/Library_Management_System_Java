package forms;

import cls.user;
import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DatePicker;

public class borrowForm extends JPanel {
    private JPanel borrowPanel;
    private JPanel InputPanel;
    private JTextArea totalArea;
    private JButton cancelButton;
    private JTextField customerNameField;
    private JTextField phoneField;
    private JTable bookTable;
    private JButton registerButton;
    private JPanel bookPanel;
    private JPanel borrowDatePanel;
    private JPanel returnDatePanel;
    private JTextArea borrowIdAREA;
    private List<String[]> selectedBooks;

    public borrowForm(List<String[]> selectedBooks) {
        this.selectedBooks = selectedBooks;
        add(borrowPanel);
        borrowDatePicker();
        returnDatePicker();
        createGUISelectedBook();
        registerButton.addActionListener(new RegisterButtonListener());
        cancelButton.addActionListener(new CancelButtonListener());
        SelectionManager.getInstance().addSelectionListener(this::updateBookSelection);

    }

    private void updateBookSelection(List<String[]> books) {
        this.selectedBooks = books;
        refreshTable();
        getTotalPrice(); // Optionally update the total price
    }

    private void refreshTable() {
        DefaultTableModel tableModel = (DefaultTableModel) bookTable.getModel();
        tableModel.setRowCount(0); // Clear existing rows

        for (String[] book : selectedBooks) {
            // Create an object array with book details
            Object[] rowData = {
                    book[1], // Name
                    book[4], // Quantity
                    book[3]  // Price
            };
            tableModel.addRow(rowData);
        }
    }

    private class RegisterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String customerName = customerNameField.getText();
            String phone = phoneField.getText();
            LocalDate borrowDate = getDateFromPicker(borrowDatePanel);
            LocalDate returnDate = getDateFromPicker(returnDatePanel);
            int userID = 0; // Assuming user.getRoleID() provides the role ID
            int id = 0;

            if (customerName.isEmpty() || phone.isEmpty() || borrowDate == null || returnDate == null) {
                JOptionPane.showMessageDialog(borrowPanel, "Please fill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedBorrowDate = borrowDate.format(formatter);
            String formattedReturnDate = returnDate.format(formatter);

            Connection conn = null;
            PreparedStatement customerStatement = null;
            PreparedStatement userStatement = null;
            PreparedStatement listStatement = null;
            PreparedStatement issuedBookStatement = null;

            try {
                conn = DBConnection.getConnection();

                String userQuery = "SELECT id FROM user WHERE username = ?";
                userStatement = conn.prepareStatement(userQuery);
                userStatement.setString(1, user.getUserName());
                ResultSet rs = userStatement.executeQuery();
                if (rs.next()) {
                    userID = rs.getInt("id");
                }

                // Insert customer
                String customersQuery = "INSERT INTO customer (customer_name, phone_number) VALUES (?, ?)";
                customerStatement = conn.prepareStatement(customersQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                customerStatement.setString(1, customerName);
                customerStatement.setString(2, phone);
                customerStatement.executeUpdate();
                ResultSet generatedKeys = customerStatement.getGeneratedKeys();
                int customerId = 0;
                if (generatedKeys.next()) {
                    customerId = generatedKeys.getInt(1);
                }

                // Prepare the SQL query for insertion
                String issuedBookQuery = "INSERT INTO book_issued_list(book_id, customer_id, issued_book_id, qty) VALUES(?, ?, ?, ?)";
                issuedBookStatement = conn.prepareStatement(issuedBookQuery, PreparedStatement.RETURN_GENERATED_KEYS);

                // Fetch the last issued_book_id to start incrementing from there
                String lastIdQuery = "SELECT MAX(issued_book_id) FROM book_issued_list";
                Statement statement = conn.createStatement();
                ResultSet resultSet = statement.executeQuery(lastIdQuery);

                int lastId = 0; // Default to 0 if no records exist
                if (resultSet.next()) {
                    lastId = resultSet.getInt(1);
                }

                // Start incrementing from the lastId + 1
                int issuedBookId = lastId + 1;

                for (String[] book : selectedBooks) {
                    issuedBookStatement.setString(1, book[0]);
                    issuedBookStatement.setInt(2, customerId);
                    issuedBookStatement.setInt(3, issuedBookId);
                    issuedBookStatement.setString(4, book[4]);
                    issuedBookStatement.addBatch();
                }

                // Execute the batch
                issuedBookStatement.executeBatch();
                ResultSet rs1 = issuedBookStatement.getGeneratedKeys();
                if (rs1.next()) {
                    id = rs1.getInt(1);
                }

                String listQuery ="INSERT INTO list (customer_id, book_id_list, qty, status_id, borrow_date, return_date, total_price, user_shift_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                listStatement = conn.prepareStatement(listQuery, Statement.RETURN_GENERATED_KEYS);
                listStatement.setInt(1, customerId);
                listStatement.setInt(2, issuedBookId); // Assuming book_id_list refers to the count of books
                listStatement.setInt(3, getTotalQTY());
                listStatement.setInt(4, 1); // Assuming statusID is 1
                listStatement.setString(5, formattedBorrowDate);
                listStatement.setString(6, formattedReturnDate);
                listStatement.setDouble(7, getTotalPrice());
                listStatement.setInt(8, userID);
                listStatement.executeUpdate();

                JOptionPane.showMessageDialog(borrowPanel, "Record inserted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

                // Get the generated list id
                ResultSet listKeys = listStatement.getGeneratedKeys();
                if (listKeys.next()) {
                    int listId = listKeys.getInt(1);
                    borrowIdAREA.setText(String.valueOf(listId));
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            } finally {
                if (customerStatement != null) try { customerStatement.close(); } catch (SQLException e1) { e1.printStackTrace(); }
                if (listStatement != null) try { listStatement.close(); } catch (SQLException e3) { e3.printStackTrace(); }
                if (conn != null) try { conn.close(); } catch (SQLException e4) { e4.printStackTrace(); }
            }
        }
    }

    private class CancelButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Clear text fields
            customerNameField.setText("");
            phoneField.setText("");
            totalArea.setText("");

            // Clear date pickers
            for (Component component : borrowDatePanel.getComponents()) {
                if (component instanceof DatePicker) {
                    ((DatePicker) component).clear();
                }
            }
            for (Component component : returnDatePanel.getComponents()) {
                if (component instanceof DatePicker) {
                    ((DatePicker) component).clear();
                }
            }

            // Clear book table
            DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
            model.setRowCount(0);
        }
    }

    private int getTotalQTY() {
        int totalQty = 0;
        for (String[] selectedBook : selectedBooks) {
            totalQty += Integer.parseInt(selectedBook[4]);
        }
        return totalQty;
    }

    private Double getTotalPrice() {
        double totalPrice = 0.0;
        for (String[] selectedBook : selectedBooks) {
            // Assuming selectedBook[3] is the price and selectedBook[4] is the quantity
            String priceStr = selectedBook[3].replace("$", "").trim();
            String quantityStr = selectedBook[4].trim();
            try {
                double price = Double.parseDouble(priceStr);
                int quantity = Integer.parseInt(quantityStr);
                totalPrice += price * quantity;
            } catch (NumberFormatException e) {
                System.err.println("Error parsing number: " + e.getMessage());
            }
        }
        totalArea.setText("$" + totalPrice);
        return totalPrice;
    }

    private void borrowDatePicker() {
        DatePickerSettings datePickerSettings = new DatePickerSettings();
        datePickerSettings.setFormatForDatesBeforeCommonEra("yyyy-MM-dd");
        DatePicker borrowDatePicker = new DatePicker(datePickerSettings);
        Calendar now = Calendar.getInstance();
        borrowDatePicker.setDate(now.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        borrowDatePanel.add(borrowDatePicker);
    }

    private void returnDatePicker() {
        DatePickerSettings datePickerSettings = new DatePickerSettings();
        datePickerSettings.setFormatForDatesBeforeCommonEra("yyyy-MM-dd");
        DatePicker returnDatePicker = new DatePicker(datePickerSettings);
        Calendar now = Calendar.getInstance();
        now.add(Calendar.WEEK_OF_MONTH, 1);
        returnDatePicker.setDate(now.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        returnDatePanel.add(returnDatePicker);
    }

    private LocalDate getDateFromPicker(JPanel panel) {
        for (Component component : panel.getComponents()) {
            if (component instanceof DatePicker) {
                return ((DatePicker) component).getDate();
            }
        }
        return null;
    }

    private void createGUISelectedBook() {
        String[] columnNames = {"Name", "QTY", "Price"};

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

        // Populate the table with the selected books
        for (String[] book : selectedBooks) {
            Object[] rowData = {book[1], book[4], book[3]}; // Name, Quantity, and Price columns
            tableModel.addRow(rowData);
        }

        bookTable.setModel(tableModel);
        bookTable.setFillsViewportHeight(true);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setColumnWidths(bookTable);

        JScrollPane scrollPane = new JScrollPane(bookTable);
        bookPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void setColumnWidths(JTable table) {
        TableColumn nameColumn = table.getColumnModel().getColumn(0);
        nameColumn.setPreferredWidth(20 * 15);

        TableColumn qtyColumn = table.getColumnModel().getColumn(1);
        qtyColumn.setPreferredWidth(10 * 10);

        TableColumn priceColumn = table.getColumnModel().getColumn(2);
        priceColumn.setPreferredWidth(10 * 10);
    }
}
