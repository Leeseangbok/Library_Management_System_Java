package src.main.java.com.mainInterface;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import src.main.java.com.database.DBconnection;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class bookPage extends JDialog {
    private int idWidth = 5;
    private int nameWidth = 30;
    private int authorWidth = 30;
    private int priceWidth = 10;
    private List<String[]> selectedBooks = new ArrayList<>();
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public bookPage(JFrame parentFrame) {
        super(parentFrame, "Book Selection", true);
        setLayout(new BorderLayout());
        setSize(800, 600);
        setLocationRelativeTo(parentFrame);

        // Fetch books from the database
        List<String[]> books = fetchBookListFromDatabase();
        String[] columnNames = {"ID", "Name", "Author", "Price"};

        // Create table model and add rows
        tableModel = new DefaultTableModel(columnNames, 0);
        for (String[] book : books) {
            tableModel.addRow(book);
        }

        // Create and set up the book table
        bookTable = new JTable(tableModel);
        bookTable.setFillsViewportHeight(true);
        bookTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // Allow multiple row selection
        setColumnWidths(bookTable);

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Done button
        JButton doneButton = new JButton("Done");
        doneButton.addActionListener(e -> handleDoneAction());

        // Select Book button
        JButton selectBookButton = new JButton("Select Book");
        selectBookButton.addActionListener(e -> handleSelectBookAction());

        // Search button action
        searchButton.addActionListener(e -> handleSearchAction());

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(doneButton);
        buttonPanel.add(selectBookButton);

        // Add components to the dialog
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(bookTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Getter for selected books
    public List<String[]> getSelectedBooks() {
        return selectedBooks;
    }

    // Set column widths for the table
    private void setColumnWidths(JTable table) {
        TableColumn idColumn = table.getColumnModel().getColumn(0);
        idColumn.setPreferredWidth(idWidth * 5);

        TableColumn nameColumn = table.getColumnModel().getColumn(1);
        nameColumn.setPreferredWidth(nameWidth * 10);

        TableColumn authorColumn = table.getColumnModel().getColumn(2);
        authorColumn.setPreferredWidth(authorWidth * 10);

        TableColumn priceColumn = table.getColumnModel().getColumn(3);
        priceColumn.setPreferredWidth(priceWidth * 10);
    }

    // Fetch book list from the database
    private List<String[]> fetchBookListFromDatabase() {
        List<String[]> bookList = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM book");
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String[] book = {
                    rs.getString("id"),
                    rs.getString("book_name"),
                    rs.getString("book_author"),
                    rs.getString("book_price")
                };
                bookList.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookList;
    }

    // Handle the "Done" button action
    private void handleDoneAction() {
        int[] selectedRows = bookTable.getSelectedRows();
        selectedBooks.clear();
        for (int row : selectedRows) {
            selectedBooks.add(new String[]{
                (String) bookTable.getValueAt(row, 0),
                (String) bookTable.getValueAt(row, 1),
                (String) bookTable.getValueAt(row, 2),
                (String) bookTable.getValueAt(row, 3)
            });
        }
        dispose();
    }

    // Handle the "Select Book" button action
    private void handleSelectBookAction() {
        int[] selectedRows = bookTable.getSelectedRows();
        if (selectedRows.length > 0) {
            StringBuilder selectedBooksInfo = new StringBuilder("Selected Books:\n");
            for (int row : selectedRows) {
                String bookId = (String) bookTable.getValueAt(row, 0);
                String bookName = (String) bookTable.getValueAt(row, 1);
                String bookAuthor = (String) bookTable.getValueAt(row, 2);
                String bookPrice = (String) bookTable.getValueAt(row, 3);
                selectedBooksInfo.append(bookId).append(" - ").append(bookName).append(" - ").append(bookAuthor).append(" - ").append(bookPrice).append("\n");
            }
            JOptionPane.showMessageDialog(this, selectedBooksInfo.toString());
        } else {
            JOptionPane.showMessageDialog(this, "No book selected");
        }
    }

    // Handle the search action
    private void handleSearchAction() {
        String searchText = searchField.getText().toLowerCase();
        List<String[]> books = fetchBookListFromDatabase();
        String[] columnNames = {"ID", "Name", "Author", "Price"};
        DefaultTableModel filteredModel = new DefaultTableModel(columnNames, 0);

        for (String[] book : books) {
            if (book[1].toLowerCase().contains(searchText)) {
                filteredModel.addRow(book);
            }
        }
        bookTable.setModel(filteredModel);
    }
}
