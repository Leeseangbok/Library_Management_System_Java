package src.main.java.com.Login;

public class login {

    public static void main(String[] args) {
        loginInterFace loginDialog = new loginInterFace(null);
        loginDialog.setVisible(true);
        if (loginDialog.isSucceeded()) {
            new login();
        } else {
            System.exit(0);
        }
    }
}
