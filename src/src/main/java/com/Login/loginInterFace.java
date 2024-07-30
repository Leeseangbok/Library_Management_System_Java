package src.main.java.com.Login;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import main.java.com.database.DBconnection;

public class loginInterFace extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private boolean succeeded;

    public loginInterFace(Frame user) {
        super(user, "Login", true);
        createGUI(user);
    }

    private void createGUI(Frame user) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.HORIZONTAL;

        JLabel adminLabel = new JLabel("Username: ");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        panel.add(adminLabel, constraints);

        usernameField = new JTextField(20);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        panel.add(usernameField, constraints);

        JLabel passwordLabel = new JLabel("Password: ");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        panel.add(passwordLabel, constraints);

        passwordField = new JPasswordField(20);
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        panel.add(passwordField, constraints);

        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new LoginButtonActionListener());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new CancelButtonActionListener());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(user);
    }

    private class LoginButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (authenticate(usernameField.getText(), new String(passwordField.getPassword()))) {
                succeeded = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(loginInterFace.this, "Input the correct information, dumbass", "Login", JOptionPane.ERROR_MESSAGE);
                usernameField.setText("");
                passwordField.setText("");
                succeeded = false;
            }
        }
    }

    private class CancelButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }

    private boolean authenticate(String username, String password) {
        try (Connection connection = DBconnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM login WHERE username =? AND password =?")) {

            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}