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
                    createMenu("ข้าวกะเพราไข่ดาว", "ผัดกะเพราพริกแห้งหอมกรุ่น เสิร์ฟคู่ไข่ดาวกรอบ", 85.0, "อาหารจานเดียว", "แนะนำ", "https://images.unsplash.com/photo-1562967914-608f82629710?w=600", true),
                    createMenu("ต้มยำกุ้งน้ำข้น", "ต้มยำกุ้งแม่น้ำรสแซ่บถึงเครื่องสมุนไพรไทย", 180.0, "ต้มและแกง", "แนะนำ", "https://images.unsplash.com/photo-1548943487-a2e4e43b4853?w=600", true),
                    createMenu("แกงเขียวหวานยอดมะพร้าว", "พริกแกงเขียวหวานตำเอง เคี่ยวกะทิสดเข้มข้น", 135.0, "ต้มและแกง", "ทั่วไป", "https://images.unsplash.com/photo-1455619452474-d2be8b1e70cd?w=600", false),
                    createMenu("ชานมไทยชีสโฟม", "ชาไทยเบลนด์พิเศษเข้มข้น หอมใบชาคั่ว ท็อปด้วยชีสโฟมนุ่มละมุน", 65.0, "เครื่องดื่ม", "ยอดนิยม", "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=600", true),
                    createMenu("น้ำมะนาวอัญชันโซดา", "สดชื่นซาบซ่าด้วยน้ำอัญชันแท้และมะนาวคั้นสด", 55.0, "เครื่องดื่ม", "แนะนำ", "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=600", false)
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