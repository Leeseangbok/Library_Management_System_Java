package cls;

public class authentication {
    private static volatile boolean authenticated = false;

    public static boolean isAuthenticated() {

        return authenticated;
    }

    public static void setAuthenticated(boolean status) {

        authenticated = status;

    }
}
