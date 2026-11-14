package com.restaurant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 500)
    private String description;

    private Double price;
    private String category; // เช่น "อาหารจานเดียว", "ต้มและแกง", "เครื่องดื่ม"
    private String type;     // "FOOD" หรือ "DRINK"
    private String imageUrl;
    private Boolean isPopular;
    private Boolean isAvailable  = true;
    private Integer soldCount = 0;

    public MenuItem() {}

    public Boolean getIsAvailable() { return isAvailable != null ? isAvailable : true; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public Integer getSoldCount() { return soldCount != null ? soldCount : 0; }
    public void setSoldCount(Integer soldCount) { this.soldCount = soldCount; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getIsPopular() { return isPopular; }
    public void setIsPopular(Boolean isPopular) { this.isPopular = isPopular; }
}