package com.restaurant.controller;

import com.restaurant.entity.MenuItem;
import com.restaurant.order.model.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MenuController {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @GetMapping("/")
    public String index(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "search", required = false) String search,
            Model model) {

        List<MenuItem> items;

        if (search != null && !search.isEmpty()) {
            items = menuItemRepository.findByNameContainingIgnoreCase(search);
        } else if (category != null && !category.isEmpty() && !"ทั้งหมด".equals(category)) {
            items = menuItemRepository.findByCategory(category);
        } else {
            items = menuItemRepository.findAll();
        }

        model.addAttribute("menuItems", items);
        model.addAttribute("activeCategory", category != null ? category : "ทั้งหมด");
        return "index";
    }

    // 👈 เพิ่ม Controller สำหรับเรียกหน้าสถานะคำสั่งซื้อ / สถานะครัว
    @GetMapping("/order-status")
    public String orderStatus() {
        return "order-status";
    }

    @GetMapping("/payment")
    public String payment() {
        return "payment";
    }

    @GetMapping ("/login")
    public String login() {
        return "login";
    }

    @GetMapping ("/profile")
    public String profile() {
        return "profile";
    }
}
