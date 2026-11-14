package com.restaurant.controller;

import com.restaurant.entity.MenuItem;
import com.restaurant.order.model.Customer;
import com.restaurant.order.model.OrderRequest;
import com.restaurant.order.model.repository.CustomerRepository;
import com.restaurant.order.model.repository.MenuItemRepository;
import com.restaurant.order.model.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private MenuItemRepository menuItemRepository;

    @GetMapping("/login")
    public String loginPage() {
        return "admin-login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        if ("Admin".equals(username) && "admin123456".equals(password)) {
            session.setAttribute("isAdmin", true);
            return "redirect:/admin/menus";
        }
        model.addAttribute("error", "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
        return "admin-login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("isAdmin");
        return "redirect:/admin/login";
    }

    @GetMapping("/customers")
    public String viewCustomers(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/admin/login";
        }
        List<Customer> customers = customerRepository.findAll();
        int totalPoints = customers.stream().mapToInt(c -> c.getPoints() != null ? c.getPoints() : 0).sum();
        int totalOrders = customers.stream().mapToInt(c -> c.getTotalOrders() != null ? c.getTotalOrders() : 0).sum();

        model.addAttribute("customers", customers);
        model.addAttribute("totalCustomers", customers.size());
        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("totalOrders", totalOrders);
        return "admin-customers";
    }

    @GetMapping("/menus")
    public String viewMenus(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/admin/login";
        }
        model.addAttribute("menus", menuItemRepository.findAll());
        return "admin-menus";
    }

    @PostMapping("/menus/add")
    public String addMenu(@RequestParam String name,
                          @RequestParam Double price,
                          @RequestParam String category,
                          @RequestParam String description,
                          @RequestParam(required = false) String imageUrl,
                          @RequestParam(defaultValue = "false") Boolean isPopular,
                          HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/admin/login";
        }
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setPrice(price);
        item.setCategory(category);
        item.setType("เครื่องดื่ม".equals(category) ? "DRINK" : "FOOD");
        item.setDescription(description);
        item.setImageUrl((imageUrl != null && !imageUrl.isEmpty()) ? imageUrl : "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=300");
        item.setIsPopular(isPopular);
        item.setIsAvailable(true);
        item.setSoldCount(0);
        menuItemRepository.save(item);
        return "redirect:/admin/menus";
    }

    @PostMapping("/menus/toggle/{id}")
    public String toggleAvailability(@PathVariable Long id, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/admin/login";
        }
        MenuItem item = menuItemRepository.findById(id).orElseThrow();
        item.setIsAvailable(!item.getIsAvailable());
        menuItemRepository.save(item);
        return "redirect:/admin/menus";
    }

    @PostMapping("/menus/price/{id}")
    public String updatePrice(@PathVariable Long id, @RequestParam Double price, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/admin/login";
        }
        MenuItem item = menuItemRepository.findById(id).orElseThrow();
        item.setPrice(price);
        menuItemRepository.save(item);
        return "redirect:/admin/menus";
    }

    @PostMapping("/menus/delete/{id}")
    public String deleteMenu(@PathVariable Long id, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/admin/login";
        }
        menuItemRepository.deleteById(id);
        return "redirect:/admin/menus";
    }

    @GetMapping("/reports")
    public String viewReports(HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/admin/login";
        }
        List<OrderRequest> allOrders = orderRepository.findAll();
        List<MenuItem> topMenus = menuItemRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(
                        b.getSoldCount() != null ? b.getSoldCount() : 0,
                        a.getSoldCount() != null ? a.getSoldCount() : 0))
                .toList();

        double totalRevenue = allOrders.stream()
                .filter(o -> "Paid".equals(o.getStatus()) || "Served".equals(o.getStatus()))
                .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0.0)
                .sum();

        long paidOrdersCount = allOrders.stream()
                .filter(o -> "Paid".equals(o.getStatus()))
                .count();

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalOrdersCount", allOrders.size());
        model.addAttribute("paidOrdersCount", paidOrdersCount);
        model.addAttribute("topMenus", topMenus);
        model.addAttribute("recentOrders", allOrders);
        return "admin-reports";
    }

    @GetMapping("/qrcodes")
    public String viewTableQRCodes(@RequestParam(defaultValue = "8") Integer totalTables,
                                   HttpSession session,
                                   Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/admin/login";
        }
        model.addAttribute("totalTables", totalTables);
        return "admin-qrcodes";
    }
}