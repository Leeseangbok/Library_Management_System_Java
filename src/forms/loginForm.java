package forms;

import cls.authentication;
import cls.user;
import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DatePicker;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class loginForm extends JDialog {
    private JPanel loginPanel;
    private JPasswordField passwordPasswordField;
    private JTextField usernameTextField;
    private JLabel passwordPanel;
    private JLabel usernamePanel;
    private JLabel loginLabel;
    private JButton cancelButton;
    private JButton loginButton;
    private JPanel dateTimepanel;
    private DatePicker datePicker;
    private JSpinner timeSpinner;

    public loginForm(Frame parent) {
        super(parent, true);
        setContentPane(loginPanel);
        setMinimumSize(new Dimension(500, 350));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        cancelButton.addActionListener(e -> setCancelButtonListener());
        loginButton.addActionListener(new setLoginButtonListener());
        DateTimePickerPanel();
    }

    private void DateTimePickerPanel() {
        // Initialize Date Picker
        DatePickerSettings datePickerSettings = new DatePickerSettings();
        datePickerSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
        datePicker = new DatePicker(datePickerSettings);

        // Initialize Time Picker
        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
        timeSpinner.setEditor(timeEditor);

        // Automatically set current date and time
        Calendar now = Calendar.getInstance();
        datePicker.setDate(now.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        timeSpinner.setValue(now.getTime());

        // Set components as read-only
        datePicker.getComponentDateTextField().setEditable(false); // Date picker read-only
        timeSpinner.setEnabled(false); // Time picker read-only
        datePicker.setEnabled(false);

        // Add components to panel
        dateTimepanel.setLayout(new GridLayout(1, 2));
        dateTimepanel.add(datePicker);
        dateTimepanel.add(timeSpinner);
    }

    private Timestamp getDateTime() {
        LocalDate date = datePicker.getDate();
        Date time = (Date) timeSpinner.getValue();
        LocalTime localTime = LocalTime.of(time.getHours(), time.getMinutes(), time.getSeconds());
        ZonedDateTime zonedDateTime = ZonedDateTime.of(date, localTime, ZoneId.systemDefault());
        return Timestamp.from(zonedDateTime.toInstant());
    }

    private class setLoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameTextField.getText();
            String password = String.valueOf(passwordPasswordField.getPassword());

            if (authenticate(username, password)) {
                authentication.setAuthenticated(true);
                user.setUserName(username);
                int userID = 0;

                try (Connection connection = DBConnection.getConnection()) {
                    // Retrieve user ID
                    PreparedStatement userStatement = connection.prepareStatement("SELECT id FROM user WHERE username = ?");
                    userStatement.setString(1, username);
                    ResultSet rs = userStatement.executeQuery();
                    if (rs.next()) {
                        userID = rs.getInt("id");
                    }

                    // Insert login record
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO login(username_id, role_id, log_in_date) VALUES (?, ?, ?)");
                    statement.setInt(1, userID);
                    statement.setInt(2, user.getRoleID());
                    statement.setTimestamp(3, getDateTime());

                    // Execute the insert statement
                    statement.executeUpdate();

                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
                dispose();
            }
            else {
                JOptionPane.showMessageDialog(loginForm.this, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
                authentication.setAuthenticated(false);
            }
        }
    }

    private void setCancelButtonListener() {
        System.exit(0);
    }

    private boolean authenticate(String username, String password) {
        String query = "SELECT * FROM user WHERE username=? AND password=?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                user.setUserName(rs.getString("username"));
                user.setRoleID(rs.getInt("role_id"));
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
