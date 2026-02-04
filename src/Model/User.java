package Model;

import javafx.beans.property.*;

public class User {
    private final IntegerProperty userId;
    private final StringProperty username;
    private final StringProperty password;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty email;
    private final StringProperty role;
    private final StringProperty phone;
    private final IntegerProperty isActive;
    private final StringProperty createdAt;
    private final StringProperty lastLogin;
    
    // Constructor
    public User() {
        this(0, "", "", "", "", "", "", "", 1, "", "");
    }
    
    public User(int userId, String username, String password, String firstName, 
                String lastName, String email, String role, String phone, 
                int isActive, String createdAt, String lastLogin) {
        this.userId = new SimpleIntegerProperty(userId);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.email = new SimpleStringProperty(email);
        this.role = new SimpleStringProperty(role);
        this.phone = new SimpleStringProperty(phone);
        this.isActive = new SimpleIntegerProperty(isActive);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.lastLogin = new SimpleStringProperty(lastLogin);
    }
    
    // User ID
    public int getUserId() { return userId.get(); }
    public void setUserId(int userId) { this.userId.set(userId); }
    public IntegerProperty userIdProperty() { return userId; }
    
    // Username
    public String getUsername() { return username.get(); }
    public void setUsername(String username) { this.username.set(username); }
    public StringProperty usernameProperty() { return username; }
    
    // Password
    public String getPassword() { return password.get(); }
    public void setPassword(String password) { this.password.set(password); }
    public StringProperty passwordProperty() { return password; }
    
    // First Name
    public String getFirstName() { return firstName.get(); }
    public void setFirstName(String firstName) { this.firstName.set(firstName); }
    public StringProperty firstNameProperty() { return firstName; }
    
    // Last Name
    public String getLastName() { return lastName.get(); }
    public void setLastName(String lastName) { this.lastName.set(lastName); }
    public StringProperty lastNameProperty() { return lastName; }
    
    // Email
    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }
    
    // Role
    public String getRole() { return role.get(); }
    public void setRole(String role) { this.role.set(role); }
    public StringProperty roleProperty() { return role; }
    
    // Phone
    public String getPhone() { return phone.get(); }
    public void setPhone(String phone) { this.phone.set(phone); }
    public StringProperty phoneProperty() { return phone; }
    
    // Is Active
    public int getIsActive() { return isActive.get(); }
    public void setIsActive(int isActive) { this.isActive.set(isActive); }
    public IntegerProperty isActiveProperty() { return isActive; }
    
    // Created At
    public String getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(String createdAt) { this.createdAt.set(createdAt); }
    public StringProperty createdAtProperty() { return createdAt; }
    
    // Last Login
    public String getLastLogin() { return lastLogin.get(); }
    public void setLastLogin(String lastLogin) { this.lastLogin.set(lastLogin); }
    public StringProperty lastLoginProperty() { return lastLogin; }
}
