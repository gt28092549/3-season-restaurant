package com.restaurant.config;

import com.restaurant.entity.MenuItem;
import com.restaurant.order.model.repository.MenuItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initMenuData(MenuItemRepository menuItemRepository) {
        return args -> {
            if (menuItemRepository.count() == 0) {
                List<MenuItem> initialMenus = List.of(
                    createMenu("ข้าวผัดต้มยำกุ้งแม่น้ำ", "ข้าวผัดรสจัดจ้านถึงเครื่องต้มยำ เสิร์ฟพร้อมกุ้งแม่น้ำย่างตัวโต", 145.0, "อาหารจานเดียว", "แนะนำ", "https://images.unsplash.com/photo-1559314809-0d155014e29e?w=600", true),
                    createMenu("ผัดไทยกุ้งสดเส้นจันท์", "เส้นจันท์เหนียวนุ่มผัดซอสมะขามสูตรโบราณ กุ้งสดตัวใหญ่", 120.0, "อาหารจานเดียว", "ยอดนิยม", "https://images.unsplash.com/photo-1559314809-0d155014e29e?w=600", true),
                    createMenu("แกงเขียวหวานไก่ยอดมะพร้าว", "พริกแกงเขียวหวานตำเอง เคี่ยวกะทิสดเข้มข้น เนื้อไก่นุ่ม", 135.0, "กับข้าว", "แนะนำ", "https://images.unsplash.com/photo-1455619452474-d2be8b1e70cd?w=600", true),
                    createMenu("ปลากะพงทอดน้ำปลา", "ปลากะพงสดทอดกรอบนอกนุ่มใน ราดซอสน้ำปลาหอมกรุ่น พร้อมยำมะม่วง", 320.0, "กับข้าว", "แนะนำ", "https://images.unsplash.com/photo-1580476262798-bddd9f4b7369?w=600", false),
                    createMenu("ชาไทยเย็นสูตรเข้มข้น", "ใบชาไทยแท้ชงสด หอมมัน หวานกำลังดี", 55.0, "เครื่องดื่ม", "ยอดนิยม", "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=600", true),
                    createMenu("น้ำมะนาวอัญชันโซดา", "สดชื่นซาบซ่าด้วยน้ำอัญชันแท้และมะนาวคั้นสด", 65.0, "เครื่องดื่ม", "ทั่วไป", "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=600", false),
                    createMenu("ข้าวเหนียวมะม่วงน้ำดอกไม้", "มะม่วงน้ำดอกไม้สุกหวานฉ่ำ เสิร์ฟคู่ข้าวเหนียวมูนกะทิสด", 95.0, "ของหวาน", "แนะนำ", "https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=600", true)
                );

                menuItemRepository.saveAll(initialMenus);
                System.out.println("✅ โหลดข้อมูลเมนูตัวอย่าง 3 Season Restaurant สำเร็จแล้ว (" + initialMenus.size() + " รายการ)");
            }
        };
    }

    private MenuItem createMenu(String name, String description, Double price, String category, String type, String imageUrl, boolean isPopular) {
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(category);
        item.setType(type);
        item.setImageUrl(imageUrl);
        item.setIsAvailable(true);
        item.setIsPopular(isPopular);
        item.setSoldCount(0);
        return item;
    }
}