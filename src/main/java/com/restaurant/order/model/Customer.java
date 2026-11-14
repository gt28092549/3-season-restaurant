package com.restaurant.order.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String phone;      // ใช้เบอร์โทรเป็นรหัสล็อกอินหลัก

    private String name;       // ชื่อลูกค้า
    private Integer points = 0; // คะแนนสะสม
    private Integer totalOrders = 0; // จำนวนครั้งที่สั่ง
    private LocalDateTime registeredAt;

    public Customer() {}

    public Customer(String phone, String name) {
        this.phone = phone;
        this.name = name;
        this.points = 0;
        this.totalOrders = 0;
        this.registeredAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}