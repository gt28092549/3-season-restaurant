package com.restaurant.controller;

import com.restaurant.order.model.OrderRequest;
import com.restaurant.order.model.StaffCall;
import com.restaurant.order.model.repository.OrderRepository;
import com.restaurant.order.model.repository.ReviewRepository;
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

    @GetMapping("/queue")
    public String kitchenQueue(HttpSession session, Model model) {
        // ตรวจสอบสิทธิ์: ต้องล็อกอินเป็น STAFF หรือ ADMIN
        if (session.getAttribute("staffUser") == null) {
            return "redirect:/staff/login";
        }

        List<OrderRequest> allOrders = orderRepository.findAll();

        // ดึงออเดอร์ทั้งหมดที่ยังไม่ถูกเคลียร์โต๊ะ (Archived)
        List<OrderRequest> activeOrders = allOrders.stream()
                .filter(o -> !"Archived".equals(o.getStatus()))
                .toList();

        // ดึงเลขโต๊ะที่กำลังมีออเดอร์ค้างอยู่
        Set<Integer> activeTables = activeOrders.stream()
                .map(OrderRequest::getTableNumber)
                .collect(Collectors.toSet());

        // ดึงรายการเรียกพนักงานที่รอรับทราบ
        List<StaffCall> pendingCalls = staffCallRepository.findByStatus("Pending");

        model.addAttribute("orders", activeOrders);
        model.addAttribute("pendingCalls", pendingCalls);
        model.addAttribute("activeTables", activeTables);
        model.addAttribute("reviews", reviewRepository.findAllByOrderByCreatedAtDesc());

        return "staff-queue";
    }

    // อัปเดตสถานะการปรุง/เสิร์ฟอาหาร (รองรับทั้ง /orders/status/{id} และ /order/status/{id})
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

    // รับทราบการเรียกพนักงาน (รองรับทั้ง /calls/confirm/{id} และ /call/resolve/{id})
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

    // เคลียร์โต๊ะเมื่อลูกค้ารอบนั้นกลับแล้ว
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