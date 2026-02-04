package Model;

public class UserSession {
    private static UserSession instance;
    private String userRole;
    private String username;
    
    private UserSession() {
        // Private constructor for singleton
    }
    
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void clearSession() {
        userRole = null;
        username = null;
    }
}
