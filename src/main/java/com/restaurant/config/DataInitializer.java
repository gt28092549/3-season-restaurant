package com.restaurant.config;

import com.restaurant.entity.MenuItem;
import com.restaurant.order.model.repository.MenuItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(MenuItemRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                MenuItem item1 = new MenuItem();
                item1.setName("ชานมไทยชีสโฟม...");
                item1.setDescription("ชาไทยเบลนด์พิเศษเข้มข้น หอมใบชาคั่ว ท็อปด้วยชีสโฟมนุ่มละมุน");
                item1.setPrice(65.0);
                item1.setCategory("เครื่องดื่ม");
                item1.setType("DRINK");
                item1.setImageUrl("https://images.unsplash.com/photo-1558857563-b371033873b8?w=300");
                item1.setIsPopular(true);
                repository.save(item1);

                MenuItem item2 = new MenuItem();
                item2.setName("ผัดไทยกุ้งสดห่อไข่");
                item2.setDescription("เส้นจันท์เหนียวนุ่ม ผัดซอสมะขามเปียกเคี่ยวโบราณ กุ้งแชบ๊วยตัวใหญ่ ห่อด้วยไข่เป็ดบางกรอบ เสิร์ฟพร้อมถั่วงอกสดและมะนาว");
                item2.setPrice(95.0);
                item2.setCategory("อาหารจานเดียว");
                item2.setType("FOOD");
                item2.setImageUrl("https://images.unsplash.com/photo-1559847844-5315695dadae?w=300");
                item2.setIsPopular(true);
                repository.save(item2);

                MenuItem item3 = new MenuItem();
                item3.setName("ต้มยำกุ้งน้ำข้น");
                item3.setDescription("ต้มยำรสเข้มข้น หอมเครื่องต้มยำสด กุ้งแม่น้ำตัวโต");
                item3.setPrice(180.0);
                item3.setCategory("ต้มและแกง");
                item3.setType("SOUP");
                item3.setImageUrl("https://images.unsplash.com/photo-1548943487-a2e4e43b4853?w=300");
                item3.setIsPopular(false);
                repository.save(item3);
            }
        };
    }
}