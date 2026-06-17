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
import java.util.List;

public class adminForm extends JFrame {

    private final int idWidth = 5;
    private final int nameWidth = 30;
    private final int authorWidth = 30;
    private final int priceWidth = 10;
    private JPanel adminPanel;
    private JPanel functionPanel;
    private JButton manageBooksButton;
    private JButton returnButton;
    private JButton manageUsersButton;
    private JButton borrowButton;
    private JButton viewIssuedBooksButton;
    private JLabel User;
    private JPanel buttonPanel;
    private JPanel adminButton;
    private JPanel bookSelectionPanel;
    private JPanel bookPanel;
    private JButton searchButton;
    private JTextField searchField;
    private JTable bookTable;
    private JButton selectButton;
    private JPanel operationPanel;
    private JButton setQTYButton;
    private JButton viewLoginButton;
    private JButton logOutButton;
    private final List<String[]> bookList = fetchBooksFromDB();

    public adminForm() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Admin Form");
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1200, 800));
        setContentPane(adminPanel);
        borrowButton.addActionListener(e -> createBorrowButton());
        returnButton.addActionListener(e -> createReturnButton());
        searchButton.addActionListener(e -> setSearchButtonAction());
        manageBooksButton.addActionListener(e -> createMageBooksButton());
        viewIssuedBooksButton.addActionListener(e -> createIssuedBooksButton());
        manageUsersButton.addActionListener(e -> createUserList());
        viewLoginButton.addActionListener(e -> createLoginList());
        logOutButton.addActionListener(e -> setLogOutButton());
        createBookTable();
        selectButton.addActionListener(e -> createSelectButton());
        setQTYButton.addActionListener(e -> handleSetQuantity());
        pack();
    }

    private void createBorrowButton() {
        operationPanel.removeAll();

        List<String[]> selectedBooks = getSelectedBooks();

        borrowForm borrowForm = new borrowForm(selectedBooks);
        borrowForm.setVisible(true);
        operationPanel.add(borrowForm, BorderLayout.CENTER);
        operationPanel.revalidate();
        operationPanel.repaint();
    }

    private void createSelectButton(){
        List<String[]> selectedBooks = getSelectedBooks();
        SelectionManager.getInstance().setSelectedBooks(selectedBooks);

    }

    private void createReturnButton() {
        operationPanel.removeAll();

        returnForm returnForm = new returnForm();
        returnForm.setVisible(true);
        operationPanel.add(returnForm, BorderLayout.CENTER);
        operationPanel.revalidate();
        operationPanel.repaint();
    }

    private void createMageBooksButton() {
        operationPanel.removeAll();

        bookListForm mageBooksForm = new bookListForm();
        mageBooksForm.setVisible(true);
        operationPanel.add(mageBooksForm, BorderLayout.CENTER);
        operationPanel.revalidate();
        operationPanel.repaint();
    }

    private void createIssuedBooksButton() {
        operationPanel.removeAll();
        viewIssuedBookForm issuedBookForm = new viewIssuedBookForm();
        issuedBookForm.setVisible(true);
        operationPanel.add(issuedBookForm, BorderLayout.CENTER);
        operationPanel.revalidate();
        operationPanel.repaint();
    }

    private void setLogOutButton() {
        int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out this user?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            this.dispose(); // Close the current window
            roleLogin.performedLogin();
        }
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
            rowData[5] = 0; // Default quantity value
            tableModel.addRow(rowData);
        }
        setColumnWidths(bookTable);
    }

    private void handleSetQuantity() {
        int[] selectedRows = bookTable.getSelectedRows();

        for (int rowIndex : selectedRows) {
            showQuantityInputDialog(rowIndex);
        }
    }

    private void showQuantityInputDialog(int rowIndex) {
        String bookId = (String) bookTable.getValueAt(rowIndex, 1); // Get the book ID
        String bookName = (String) bookTable.getValueAt(rowIndex, 2); // Get the book Name

        String input = JOptionPane.showInputDialog(this, "Enter quantity for " + bookName + " (Book ID: " + bookId + "):");
        if (input != null && !input.trim().isEmpty()) {
            try {
                int quantity = Integer.parseInt(input.trim());
                if (quantity > 0) {
                    bookTable.setValueAt(quantity, rowIndex, 5); // Update the Quantity column
                } else {
                    JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid quantity entered. Please enter a numeric value.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void createUserList(){
        operationPanel.removeAll();
        viewUserForm userForm = new viewUserForm();
        userForm.setVisible(true);
        operationPanel.add(userForm, BorderLayout.CENTER);
        operationPanel.revalidate();
        operationPanel.repaint();

    }

    private void createLoginList(){
        operationPanel.removeAll();

        viewLogForm loginForm = new viewLogForm();
        loginForm.setVisible(true);
        operationPanel.add(loginForm, BorderLayout.CENTER);
        operationPanel.revalidate();
        operationPanel.repaint();
    }

    private List<String[]> getSelectedBooks() {
        List<String[]> selectedBooks = new ArrayList<>();
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();

        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean isSelected = (Boolean) model.getValueAt(i, 0); // Get value of the "Select" column

            if (isSelected != null) {
                String id = (String) model.getValueAt(i, 1);
                String name = (String) model.getValueAt(i, 2);
                String author = (String) model.getValueAt(i, 3);
                String price = (String) model.getValueAt(i, 4);
                Integer quantity = (Integer) model.getValueAt(i, 5); // Accessing column index 5

                if (isSelected) {
                    selectedBooks.add(new String[]{id, name, author, price, quantity.toString()});
                } else {
                    // Reset quantity to 0 for unselected books
                    bookTable.setValueAt(0, i, 5); // Update the Quantity column
                }
            }
        }
        return selectedBooks;
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
        String[] columnNames = {"Select", "ID", "Name", "Author", "Price", "QTY"};

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
            rowData[book.length + 1] = 0; // Default quantity value
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

        TableColumn authorColumn = table.getColumnModel().getColumn(3);
        authorColumn.setPreferredWidth(authorWidth * 10);

        TableColumn priceColumn = table.getColumnModel().getColumn(4);
        priceColumn.setPreferredWidth(priceWidth * 10);

        TableColumn quantityColumn = table.getColumnModel().getColumn(5);
        quantityColumn.setPreferredWidth(priceWidth * 10);
    }
}
