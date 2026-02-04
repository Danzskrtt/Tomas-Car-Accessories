package Model;

import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UserManagementModel {
    
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    
    // Establish database connection
    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return conn;
    }
    
    // Get all users from database
    public ObservableList<User> getAllUsers() {
        ObservableList<User> users = FXCollections.observableArrayList();
        String sql = "SELECT * FROM users ORDER BY user_id";
        
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getString("phone"),
                    rs.getInt("is_active"),
                    rs.getString(" created_at"),
                    rs.getString("  last_login")
                );
                users.add(user);
            }
            
        } catch (SQLException e) {
            System.out.println("Error fetching users: " + e.getMessage());
        }
        
        return users;
    }
    
    // Add new user
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, first_name, last_name, email, role, phone, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getLastName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getRole());
            pstmt.setString(7, user.getPhone());
            pstmt.setInt(8, user.getIsActive());
            
            pstmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            System.out.println("Error adding user: " + e.getMessage());
            return false;
        }
    }
    
    // Update existing user
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, first_name = ?, last_name = ?, email = ?, role = ?, phone = ?, is_active = ? WHERE user_id = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getLastName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getRole());
            pstmt.setString(7, user.getPhone());
            pstmt.setInt(8, user.getIsActive());
            pstmt.setInt(9, user.getUserId());
            
            pstmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            System.out.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
    
    // Delete user
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
    
    // Search users by username or name
    public ObservableList<User> searchUsers(String searchTerm) {
        ObservableList<User> users = FXCollections.observableArrayList();
        String sql = "SELECT * FROM users WHERE username LIKE ? OR first_name LIKE ? OR last_name LIKE ? ORDER BY user_id";
        
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getString("phone"),
                    rs.getInt("is_active"),
                    rs.getString(" created_at"),
                    rs.getString("  last_login")
                );
                users.add(user);
            }
            
        } catch (SQLException e) {
            System.out.println("Error searching users: " + e.getMessage());
        }
        
        return users;
    }
    
    // Get total user count
    public int getTotalUserCount() {
        String sql = "SELECT COUNT(*) as count FROM users";
        int count = 0;
        
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                count = rs.getInt("count");
            }
            
        } catch (SQLException e) {
            System.out.println("Error fetching user count: " + e.getMessage());
        }
        
        return count;
    }
}
