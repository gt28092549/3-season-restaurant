package com.restaurant.order.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_calls")
public class StaffCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer tableNumber;
    private String callStatus; // "Pending" หรือ "Confirmed"
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;

    public StaffCall() {}

    public StaffCall(Integer tableNumber) {
        this.tableNumber = tableNumber;
        this.callStatus = "Pending";
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getTableNumber() { return tableNumber; }
    public void setTableNumber(Integer tableNumber) { this.tableNumber = tableNumber; }

    public String getCallStatus() { return callStatus; }
    public void setCallStatus(String callStatus) { this.callStatus = callStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
}