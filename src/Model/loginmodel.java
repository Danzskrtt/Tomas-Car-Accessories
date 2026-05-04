package Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class loginmodel {
    
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    private String userRole = null;
    private int userId = 0;
    
    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return conn;
    }
    
    public boolean validateLogin(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
           
            if (rs.next()) {
                userId = rs.getInt("user_id");
                userRole = rs.getString("role");
                return true;
            }
            
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        
        return false;
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    public int getUserId() {
        return userId;
    }
}
