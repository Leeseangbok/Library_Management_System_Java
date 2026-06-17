package forms;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class crudBookForm extends JDialog {
    private JPanel crudPanel;
    private JTextField priceField;
    private JTextField bookField;
    private JTextField qtyField;
    private JTextField authorField;
    private JButton confirmButton;
    private JButton cancelButton;
    private JLabel crudLabel;
    private JLabel nameLabel;
    private final String action;
    private final String[] bookData;

    public crudBookForm(String action, String[] bookData) {
        this.action = action;
        this.bookData = bookData;

        setTitle("Book Form");
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 600, 300);
        confirmButton.addActionListener(new ConfirmButtonListener());
        cancelButton.addActionListener(new CancelButtonListener());
        setContentPane(crudPanel);

        if (action.equals("edit") && bookData != null) {
            bookField.setText(bookData[1]);
            qtyField.setText(bookData[2]);
            authorField.setText(bookData[3]);
            priceField.setText(bookData[4]);
        }
    }

    private class ConfirmButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = bookField.getText();
            String author = authorField.getText();
            String priceText = priceField.getText();
            String qty = qtyField.getText();

            if (action.equals("edit")) {
                priceText = priceText.replace("$", "").trim();
            }

            // Input validation
            if (name.isEmpty() || author.isEmpty() || priceText.isEmpty() || qty.isEmpty()) {
                JOptionPane.showMessageDialog(crudPanel, "Please fill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection connection = DBConnection.getConnection()) {
                String query;
                if (action.equals("add")) {
                    query = "INSERT INTO book(book_name, book_qty, book_author, book_price) VALUES(?, ?, ?, ?)";
                } else {
                    query = "UPDATE book SET book_name = ?, book_qty = ?, book_author = ?, book_price = ? WHERE id = ?";
                }

                try (PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, name);
                    statement.setInt(2, Integer.parseInt(qty));
                    statement.setString(3, author);
                    statement.setString(4, priceText);
                    if (action.equals("edit")) {
                        statement.setString(5, bookData[0]);
                    }
                    statement.executeUpdate();

                    if (action.equals("add")) {
                        ResultSet rs = statement.getGeneratedKeys();
                        if (rs.next()) {
                            int id = rs.getInt(1);
                            JOptionPane.showMessageDialog(crudPanel, "Book added with ID: " + id, "Success", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(crudPanel, "Book updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }

                    dispose(); // Close the form after successful insertion or update
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(crudPanel, "Error saving book information", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class CancelButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            bookField.setText("");
            authorField.setText("");
            priceField.setText("");
            qtyField.setText("");
        }
    }
}
