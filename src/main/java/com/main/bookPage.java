package src.main.java.com.mainInterface;


import src.main.java.com.database.DataBase;

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

public class bookPage {

    private int idWidth = 5;
    private int nameWidth = 30;
    private int authorWidth = 30;
    private int priceWidth = 10;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new bookPage().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Book List Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Fetch book list from the database
        List<String[]> books = fetchBookListFromDatabase();

        // Create column names
        String[] columnNames = {"ID", "Name", "Author", "Price"};

        // Create the table model
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        for (String[] book : books) {
            tableModel.addRow(book);
        }

        JTable bookTable = new JTable(tableModel);
        bookTable.setFillsViewportHeight(true);

        TableColumn idColumn = bookTable.getColumnModel().getColumn(0);
        idColumn.setPreferredWidth(idWidth * 5);

        TableColumn nameColumn = bookTable.getColumnModel().getColumn(1);
        nameColumn.setPreferredWidth(nameWidth * 10);

        TableColumn authorColumn = bookTable.getColumnModel().getColumn(2);
        authorColumn.setPreferredWidth(authorWidth * 10);

        TableColumn priceColumn = bookTable.getColumnModel().getColumn(3);
        priceColumn.setPreferredWidth(priceWidth * 10);

        JPanel searchPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        JButton doneButton = new JButton("Done");
        doneButton.addActionListener(e -> {
            int[] selectedRows = bookTable.getSelectedRows();
            List<String> selectedBooks = new ArrayList<>();
            for (int row : selectedRows) {
                String bookId = (String) bookTable.getValueAt(row, 0);
                String bookName = (String) bookTable.getValueAt(row, 1);
                String bookAuthor = (String) bookTable.getValueAt(row, 2);
                String bookPrice = (String) bookTable.getValueAt(row, 3);
                selectedBooks.add(String.format("%s - %s - %s - %s", bookId, bookName, bookAuthor, bookPrice));
            }
            JOptionPane.showMessageDialog(frame, "Selected Books: " + String.join(", ", selectedBooks));
        });

        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().toLowerCase();
            DefaultTableModel filteredModel = new DefaultTableModel(columnNames, 0);
            for (String[] book : fetchBookListFromDatabase()) {
                if (book[1].toLowerCase().contains(searchText)) {
                    filteredModel.addRow(book);
                }
            }
            bookTable.setModel(filteredModel);
        });

        frame.add(searchPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(bookTable), BorderLayout.CENTER);
        frame.add(doneButton, BorderLayout.SOUTH);

        frame.setSize(800, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private List<String[]> fetchBookListFromDatabase() {
        List<String[]> books = new ArrayList<>();
        String query = "SELECT id, book_name, book_price, book_author FROM book";

        try (Connection connection = DataBase.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                String bookId = String.valueOf(rs.getInt("id"));
                String bookName = rs.getString("book_name");
                String bookAuthor = rs.getString("book_author");
                String bookPrice = String.format("$%.2f", rs.getDouble("book_price"));

                books.add(new String[]{bookId, bookName, bookAuthor, bookPrice});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching book list: " + e.getMessage());
        }

        return books;
    }
}
