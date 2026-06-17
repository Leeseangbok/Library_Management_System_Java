package cls;

import forms.guestForm;
import forms.loginForm;
import forms.adminForm;
import forms.userForm;

import javax.swing.*;

public class roleLogin {
    public static void  performedLogin(){
        loginForm loginForm = new loginForm(null);
        loginForm.setVisible(true);

        if (authentication.isAuthenticated()) {
            System.out.println("Login Successful. Connection Established.");
            int roleID = user.getRoleID();

            switch (roleID) {
                case 1:
                    adminForm admin = new adminForm();
                    admin.setVisible(true);
                    break;
                case 2:
                    userForm user = new userForm();
                    user.setVisible(true);
                    break;
                case 3:
                    guestForm guest = new guestForm();
                    guest.setVisible(true);
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Invalid role ID");
            }
        }
        else {
            System.out.println("Login Failed. Connection Not Established.");
            System.exit(0);
        }
    }
}
