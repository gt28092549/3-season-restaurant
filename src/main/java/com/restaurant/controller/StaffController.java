package com.restaurant.controller;

import com.restaurant.order.model.OrderRequest;
import com.restaurant.order.model.StaffAccount;
import com.restaurant.order.model.StaffCall;
import com.restaurant.order.model.repository.OrderRepository;
import com.restaurant.order.model.repository.ReviewRepository;
import com.restaurant.order.model.repository.StaffAccountRepository;
import com.restaurant.order.model.repository.StaffCallRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StaffCallRepository staffCallRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private StaffAccountRepository staffAccountRepository;

    // --- 1. หน้าเข้าสู่ระบบพนักงานและผู้จัดการร้าน ---
    @GetMapping("/login")
    public String staffLoginPage() {
        return "staff-login";
    }

    @PostMapping("/login")
    public String processStaffLogin(@RequestParam String username,
                                    @RequestParam String password,
                                    HttpSession session,
                                    Model model) {
        String nameInput = username.trim();

        // บัญชีหลัก Admin -> Name: admin / Password: admin123456
        if ("admin".equalsIgnoreCase(nameInput) && "admin123456".equals(password)) {
            session.setAttribute("staffUser", "admin");
            session.setAttribute("staffRole", "ADMIN");
            session.setAttribute("staffName", "ผู้จัดการร้าน (Admin)");
            return "redirect:/admin";
        }

        // บัญชีหลัก Staff -> Name: staff / Password: 1234
        if ("staff".equalsIgnoreCase(nameInput) && "1234".equals(password)) {
            session.setAttribute("staffUser", "staff");
            session.setAttribute("staffRole", "STAFF");
            session.setAttribute("staffName", "พนักงานครัว (Staff)");
            return "redirect:/staff/queue";
        }

        // บัญชีพนักงานเพิ่มเติมที่สร้างจากหน้า Admin (FR-09)
        for (StaffAccount acc : staffAccountRepository.findAll()) {
            if (acc.getUsername().equalsIgnoreCase(nameInput) && acc.getPassword().equals(password)) {
                session.setAttribute("staffUser", acc.getUsername());
                session.setAttribute("staffRole", acc.getRole());
                session.setAttribute("staffName", acc.getFullName());
                return "ADMIN".equalsIgnoreCase(acc.getRole()) ? "redirect:/admin" : "redirect:/staff/queue";
            }
        }

        model.addAttribute("error", "Name หรือ Password ไม่ถูกต้อง กรุณาลองใหม่อีกครั้ง");
        return "staff-login";
    }

    // --- 2. ออกจากระบบ ---
    @GetMapping("/logout")
    public String staffLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/staff/login";
    }

    // --- 3. หน้าจอครัว (KDS) ---
    @GetMapping("/queue")
    public String kitchenQueue(HttpSession session, Model model) {
        if (session.getAttribute("staffUser") == null) {
            return "redirect:/staff/login";
        }

        List<OrderRequest> allOrders = orderRepository.findAll();

        List<OrderRequest> activeOrders = allOrders.stream()
                .filter(o -> !"Archived".equals(o.getStatus()))
                .toList();

        Set<Integer> activeTables = activeOrders.stream()
                .map(OrderRequest::getTableNumber)
                .collect(Collectors.toSet());

        List<StaffCall> pendingCalls = staffCallRepository.findByStatus("Pending");

        model.addAttribute("orders", activeOrders);
        model.addAttribute("pendingCalls", pendingCalls);
        model.addAttribute("activeTables", activeTables);
        model.addAttribute("reviews", reviewRepository.findAllByOrderByCreatedAtDesc());

        return "staff-queue";
    }

    // --- 4. อัปเดตสถานะอาหาร ---
    @PostMapping({"/orders/status/{id}", "/order/status/{id}"})
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    HttpSession session) {
        if (session.getAttribute("staffUser") == null) {
            return "redirect:/staff/login";
        }

        OrderRequest order = orderRepository.findById(id).orElseThrow();
        order.setStatus(status);
        orderRepository.save(order);
        return "redirect:/staff/queue";
    }

    // --- 5. รับทราบการเรียกพนักงาน ---
    @PostMapping({"/calls/confirm/{id}", "/call/resolve/{id}"})
    public String resolveStaffCall(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("staffUser") == null) {
            return "redirect:/staff/login";
        }

        StaffCall call = staffCallRepository.findById(id).orElseThrow();
        call.setStatus("Resolved");
        staffCallRepository.save(call);
        return "redirect:/staff/queue";
    }

    // --- 6. เคลียร์โต๊ะ ---
    @PostMapping("/tables/clear/{tableNumber}")
    public String clearTable(@PathVariable Integer tableNumber, HttpSession session) {
        if (session.getAttribute("staffUser") == null) {
            return "redirect:/staff/login";
        }

        List<OrderRequest> tableOrders = orderRepository.findByTableNumberOrderByOrderTimeDesc(tableNumber);
        for (OrderRequest o : tableOrders) {
            if (!"Archived".equals(o.getStatus())) {
                o.setStatus("Archived");
                orderRepository.save(o);
            }
        }
        return "redirect:/staff/queue";
    }
}