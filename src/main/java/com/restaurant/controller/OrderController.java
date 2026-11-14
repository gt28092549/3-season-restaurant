package com.restaurant.controller;

import com.restaurant.order.model.Customer;
import com.restaurant.entity.MenuItem;
import com.restaurant.order.model.OrderRequest;
import com.restaurant.order.model.StaffCall;
import com.restaurant.order.model.repository.CustomerRepository;
import com.restaurant.order.model.repository.MenuItemRepository;
import com.restaurant.order.model.repository.OrderRepository;
import com.restaurant.order.model.repository.StaffCallRepository;
import com.restaurant.order.model.Review;
import com.restaurant.order.model.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class OrderController {

    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private StaffCallRepository staffCallRepository;

    // 1. หน้าหลักเมนูอาหาร (รองรับค้นหาและเลือกหมวดหมู่)
    @GetMapping("/")
    public String index(@RequestParam(defaultValue = "1") Integer table,
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false) String search,
                        HttpSession session,
                        Model model) {
        List<MenuItem> items = menuItemRepository.findAll();

        if (category != null && !category.isEmpty() && !"ทั้งหมด".equals(category)) {
            items = items.stream()
                    .filter(m -> category.equals(m.getCategory()))
                    .collect(Collectors.toList());
        }
        if (search != null && !search.trim().isEmpty()) {
            String kw = search.trim().toLowerCase();
            items = items.stream()
                    .filter(m -> m.getName() != null && m.getName().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }

        Customer currentCustomer = getSessionCustomer(session);
        model.addAttribute("tableNumber", table);
        model.addAttribute("activeCategory", category != null ? category : "ทั้งหมด");
        model.addAttribute("menuItems", items);
        model.addAttribute("customer", currentCustomer);
        return "index";
    }

    // 2. หน้าสถานะครัวของลูกค้า (/order-status)
    @GetMapping("/order-status")
    public String orderStatus(@RequestParam(defaultValue = "1") Integer table, Model model) {
        List<OrderRequest> orders = orderRepository.findByTableNumberOrderByOrderTimeDesc(table);
        double grandTotal = orders.stream()
                .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0.0)
                .sum();

        model.addAttribute("tableNumber", table);
        model.addAttribute("orders", orders);
        model.addAttribute("grandTotal", grandTotal);
        return "order-status";
    }

    // 3. หน้าชำระเงินและใช้โค้ดส่วนลด (/payment)
    @GetMapping("/payment")
    public String paymentPage(@RequestParam(defaultValue = "1") Integer table,
                              @RequestParam(required = false) String promoCode,
                              HttpSession session,
                              Model model) {
        List<OrderRequest> orders = orderRepository.findByTableNumberOrderByOrderTimeDesc(table);
        double subtotal = orders.stream()
                .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0.0)
                .sum();

        double discount = 0.0;
        String promoMessage = null;

        // ตรวจสอบโค้ดส่วนลดตามเอกสาร SRS (3SEASON50, DINEIN25, YUMMY20)
        if (promoCode != null && !promoCode.trim().isEmpty()) {
            String code = promoCode.trim().toUpperCase();
            if ("3SEASON50".equals(code) && subtotal >= 150) {
                discount = 50.0;
                promoMessage = "ใช้โค้ด 3SEASON50 สำเร็จ! ลด ฿50";
            } else if ("DINEIN25".equals(code) && subtotal >= 100) {
                discount = 25.0;
                promoMessage = "ใช้โค้ด DINEIN25 สำเร็จ! ลด ฿25";
            } else if ("YUMMY20".equals(code)) {
                discount = 20.0;
                promoMessage = "ใช้โค้ด YUMMY20 สำเร็จ! ลด ฿20";
            } else {
                promoMessage = "โค้ดไม่ถูกต้อง หรือยอดสั่งซื้อยังไม่ถึงขั้นต่ำ";
            }
        }

        double netTotal = Math.max(0, subtotal - discount);

        model.addAttribute("tableNumber", table);
        model.addAttribute("orders", orders);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", discount);
        model.addAttribute("netTotal", netTotal);
        model.addAttribute("promoCode", promoCode);
        model.addAttribute("promoMessage", promoMessage);
        model.addAttribute("customer", getSessionCustomer(session));
        return "payment";
    }

    // ยืนยันการชำระเงิน
    @PostMapping("/payment/confirm")
    public String confirmPayment(@RequestParam Integer tableNumber,
                                 @RequestParam Double netTotal,
                                 HttpSession session) {
        List<OrderRequest> orders = orderRepository.findByTableNumberOrderByOrderTimeDesc(tableNumber);
        for (OrderRequest o : orders) {
            o.setStatus("Paid");
            orderRepository.save(o);
        }

        Customer currentCustomer = getSessionCustomer(session);
        if (currentCustomer != null) {
            int bonusPoints = (int) (netTotal / 10);
            currentCustomer.setPoints(currentCustomer.getPoints() + bonusPoints);
            customerRepository.save(currentCustomer);
        }

        return "redirect:/payment?table=" + tableNumber + "&paid=true";
    }

    // 4. หน้าสมาชิก / ล็อกอินลูกค้า (/profile)
    @GetMapping("/profile")
    public String profilePage(@RequestParam(defaultValue = "1") Integer table,
                              HttpSession session,
                              Model model) {
        model.addAttribute("tableNumber", table);
        model.addAttribute("customer", getSessionCustomer(session));
        return "profile";
    }

    // สมัครสมาชิก / ล็อกอินด้วยเบอร์โทร
    @PostMapping("/customer/login")
    public String loginOrRegister(@RequestParam Integer tableNumber,
                                  @RequestParam String phone,
                                  @RequestParam(required = false) String name,
                                  HttpSession session) {
        Customer customer = customerRepository.findByPhone(phone.trim())
                .orElseGet(() -> {
                    String displayName = (name != null && !name.trim().isEmpty()) ? name.trim() : "ลูกค้าสมาชิก";
                    return customerRepository.save(new Customer(phone.trim(), displayName));
                });
        session.setAttribute("currentCustomer", customer);
        return "redirect:/profile?table=" + tableNumber;
    }

    // ออกจากระบบลูกค้า
    @GetMapping("/customer/logout")
    public String logout(@RequestParam(defaultValue = "1") Integer table, HttpSession session) {
        session.removeAttribute("currentCustomer");
        return "redirect:/profile?table=" + table;
    }

    // 5. สั่งอาหารเข้าครัว
    @PostMapping("/order/place")
    public String placeOrder(@RequestParam Integer tableNumber,
                             @RequestParam Long menuItemId,
                             @RequestParam Integer quantity,
                             @RequestParam(required = false) String note,
                             HttpSession session) {
        MenuItem item = menuItemRepository.findById(menuItemId).orElseThrow();
        double total = item.getPrice() * quantity;

        item.setSoldCount(item.getSoldCount() + quantity);
        menuItemRepository.save(item);

        OrderRequest order = new OrderRequest();
        order.setTableNumber(tableNumber);
        order.setFoodName(item.getName());
        order.setQuantity(quantity);
        order.setTotalPrice(total);
        order.setNote(note);
        order.setStatus("Queued");
        order.setOrderTime(LocalDateTime.now());

        Customer currentCustomer = getSessionCustomer(session);
        if (currentCustomer != null) {
            order.setCustomerPhone(currentCustomer.getPhone());
            currentCustomer.setPoints(currentCustomer.getPoints() + (int) (total / 10));
            currentCustomer.setTotalOrders(currentCustomer.getTotalOrders() + 1);
            customerRepository.save(currentCustomer);
        }

        orderRepository.save(order);
        return "redirect:/order-status?table=" + tableNumber;
    }

    // 6. เรียกพนักงาน
    @PostMapping("/order/call-staff")
    public String callStaff(@RequestParam Integer tableNumber) {
        staffCallRepository.save(new StaffCall(tableNumber));
        return "redirect:/?table=" + tableNumber + "&called=true";
    }

    private Customer getSessionCustomer(HttpSession session) {
        Customer c = (Customer) session.getAttribute("currentCustomer");
        if (c != null) {
            c = customerRepository.findById(c.getId()).orElse(null);
            session.setAttribute("currentCustomer", c);
        }
        return c;
    }

    // รองรับกรณีเข้าผ่าน URL /login โดยตรง (ส่งต่อไปหน้าสมาชิก /profile)
    @GetMapping("/login")
    public String loginRedirect(@RequestParam(defaultValue = "1") Integer table) {
        return "redirect:/profile?table=" + table;
    }
    
    @Autowired
    private ReviewRepository reviewRepository;

    @PostMapping("/review/submit")
    public String submitReview(@RequestParam Integer tableNumber,
                           @RequestParam Integer rating,
                           @RequestParam(required = false) String comment,
                           HttpSession session) {
    Customer currentCustomer = getSessionCustomer(session);
    String name = (currentCustomer != null) ? currentCustomer.getName() : "ลูกค้าโต๊ะ " + tableNumber;

    reviewRepository.save(new Review(tableNumber, name, rating, comment));
    return "redirect:/payment?table=" + tableNumber + "&reviewed=true";
}
    
}
