package com.restaurant.controller;

import com.restaurant.order.model.OrderRequest;
import com.restaurant.order.model.StaffCall;
import com.restaurant.order.model.repository.OrderRepository;
import com.restaurant.order.model.repository.StaffCallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StaffCallRepository staffCallRepository;

    // หน้าจอหลักของครัวและพนักงานเสิร์ฟ (UI-03)
    @GetMapping("/queue")
    public String viewOrderQueue(Model model) {
        List<OrderRequest> allOrders = orderRepository.findAllByOrderByOrderTimeAsc();
        List<StaffCall> pendingCalls = staffCallRepository.findByCallStatusOrderByCreatedAtAsc("Pending");

        model.addAttribute("orders", allOrders);
        model.addAttribute("pendingCalls", pendingCalls);
        return "staff-queue";
    }

    // อัปเดตสถานะอาหาร: Queued -> Cooking -> Ready -> Served (UC-06, UC-08)
    @PostMapping("/orders/status/{id}")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        OrderRequest order = orderRepository.findById(id).orElseThrow();
        order.setStatus(status);
        orderRepository.save(order);
        return "redirect:/staff/queue";
    }

    // พนักงานเสิร์ฟกดยืนยันการเรียกจากโต๊ะ (UC-05)
    @PostMapping("/calls/confirm/{id}")
    public String confirmStaffCall(@PathVariable Long id) {
        StaffCall call = staffCallRepository.findById(id).orElseThrow();
        call.setCallStatus("Confirmed");
        call.setConfirmedAt(LocalDateTime.now());
        staffCallRepository.save(call);
        return "redirect:/staff/queue";
    }
}