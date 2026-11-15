package com.restaurant.order.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "staff_accounts")
public class StaffAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String fullName;
    private String employeeCode; // เช่น EMP-001
    private String role;         // STAFF หรือ ADMIN
    private String position;     // เช่น Kitchen Staff, Waitstaff, Manager
    private LocalDate hireDate = LocalDate.now();

    public StaffAccount() {}

    public StaffAccount(String username, String password, String fullName, String employeeCode, String role, String position) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.employeeCode = employeeCode;
        this.role = role;
        this.position = position;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
}