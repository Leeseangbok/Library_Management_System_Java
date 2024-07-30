import src.main.java.com.Login.loginInterFace;
import src.main.menuPage;
public class main{
    public static void main(String[] args) {
        loginInterFace loginDialog = new loginInterFace(null);
        loginDialog.setVisible(true);
        if (loginDialog.isSucceeded()) {
            menuPage menu = new menuPage();
            menu.setVisible(true);
        
        } else {
            System.exit(0);
        }
    }
}