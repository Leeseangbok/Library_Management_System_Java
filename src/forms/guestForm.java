package forms;

import cls.roleLogin;
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
import java.util.Arrays;
import java.util.List;

public class guestForm extends JFrame {

    private static final int ID_WIDTH = 75;
    private static final int NAME_WIDTH = 300;
    private static final int AUTHOR_WIDTH = 300;
    private static final int PRICE_WIDTH = 100;

    private JPanel guestPanel;
    private JTextField searchField;
    private JButton searchButton;
    private JTable table1;
    private JPanel tablePanel;
    private JButton logOutButton;
    private final List<String[]> bookList = fetchBooksFromDB();

    public guestForm() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Guest Form");
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1200, 800));
        setContentPane(guestPanel);
        createBookTable();
        searchButton.addActionListener(e -> handleSearch());
        logOutButton.addActionListener(e -> handleLogout());
        pack();
    }

    private void handleLogout() {
        int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            dispose(); // Close the current window
            roleLogin.performedLogin();
        }
    }

    private List<String[]> fetchBooksFromDB() {
        List<String[]> books = new ArrayList<>();
        String query = "SELECT id, book_name, book_author, CONCAT('$', FORMAT(book_price, 2)) AS book_price FROM book";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String[] book = {
                        resultSet.getString("id"),
                        resultSet.getString("book_name"),
                        resultSet.getString("book_author"),
                        resultSet.getString("book_price")
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

        for (String[] book : bookList) {
            if (Arrays.stream(book).anyMatch(field -> field.toLowerCase().contains(search))) {
                filteredList.add(book);
            }
        }
        return filteredList;
    }

    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        List<String[]> filteredBooks = getFilteredList(searchText);

        DefaultTableModel tableModel = (DefaultTableModel) table1.getModel();
        tableModel.setRowCount(0); // Clear existing rows

        for (String[] book : filteredBooks) {
            tableModel.addRow(book);
        }
        adjustColumnWidths();
    }

    private void createBookTable() {
        String[] columnNames = {"ID", "Name", "Author", "Price"};

        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };

        for (String[] book : bookList) {
            tableModel.addRow(book);
        }

        table1.setModel(tableModel);
        table1.setFillsViewportHeight(true);
        adjustColumnWidths();

        JScrollPane scrollPane = new JScrollPane(table1);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void adjustColumnWidths() {
        TableColumn idColumn = table1.getColumnModel().getColumn(0);
        idColumn.setPreferredWidth(ID_WIDTH);

        TableColumn nameColumn = table1.getColumnModel().getColumn(1);
        nameColumn.setPreferredWidth(NAME_WIDTH);

        TableColumn authorColumn = table1.getColumnModel().getColumn(2);
        authorColumn.setPreferredWidth(AUTHOR_WIDTH);

        TableColumn priceColumn = table1.getColumnModel().getColumn(3);
        priceColumn.setPreferredWidth(PRICE_WIDTH);
    }
}
