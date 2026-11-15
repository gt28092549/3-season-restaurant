package com.restaurant.controller;

import com.restaurant.entity.MenuItem;
import com.restaurant.order.model.OrderRequest;
import com.restaurant.order.model.Promotion;
import com.restaurant.order.model.StaffAccount;
import com.restaurant.order.model.repository.MenuItemRepository;
import com.restaurant.order.model.repository.OrderRepository;
import com.restaurant.order.model.repository.PromotionRepository;
import com.restaurant.order.model.repository.ReviewRepository;
import com.restaurant.order.model.repository.StaffAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private StaffAccountRepository staffAccountRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    // --- หน้าหลัก Admin Dashboard (รองรับทั้ง /admin, /admin/ และ /admin/dashboard) ---
    @GetMapping({"", "/", "/dashboard"})
    public String adminDashboard(HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("staffRole"))) {
            return "redirect:/staff/login";
        }

        List<MenuItem> menuItems = menuItemRepository.findAll();
        List<OrderRequest> allOrders = orderRepository.findAll();

        // สร้าง Map จับคู่ชื่ออาหาร -> ราคา เพื่อคำนวณยอดขายรวมได้อย่างแม่นยำ
        java.util.Map<String, Double> priceMap = menuItems.stream()
                .collect(Collectors.toMap(MenuItem::getName, MenuItem::getPrice, (a, b) -> a));

        double totalSales = allOrders.stream()
                .mapToDouble(o -> priceMap.getOrDefault(o.getFoodName(), 0.0) * o.getQuantity())
                .sum();

        Set<Integer> activeTables = allOrders.stream()
                .filter(o -> !"Archived".equals(o.getStatus()))
                .map(OrderRequest::getTableNumber)
                .collect(Collectors.toSet());

        model.addAttribute("menuItems", menuItems);
        model.addAttribute("orders", allOrders);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("activeTableCount", activeTables.size());
        model.addAttribute("reviews", reviewRepository.findAllByOrderByCreatedAtDesc());

        return "admin-dashboard"; // เช็กในโฟลเดอร์ templates/ ว่าไฟล์ชื่อ admin-dashboard.html หรือ admin.html
    }

    // --- FR-05: เพิ่มเมนูอาหาร ---
    @PostMapping("/menu/add")
    public String addMenuItem(@RequestParam String name,
                              @RequestParam String category,
                              @RequestParam double price,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) String imageUrl,
                              HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("staffRole"))) {
            return "redirect:/staff/login";
        }
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setCategory(category);
        item.setPrice(price);
        item.setDescription(description != null ? description : "");
        item.setImageUrl(imageUrl != null ? imageUrl : "");
        menuItemRepository.save(item);
        return "redirect:/admin";
    }

    @PostMapping("/menu/delete/{id}")
    public String deleteMenuItem(@PathVariable Long id, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("staffRole"))) {
            return "redirect:/staff/login";
        }
        menuItemRepository.deleteById(id);
        return "redirect:/admin";
    }

    // --- FR-01: พิมพ์ป้าย QR Code ประจำโต๊ะ (โต๊ะ 1 - 12) ---
    @GetMapping("/qrcodes")
    public String tableQrCodes(HttpSession session, Model model, HttpServletRequest request) {
        if (!"ADMIN".equals(session.getAttribute("staffRole"))) {
            return "redirect:/staff/login";
        }

        String forwardedHost = request.getHeader("X-Forwarded-Host");
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String baseUrl;
        if (forwardedHost != null && !forwardedHost.isEmpty()) {
            String proto = (forwardedProto != null) ? forwardedProto : "https";
            baseUrl = proto + "://" + forwardedHost;
        } else {
            baseUrl = request.getScheme() + "://" + request.getServerName()
                    + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort());
        }

        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("totalTables", 5);
        return "admin-qrcodes";
    }

    // --- FR-09: จัดการบัญชีพนักงาน ---
    @GetMapping("/staff-manage")
    public String manageStaff(HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("staffRole"))) {
            return "redirect:/staff/login";
        }
        model.addAttribute("staffList", staffAccountRepository.findAll());
        return "admin-staff";
    }

    @PostMapping("/staff-manage/add")
    public String addStaff(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String fullName,
                           @RequestParam String employeeCode,
                           @RequestParam String role,
                           @RequestParam String position,
                           HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("staffRole"))) {
            return "redirect:/staff/login";
        }
        staffAccountRepository.save(new StaffAccount(
                username.trim().toLowerCase(),
                password,
                fullName,
                employeeCode,
                role,
                position
        ));
        return "redirect:/admin/staff-manage";
    }

    @PostMapping("/staff-manage/delete/{id}")
    public String deleteStaff(@PathVariable Long id, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("staffRole"))) {
            return "redirect:/staff/login";
        }
        staffAccountRepository.deleteById(id);
        return "redirect:/admin/staff-manage";
    }

    // --- FR-10: จัดการโค้ดส่วนลดและโปรโมชัน ---
    @GetMapping("/promotions")
    public String managePromotions(HttpSession session, Model model) {
        if (!"ADMIN".equals(session.getAttribute("staffRole"))) {
            return "redirect:/staff/login";
        }
        model.addAttribute("promotions", promotionRepository.findAll());
        return "admin-promotions";
    }

    @PostMapping("/promotions/add")
    public String addPromotion(@RequestParam String promoCode,
                               @RequestParam String discountType,
                               @RequestParam double discountValue,
                               @RequestParam double minOrderAmount,
                               @RequestParam String startDate,
                               @RequestParam String endDate,
                               HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("staffRole"))) {
            return "redirect:/staff/login";
        }
        Promotion promo = new Promotion(
                promoCode.toUpperCase().trim(),
                discountType,
                discountValue,
                minOrderAmount,
                java.time.LocalDate.parse(startDate),
                java.time.LocalDate.parse(endDate),
                true
        );
        promotionRepository.save(promo);
        return "redirect:/admin/promotions";
    }

    @PostMapping("/promotions/toggle/{id}")
    public String togglePromotion(@PathVariable Long id, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("staffRole"))) {
            return "redirect:/staff/login";
        }
        Promotion promo = promotionRepository.findById(id).orElseThrow();
        promo.setActive(!promo.isActive());
        promotionRepository.save(promo);
        return "redirect:/admin/promotions";
    }

    @PostMapping("/promotions/delete/{id}")
    public String deletePromotion(@PathVariable Long id, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("staffRole"))) {
            return "redirect:/staff/login";
        }
        promotionRepository.deleteById(id);
        return "redirect:/admin/promotions";
    }
}