package com.restaurant.controller;

import com.restaurant.order.model.Customer;
import com.restaurant.entity.MenuItem;
import com.restaurant.order.model.OrderRequest;
import com.restaurant.order.model.StaffCall;
import com.restaurant.order.model.repository.CustomerRepository;
import com.restaurant.order.model.repository.MenuItemRepository;
import com.restaurant.order.model.repository.OrderRepository;
import com.restaurant.order.model.repository.PromotionRepository;
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
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private PromotionRepository promotionRepository;

    private boolean isUnpaid(OrderRequest o) {
        return !"Paid".equalsIgnoreCase(o.getStatus())
            && !"Archived".equalsIgnoreCase(o.getStatus());
    }

    private double sumPrice(List<OrderRequest> list) {
        return list.stream()
                .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0.0)
                .sum();
    }

    // คำนวณส่วนลดตาม SRS (ใช้ทั้งหน้าชำระเงินและตอนยืนยัน)
    private double calcDiscount(String promoCode, double subtotal) {
        if (promoCode == null || promoCode.trim().isEmpty()) return 0.0;
        String code = promoCode.trim().toUpperCase();

        // 1. ค้นหาจากฐานข้อมูลโปรโมชันที่ Admin สร้างไว้ (FR-10)
        java.time.LocalDate today = java.time.LocalDate.now();
        for (com.restaurant.order.model.Promotion p : promotionRepository.findAll()) {
            if (p.isActive()
                    && code.equalsIgnoreCase(p.getPromoCode())
                    && !today.isBefore(p.getStartDate())
                    && !today.isAfter(p.getEndDate())
                    && subtotal >= p.getMinOrderAmount()) {
                if ("PERCENT".equalsIgnoreCase(p.getDiscountType())) {
                    return Math.round(subtotal * (p.getDiscountValue() / 100.0));
                }
                return p.getDiscountValue();
            }
        }

        // 2. โค้ดมาตรฐานตาม SRS (Fallback)
        if ("3SEASON50".equals(code) && subtotal >= 150) return 50.0;
        if ("DINEIN25".equals(code) && subtotal >= 100) return 25.0;
        if ("YUMMY20".equals(code)) return 20.0;
        return 0.0;
    }

    @GetMapping("/payment")
    public String paymentPage(@RequestParam(defaultValue = "1") Integer table,
                              @RequestParam(required = false) String promoCode,
                              @RequestParam(required = false) String paid,
                              @RequestParam(required = false) String reviewed,
                              HttpSession session,
                              Model model) {
        List<OrderRequest> orders;
        double discount;
        String promoMessage = null;

        @SuppressWarnings("unchecked")
        List<Long> receiptIds = (List<Long>) session.getAttribute("receiptIds_" + table);
        Double receiptDiscount = (Double) session.getAttribute("receiptDiscount_" + table);

        if (paid != null && reviewed == null && receiptIds != null && receiptDiscount != null) {
            // ใบเสร็จของบิลที่เพิ่งจ่าย: เฉพาะออเดอร์ในบิลนั้น + ส่วนลดที่ใช้จริง
            orders = orderRepository.findAllById(receiptIds);
            discount = receiptDiscount;
        } else {
            // ปกติ / หลังรีวิว: เอาเฉพาะออเดอร์ที่ยังไม่จ่าย
            orders = orderRepository.findByTableNumberOrderByOrderTimeDesc(table)
                    .stream().filter(this::isUnpaid).collect(Collectors.toList());
            discount = calcDiscount(promoCode, sumPrice(orders));
            if (promoCode != null && !promoCode.trim().isEmpty()) {
                promoMessage = discount > 0
                        ? "ใช้โค้ด " + promoCode.trim().toUpperCase() + " สำเร็จ! ลด ฿" + (int) discount
                        : "โค้ดไม่ถูกต้อง หรือยอดสั่งซื้อยังไม่ถึงขั้นต่ำ";
            }
        }

        double subtotal = sumPrice(orders);
        double netTotal = Math.max(0, subtotal - discount);

        model.addAttribute("tableNumber", table);
        model.addAttribute("orders", orders);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", discount);
        model.addAttribute("netTotal", netTotal);
        model.addAttribute("promoCode", promoCode);
        model.addAttribute("promoMessage", promoMessage);
        model.addAttribute("customer", getSessionCustomer(session));
    
        java.time.LocalDate today = java.time.LocalDate.now();
        List<com.restaurant.order.model.Promotion> activePromos = promotionRepository.findAll().stream()
                .filter(p -> p.isActive() && !today.isBefore(p.getStartDate()) && !today.isAfter(p.getEndDate()))
                .collect(Collectors.toList());
        model.addAttribute("activePromos", activePromos);

        return "payment";
    }

    @PostMapping("/payment/confirm")
    public String confirmPayment(@RequestParam Integer tableNumber,
                                 @RequestParam(required = false) String promoCode,
                                 HttpSession session) {
        List<OrderRequest> unpaid = orderRepository.findByTableNumberOrderByOrderTimeDesc(tableNumber)
                .stream().filter(this::isUnpaid).collect(Collectors.toList());

        // กดซ้ำ / ไม่มีรายการค้างจ่าย -> ไม่ทำอะไร ไม่บวกแต้มซ้ำ
        if (unpaid.isEmpty()) {
            return "redirect:/payment?table=" + tableNumber;
        }

        double discount = calcDiscount(promoCode, sumPrice(unpaid));
        double netTotal = Math.max(0, sumPrice(unpaid) - discount);

        for (OrderRequest o : unpaid) {
            o.setStatus("Paid");
            orderRepository.save(o);
        }

        // จดไว้ให้หน้าใบเสร็จใช้
        session.setAttribute("receiptIds_" + tableNumber,
                unpaid.stream().map(OrderRequest::getId).collect(Collectors.toList()));
        session.setAttribute("receiptDiscount_" + tableNumber, discount);

        Customer currentCustomer = getSessionCustomer(session);
        if (currentCustomer != null) {
            int bonusPoints = (int) (netTotal / 10);
            currentCustomer.setPoints(currentCustomer.getPoints() + bonusPoints);
            customerRepository.save(currentCustomer);
        }

        return "redirect:/payment?table=" + tableNumber + "&paid=true";
    }

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
    public String orderStatus(@RequestParam(defaultValue = "1") Integer table,
                              HttpSession session,
                              Model model) {
        List<OrderRequest> orders = orderRepository.findByTableNumberOrderByOrderTimeDesc(table)
                .stream()
                .filter(o -> !"Paid".equalsIgnoreCase(o.getStatus()) && !"Archived".equalsIgnoreCase(o.getStatus()))
                .collect(Collectors.toList());

        double grandTotal = orders.stream()
                .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0.0)
                .sum();

        model.addAttribute("tableNumber", table);
        model.addAttribute("orders", orders);
        model.addAttribute("activeOrders", orders);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("customer", getSessionCustomer(session));
        return "order-status";
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

    // 5. สั่งอาหารเข้าครัว (บันทึกเฉพาะจำนวนครั้งที่สั่ง ยังไม่บวกแต้มจนกว่าจะชำระเงิน)
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