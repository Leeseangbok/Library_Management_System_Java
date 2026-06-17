package cls;

public class user {
    private static String username;
    private static int roleID;

    public static String getUserName() {
        return user.username;
    }

    public static void setUserName(String username) {
        user.username = username;
    }

    public static int getRoleID() {return roleID;}

    public static void setRoleID(int roleId) {user.roleID = roleId;}
}
