package forms;

import cls.user;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.CookieHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class returnForm extends JPanel{
    private JPanel returnPanel;
    private JPanel inputPanel;
    private JTextArea nameArea;
    private JTextField searchField;
    private JButton searchButton;
    private JButton returnButton;
    private JButton cancelButton;
    private JPanel bookPanel;
    private JPanel borrowDatePanel;
    private JPanel returnDatePanel;
    private JPanel returnedDatePanel;
    private JTextArea totalArea;
    private JTextField feeField;
    private JTextArea totalFeeArea;
    private JTable bookTable;
    private JTextArea borrowIDArea;
    private JTextArea borrowArea;
    private JTextArea returnArea;
    private final List<String[]> books = fetchList();

    public returnForm(){
        add(returnPanel);
        returnPanel.setSize(new Dimension(500, 800));
        returnedDatePicker();
        createGUIBook();
        searchButton.addActionListener(new SetSearchButtonListener());
        returnButton.addActionListener(new SetReturnButton());
        cancelButton.addActionListener(new CancelButtonListener());
    }

    private void getTotalFee() {
        double totalFee = 0.0;
        String feeText = feeField.getText().trim();

        // Validate fee input
        if (feeText.isEmpty()) {
            JOptionPane.showMessageDialog(returnedDatePanel,
                    "Fee field cannot be empty",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        double fee = 0.0;
        try {
            fee = Double.parseDouble(feeText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(returnedDatePanel,
                    "Invalid fee format",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        String query = "SELECT total_price FROM list WHERE id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, searchField.getText().trim());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    totalFee = rs.getDouble("total_price") + fee;
                    totalFeeArea.setText(String.format("%.2f", totalFee));
                } else {
                    // If no result found, assume totalFee remains zero
                    totalFeeArea.setText(String.format("%.2f", totalFee));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(returnedDatePanel,
                    "Database error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    private class SetReturnButton implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String id = searchField.getText().trim();
            LocalDate returnedDate = getDateFromPicker(returnedDatePanel);
            getTotalFee();
            String fee = feeField.getText().trim();
            String totalFee = totalFeeArea.getText().trim();

            // Validate inputs
            if (id.isEmpty() || fee.isEmpty() || returnedDate == null || totalFee.isEmpty()) {
                JOptionPane.showMessageDialog(returnedDatePanel,
                        "Please fill all the fields",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedReturnedDate = returnedDate.format(formatter);
            int userID = 0;

            // Query to fetch user ID
            String userQuery = "SELECT id FROM user WHERE username = ?";
            // Query to update list record
            String listQuery = "UPDATE list SET status_id = ?, returned_date = ?, late_fee = ?, total_price = ?, user_shift_id = ? WHERE id = ?";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement userStatement = connection.prepareStatement(userQuery);
                 PreparedStatement listStatement = connection.prepareStatement(listQuery)) {

                // Fetch user ID
                userStatement.setString(1, user.getUserName());
                try (ResultSet rs = userStatement.executeQuery()) {
                    if (rs.next()) {
                        userID = rs.getInt("id");
                    } else {
                        JOptionPane.showMessageDialog(returnedDatePanel,
                                "User not found",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // Update list record
                listStatement.setInt(1, 2); // Assuming status_id = 2 means returned
                listStatement.setString(2, formattedReturnedDate);
                listStatement.setString(3, fee);
                listStatement.setString(4, totalFee);
                listStatement.setInt(5, userID);
                listStatement.setString(6, id); // Update the record with this ID

                int rowsAffected = listStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(returnedDatePanel,
                            "Return processed successfully",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(returnedDatePanel,
                            "No record found with the given ID",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(returnedDatePanel,
                        "Database error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class CancelButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Clear text fields
            borrowIDArea.setText("");
            nameArea.setText("");
            borrowArea.setText("");
            returnArea.setText("");
            totalArea.setText("");
            feeField.setText("");
            totalFeeArea.setText("");

            // Clear date pickers
            for (Component component : returnedDatePanel.getComponents()) {
                if (component instanceof DatePicker) {
                    ((DatePicker) component).clear();
                }
            }
            // Clear book table
            DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
            model.setRowCount(0);
        }
    }

    private class SetSearchButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String searchText = searchField.getText().toLowerCase();
            List<String[]> filteredList = getFilteredList(searchText);

            DefaultTableModel tableModel = (DefaultTableModel) bookTable.getModel();
            tableModel.setRowCount(0); // Clear existing rows

            for (String[] book : filteredList) {
                // Update text fields with values from filteredList
                nameArea.setText(book[1]);
                borrowIDArea.setText(book[0]);
                totalArea.setText("$" + book[5]);
                borrowArea.setText(book[3]);
                returnArea.setText(book[4]);

                // Assuming book[2] is the book_id_list which might need to be split or processed
                String bookIdList = book[2];
                // Assuming bookIdList contains comma-separated IDs; this may vary based on your data
                for (String bookId : bookIdList.split(",")) {
                    try {
                        List<String[]> filteredBooks = fetchIssuedBook(Integer.parseInt(bookId.trim()));
                        for (String[] arg : filteredBooks) {
                            Object[] rowData = new Object[3];
                            rowData[0] = arg[1];
                            rowData[1] = arg[2];
                            rowData[2] = arg[3];
                            tableModel.addRow(rowData);
                        }
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private List<String[]> fetchIssuedBook(int issuedID) {
        List<String[]> bookList = new ArrayList<>();
        String query = "SELECT i.issued_book_id, b.book_name, i.qty, b.book_price " +
                "FROM book_issued_list i " +
                "JOIN book b ON i.book_id = b.id " +
                "WHERE i.issued_book_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, issuedID); // Use setInt for integer parameters
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String[] book = {
                            rs.getString("issued_book_id"),
                            rs.getString("book_name"),
                            rs.getString("qty"),
                            rs.getString("book_price") // Convert double to String
                    };
                    bookList.add(book); // Add the book to the list
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookList;
    }

    private List<String[]> fetchList() {
        List<String[]> books = new ArrayList<>();
        String query = "SELECT l.id, c.customer_name, l.book_id_list, l.borrow_date, l.return_date, l.total_price " +
                "FROM list l " +
                "JOIN customer c ON l.customer_id = c.id ";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String[] book = {
                            resultSet.getString("id"),
                            resultSet.getString("customer_name"),
                            resultSet.getString("book_id_list"),
                            resultSet.getString("borrow_date"),
                            resultSet.getString("return_date"),
                            resultSet.getString("total_price"),
                    };
                    books.add(book);
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    private List<String[]> getFilteredList(String search) {
        List<String[]> filteredList = new ArrayList<>();

        for (String[] book : books) {
            if (book[0].toLowerCase().contains(search)) {
                filteredList.add(book);
            } else if (book[1].toLowerCase().contains(search)) {
                filteredList.add(book);
            }
        }
        return filteredList;
    }

    private void createGUIBook() {
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

        bookTable.setModel(tableModel);
        bookTable.setFillsViewportHeight(true);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(bookTable);
        bookPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private LocalDate getDateFromPicker(JPanel panel) {
        for (Component component : panel.getComponents()) {
            if (component instanceof DatePicker) {
                return ((DatePicker) component).getDate();
            }
        }
        return null;
    }

    private void returnedDatePicker() {
        DatePickerSettings datePickerSettings = new DatePickerSettings();
        datePickerSettings.setFormatForDatesBeforeCommonEra("yyyy-MM-dd");
        DatePicker returnedDatePicker = new DatePicker(datePickerSettings);
        Calendar now = Calendar.getInstance();
        returnedDatePicker.setDate(now.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        returnedDatePanel.add(returnedDatePicker);
    }
}
