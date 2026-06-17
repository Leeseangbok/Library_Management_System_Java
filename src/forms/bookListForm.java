package forms;

import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class bookListForm extends JPanel {

    private final int idWidth = 5;
    private final int nameWidth = 30;
    private final int authorWidth = 30;
    private final int priceWidth = 10;
    private JPanel bookListPanel;
    private JTextField searchField;
    private JButton searchButton;
    private JButton deleteButton;
    private JButton addButton;
    private JButton updateButton;
    private JTable bookTable;
    private JPanel bookPanel;
    private JPanel panel;

    private List<String[]> bookList = fetchBooksFromDB();

    public bookListForm() {
        add(bookListPanel);
        createBookTable();
        searchButton.addActionListener(e -> setSearchButtonAction());
        addButton.addActionListener(e -> setAddButtonAction());
        updateButton.addActionListener(e -> setEditButtonAction());
        deleteButton.addActionListener(e -> setDeleteButtonAction());
    }

    private void setSearchButtonAction() {
        String searchText = searchField.getText().toLowerCase();
        List<String[]> filteredBooks = fetchFilteredBooks(searchText);

        DefaultTableModel tableModel = (DefaultTableModel) bookTable.getModel();
        tableModel.setRowCount(0); // Clear existing rows

        for (String[] book : filteredBooks) {
            Object[] rowData = new Object[6]; // "Select", "ID", "Name", "Author", "Price", "Quantity"
            rowData[0] = Boolean.FALSE;
            System.arraycopy(book, 0, rowData, 1, book.length);
            tableModel.addRow(rowData);
        }
        setColumnWidths(bookTable);
    }

    private void setAddButtonAction() {
        crudBookForm addBookForm = new crudBookForm("add", null);
        addBookForm.setVisible(true);
        addBookForm.setLocationRelativeTo(null);
        addBookForm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                refreshBookList();
            }
        });
    }

    private void setEditButtonAction() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow != -1) {
            String[] bookData = new String[bookTable.getColumnCount() - 1];
            for (int i = 1; i < bookTable.getColumnCount(); i++) {
                bookData[i - 1] = bookTable.getValueAt(selectedRow, i).toString();
            }
            crudBookForm editBookForm = new crudBookForm("edit", bookData);
            editBookForm.setLocationRelativeTo(null);
            editBookForm.setVisible(true);

            editBookForm.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    refreshBookList();
                }
            });
        } else {
            JOptionPane.showMessageDialog(this, "Please select a book to edit", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void setDeleteButtonAction() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow != -1) {
            String bookId = bookTable.getValueAt(selectedRow, 1).toString(); // Assuming ID is the second column

            int option = JOptionPane.showConfirmDialog(bookListForm.this, "Are you sure you want to delete this book?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                try (Connection connection = DBConnection.getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM book WHERE id = ?")) {

                    preparedStatement.setString(1, bookId);
                    preparedStatement.executeUpdate();

                    JOptionPane.showMessageDialog(bookListForm.this, "Book deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshBookList();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(bookListForm.this, "Error deleting book from database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(bookListForm.this, "Please select a book to delete", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshBookList() {
        bookList = fetchBooksFromDB();
        DefaultTableModel tableModel = (DefaultTableModel) bookTable.getModel();
        tableModel.setRowCount(0); // Clear existing rows

        for (String[] book : bookList) {
            Object[] rowData = new Object[6]; // "Select", "ID", "Name", "QTY", "Author", "Price"
            rowData[0] = Boolean.FALSE;
            System.arraycopy(book, 0, rowData, 1, book.length);
            tableModel.addRow(rowData);
        }
        setColumnWidths(bookTable);
    }

    private List<String[]> fetchBooksFromDB() {
        List<String[]> books = new ArrayList<>();
        String query = "SELECT id, book_name, book_qty, book_author, CONCAT('$', FORMAT(book_price, 2)) AS book_price FROM book";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String[] book = {
                        resultSet.getString("id"),
                        resultSet.getString("book_name"),
                        resultSet.getString("book_qty"),
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

    private List<String[]> fetchFilteredBooks(String searchText) {
        List<String[]> books = new ArrayList<>();

        for (String[] book : fetchBooksFromDB()) {
            if (book[0].toLowerCase().contains(searchText)) {
                books.add(book);
            } else if (book[1].toLowerCase().contains(searchText)) {
                books.add(book);
            }
        }
        return books;
    }

    private void createBookTable() {
        String[] columnNames = {"Select", "ID", "Name", "QTY", "Author", "Price"};

        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // Only the "Select" column is editable
            }
        };

        for (String[] book : bookList) {
            Object[] rowData = new Object[columnNames.length];
            rowData[0] = Boolean.FALSE; // Select column
            System.arraycopy(book, 0, rowData, 1, book.length); // Copy book details
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
        TableColumn selectColumn = table.getColumnModel().getColumn(0);
        selectColumn.setPreferredWidth(idWidth * 15);

        TableColumn idColumn = table.getColumnModel().getColumn(1);
        idColumn.setPreferredWidth(idWidth * 15);

        TableColumn nameColumn = table.getColumnModel().getColumn(2);
        nameColumn.setPreferredWidth(nameWidth * 10);

        TableColumn authorColumn = table.getColumnModel().getColumn(4);
        authorColumn.setPreferredWidth(authorWidth * 10);

        TableColumn priceColumn = table.getColumnModel().getColumn(5);
        priceColumn.setPreferredWidth(priceWidth * 10);

        TableColumn quantityColumn = table.getColumnModel().getColumn(3);
        quantityColumn.setPreferredWidth(priceWidth * 10);
    }

}
