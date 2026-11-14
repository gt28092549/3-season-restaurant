package com.restaurant.controller;

import com.restaurant.entity.MenuItem;
import com.restaurant.order.model.OrderRequest;
import com.restaurant.order.model.Review;
import com.restaurant.order.model.repository.MenuItemRepository;
import com.restaurant.order.model.repository.OrderRequestRepository;
import com.restaurant.order.model.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private OrderRequestRepository orderRequestRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping
    public String dashboard(Model model) {
        List<MenuItem> menuItems = menuItemRepository.findAll();
        List<OrderRequest> allOrders = orderRequestRepository.findAll();
        List<Review> reviews = reviewRepository.findAllByOrderByCreatedAtDesc();

        // คำนวณยอดขายรวมจากออเดอร์ที่เสิร์ฟหรือชำระเงินแล้ว
        double totalRevenue = allOrders.stream()
                .filter(o -> "Served".equals(o.getStatus()) || "Archived".equals(o.getStatus()))
                .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0.0)
                .sum();

        // คำนวณคะแนนรีวิวเฉลี่ย
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        model.addAttribute("menuItems", menuItems);
        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("averageRating", String.format("%.1f", averageRating));
        model.addAttribute("reviews", reviews);

        return "admin";
    }

    @PostMapping("/menu/add")
    public String addMenu(@RequestParam String name,
                          @RequestParam String description,
                          @RequestParam Double price,
                          @RequestParam String category,
                          @RequestParam String type,
                          @RequestParam(required = false) String imageUrl) {
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(category);
        item.setType(type);
        item.setImageUrl((imageUrl != null && !imageUrl.isEmpty()) ? imageUrl : "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600");
        item.setIsAvailable(true);
        item.setIsPopular(false);
        item.setSoldCount(0);
        menuItemRepository.save(item);
        return "redirect:/admin";
    }

    @PostMapping("/menu/delete/{id}")
    public String deleteMenu(@PathVariable Long id) {
        menuItemRepository.deleteById(id);
        return "redirect:/admin";
    }
}