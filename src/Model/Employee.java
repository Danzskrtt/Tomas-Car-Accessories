package Model;

import javafx.beans.property.*;

public class Employee {
    private final IntegerProperty employeeId;
    private final StringProperty employeeName;
    private final StringProperty position;
    private final StringProperty department;
    private final StringProperty email;
    private final StringProperty phone;
    private final StringProperty hireDate;
    private final DoubleProperty salary;
    private final StringProperty imagePath;
    
    // Constructor
    public Employee() {
        this(0, "", "", "", "", "", "", 0.0, null);
    }
    
    public Employee(int employeeId, String employeeName, String position, 
                   String department, String email, String phone, 
                   String hireDate, double salary, String imagePath) {
        this.employeeId = new SimpleIntegerProperty(employeeId);
        this.employeeName = new SimpleStringProperty(employeeName);
        this.position = new SimpleStringProperty(position);
        this.department = new SimpleStringProperty(department);
        this.email = new SimpleStringProperty(email);
        this.phone = new SimpleStringProperty(phone);
        this.hireDate = new SimpleStringProperty(hireDate);
        this.salary = new SimpleDoubleProperty(salary);
        this.imagePath = new SimpleStringProperty(imagePath);
    }
    
    // Employee ID
    public int getEmployeeId() { return employeeId.get(); }
    public void setEmployeeId(int employeeId) { this.employeeId.set(employeeId); }
    public IntegerProperty employeeIdProperty() { return employeeId; }
    
    // Employee Name
    public String getEmployeeName() { return employeeName.get(); }
    public void setEmployeeName(String employeeName) { this.employeeName.set(employeeName); }
    public StringProperty employeeNameProperty() { return employeeName; }
    
    // Position
    public String getPosition() { return position.get(); }
    public void setPosition(String position) { this.position.set(position); }
    public StringProperty positionProperty() { return position; }
    
    // Department
    public String getDepartment() { return department.get(); }
    public void setDepartment(String department) { this.department.set(department); }
    public StringProperty departmentProperty() { return department; }
    
    // Email
    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }
    
    // Phone
    public String getPhone() { return phone.get(); }
    public void setPhone(String phone) { this.phone.set(phone); }
    public StringProperty phoneProperty() { return phone; }
    
    // Hire Date
    public String getHireDate() { return hireDate.get(); }
    public void setHireDate(String hireDate) { this.hireDate.set(hireDate); }
    public StringProperty hireDateProperty() { return hireDate; }
    
    // Salary
    public double getSalary() { return salary.get(); }
    public void setSalary(double salary) { this.salary.set(salary); }
    public DoubleProperty salaryProperty() { return salary; }
    
    // Image Path
    public String getImagePath() { return imagePath.get(); }
    public void setImagePath(String imagePath) { this.imagePath.set(imagePath); }
    public StringProperty imagePathProperty() { return imagePath; }
}
