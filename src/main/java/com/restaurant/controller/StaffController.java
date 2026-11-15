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

    @GetMapping("/queue") // หรือ path เดิมของคุณ
    public String kitchenQueue(HttpSession session, Model model) {
        // ตรวจสอบสิทธิ์: ต้องล็อกอินเป็น STAFF หรือ ADMIN
        if (session.getAttribute("staffUser") == null) {
            return "redirect:/staff/login";
        }
        List<OrderRequest> allOrders = orderRepository.findAll();

        List<OrderRequest> queuedOrders = allOrders.stream()
                .filter(o -> "Queued".equals(o.getStatus()))
                .toList();

        List<OrderRequest> cookingOrders = allOrders.stream()
                .filter(o -> "Cooking".equals(o.getStatus()))
                .toList();

        List<OrderRequest> servedOrders = allOrders.stream()
                .filter(o -> "Served".equals(o.getStatus()))
                .toList();

        Set<Integer> activeTables = allOrders.stream()
                .filter(o -> !"Archived".equals(o.getStatus()))
                .map(OrderRequest::getTableNumber)
                .collect(Collectors.toSet());

        List<StaffCall> activeCalls = staffCallRepository.findByStatus("Pending");

        model.addAttribute("queuedOrders", queuedOrders);
        model.addAttribute("cookingOrders", cookingOrders);
        model.addAttribute("servedOrders", servedOrders);
        model.addAttribute("activeTables", activeTables);
        model.addAttribute("activeCalls", activeCalls);
        model.addAttribute("reviews", reviewRepository.findAllByOrderByCreatedAtDesc());

        return "staff-queue";
    }

    @PostMapping("/order/status/{id}")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        OrderRequest order = orderRepository.findById(id).orElseThrow();
        order.setStatus(status);
        orderRepository.save(order);
        return "redirect:/staff/queue";
    }

    @PostMapping("/call/resolve/{id}")
    public String resolveStaffCall(@PathVariable Long id) {
        StaffCall call = staffCallRepository.findById(id).orElseThrow();
        call.setStatus("Resolved");
        staffCallRepository.save(call);
        return "redirect:/staff/queue";
    }

    @PostMapping("/tables/clear/{tableNumber}")
    public String clearTable(@PathVariable Integer tableNumber) {
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