package com.restaurant.order.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_requests")
public class OrderRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer tableNumber;
    private String foodName;
    private Integer quantity;
    private Double totalPrice;
    private String note;
    private String status;
    private LocalDateTime orderTime;
    private String customerPhone;

    public OrderRequest() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getTableNumber() { return tableNumber; }
    public void setTableNumber(Integer tableNumber) { this.tableNumber = tableNumber; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
}