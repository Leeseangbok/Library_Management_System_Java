package forms;

import cls.KeyValue;
import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class viewIssuedBookForm extends JPanel {
    private JPanel issuedBookPanel;
    private JPanel tablePanel;
    private JTable listTable;
    private JComboBox<KeyValue> cate;
    private JPanel viewPanel;
    private JTextField searchField;
    private JButton searchButton;
    private JTextArea customrAll;
    private JTextArea priceAll;
    private final List<String[]> lists;

    public viewIssuedBookForm() {
        this.lists = fetchList();
        add(issuedBookPanel);
        createBookTable();
        initializeStatus();
        searchButton.addActionListener(e -> setSearchButtonAction());
        cate.addActionListener(e -> cateGetList());
    }

    private void cateGetList() {
        double totalPrice = 0.0;
        int totalCate = 0;
        KeyValue selectedCate = (KeyValue) cate.getSelectedItem();
        String cateValue = selectedCate != null ? String.valueOf(selectedCate.getKey()) : "0";

        DefaultTableModel tableModel = (DefaultTableModel) listTable.getModel();
        tableModel.setRowCount(0);

        for (String[] book : lists) {
            if ("0".equals(cateValue) || book[4].equalsIgnoreCase(selectedCate.getValue())) {
                tableModel.addRow(book);
                totalPrice += Double.parseDouble(book[8].replace("$", "").trim());
                totalCate++;
            }
        }
        setColumnWidths(listTable);
        priceAll.setText("$" + String.format("%.2f", totalPrice));
        customrAll.setText(String.valueOf(totalCate));
    }


    private void setSearchButtonAction() {
        String searchText = searchField.getText().toLowerCase();
        List<String[]> filteredBooks = getFilteredList(searchText);

        DefaultTableModel tableModel = (DefaultTableModel) listTable.getModel();
        tableModel.setRowCount(0); // Clear existing rows

        for (String[] book : filteredBooks) {
            tableModel.addRow(book);
        }
        setColumnWidths(listTable);
    }

    private List<String[]> fetchList() {
        List<String[]> list = new ArrayList<>();
        String query = "SELECT l.id, c.customer_name, GROUP_CONCAT(i.book_id) AS book_ids, l.qty, s.status_name, " +
                "l.borrow_date, l.return_date, CONCAT('$', Format(l.late_fee, 2)) AS late_fee, CONCAT('$', Format(l.total_price, 2)) AS total_price, u.username " +
                "FROM list l " +
                "JOIN customer c ON l.customer_id = c.id " +
                "JOIN book_issued_list i ON l.book_id_list = i.issued_book_id " +
                "JOIN user u ON l.user_shift_id = u.id " +
                "JOIN status s ON l.status_id = s.id " +
                "GROUP BY l.id, c.customer_name, l.qty, s.status_name, l.borrow_date, l.return_date, l.total_price, u.username";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String[] book = {
                        rs.getString("id"),
                        rs.getString("customer_name"),
                        rs.getString("book_ids"), // Concatenated book IDs
                        rs.getString("qty"),
                        rs.getString("status_name"),
                        rs.getString("borrow_date"),
                        rs.getString("return_date"),
                        rs.getString("late_fee"),
                        rs.getString("total_price"),
                        rs.getString("username")
                };
                list.add(book);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching data from database.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return list;
    }

    private List<String[]> getFilteredList(String search) {
        List<String[]> filteredList = new ArrayList<>();

        for (String[] book : lists) {
            if (book[0].toLowerCase().contains(search)) {
                filteredList.add(book);
            } else if (book[1].toLowerCase().contains(search)) {
                filteredList.add(book);
            }
        }
        return filteredList;
    }

    private void createBookTable() {
        double totalPrice = 0.0;
        int totalCus = 0;
        String[] columnNames = {"Borrow ID", "Customer Name", "Book ID", "QTY", "Status", "Borrow Date", "Return Date", "Late Fee", "Total Fee", "Shift"};

        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }
        };

        for (String[] book : lists) {
            tableModel.addRow(book);
            totalPrice += Double.parseDouble(book[8].replace("$", "").trim());
            totalCus ++;
        }

        priceAll.setText("$" + String.format("%.2f", totalPrice));
        customrAll.setText(String.valueOf(totalCus));

        listTable.setModel(tableModel);
        listTable.setFillsViewportHeight(true);
        setColumnWidths(listTable);

        JScrollPane scrollPane = new JScrollPane(listTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void setColumnWidths(JTable table) {
        int[] columnWidths = {80, 150, 100, 50, 80, 100, 100, 100, 100, 100}; // Example widths
        for (int i = 0; i < columnWidths.length; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(columnWidths[i]);
        }
    }

    private void initializeStatus() {
        KeyValue[] status = {
                new KeyValue(0, "All"),
                new KeyValue(1, "Borrow"),
                new KeyValue(2, "Return")
        };
        for (KeyValue keyValue : status) {
            cate.addItem(keyValue);
        }
    }
}
