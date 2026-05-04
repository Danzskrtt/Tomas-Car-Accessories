package Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActivityManager {
    private static final String DB_URL = "jdbc:sqlite:tomasDB.db";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Initialize the activities table if it doesn't exist
     */
    public static void initializeActivityTable() {
        String sql = "CREATE TABLE IF NOT EXISTS activities (" +
                "activity_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "activity_type TEXT NOT NULL," +
                "description TEXT NOT NULL," +
                "timestamp TEXT NOT NULL," +
                "performed_by TEXT NOT NULL" +
                ")";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Activities table initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing activities table: " + e.getMessage());
        }
    }
    
    /**
     * Log an activity to the database
     * @param activityType Type of activity (e.g., "PRODUCT_ADDED", "SALE_PROCESSED", "CUSTOMER_ADDED", "APPOINTMENT_SCHEDULED")
     * @param description Description of the activity
     * @param performedBy User who performed the activity
     */
    public static void logActivity(String activityType, String description, String performedBy) {
        String sql = "INSERT INTO activities (activity_type, description, timestamp, performed_by) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String timestamp = LocalDateTime.now().format(formatter);
            pstmt.setString(1, activityType);
            pstmt.setString(2, description);
            pstmt.setString(3, timestamp);
            pstmt.setString(4, performedBy);
            pstmt.executeUpdate();
            
            System.out.println("Activity logged: " + activityType + " - " + description);
        } catch (SQLException e) {
            System.err.println("Error logging activity: " + e.getMessage());
        }
    }
    
    /**
     * Retrieve recent activities from the database
     * @param limit Number of recent activities to retrieve
     * @return ObservableList of Activity objects
     */
    public static ObservableList<Activity> getRecentActivities(int limit) {
        ObservableList<Activity> activities = FXCollections.observableArrayList();
        String sql = "SELECT activity_id, activity_type, description, timestamp, performed_by " +
                "FROM activities ORDER BY activity_id DESC LIMIT ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Activity activity = new Activity(
                        rs.getInt("activity_id"),
                        rs.getString("activity_type"),
                        rs.getString("description"),
                        rs.getString("timestamp"),
                        rs.getString("performed_by")
                );
                activities.add(activity);
            }
            
            System.out.println("Retrieved " + activities.size() + " recent activities");
        } catch (SQLException e) {
            System.err.println("Error retrieving activities: " + e.getMessage());
        }
        
        return activities;
    }
    
    /**
     * Retrieve all activities from the database
     * @return ObservableList of Activity objects
     */
    public static ObservableList<Activity> getAllActivities() {
        ObservableList<Activity> activities = FXCollections.observableArrayList();
        String sql = "SELECT activity_id, activity_type, description, timestamp, performed_by " +
                "FROM activities ORDER BY activity_id DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Activity activity = new Activity(
                        rs.getInt("activity_id"),
                        rs.getString("activity_type"),
                        rs.getString("description"),
                        rs.getString("timestamp"),
                        rs.getString("performed_by")
                );
                activities.add(activity);
            }
            
            System.out.println("Retrieved " + activities.size() + " total activities");
        } catch (SQLException e) {
            System.err.println("Error retrieving all activities: " + e.getMessage());
        }
        
        return activities;
    }
    
    /**
     * Clear all activities from the database
     */
    public static void clearAllActivities() {
        String sql = "DELETE FROM activities";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sql);
            System.out.println("All activities cleared");
        } catch (SQLException e) {
            System.err.println("Error clearing activities: " + e.getMessage());
        }
    }
}
