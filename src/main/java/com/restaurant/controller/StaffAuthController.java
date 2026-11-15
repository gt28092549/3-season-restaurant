package com.restaurant.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StaffAuthController {

    // 1. หน้าล็อกอินของพนักงานและแอดมิน (รองรับทั้ง /staff/login และ /admin/login)
    @GetMapping({"/staff/login", "/admin/login"})
    public String staffLoginPage(@RequestParam(required = false) String error,
                                 @RequestParam(required = false) String logout,
                                 Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "รหัสพนักงานหรือรหัสผ่านไม่ถูกต้อง");
        }
        if (logout != null) {
            model.addAttribute("logoutMsg", "ออกจากระบบเรียบร้อยแล้ว");
        }
        return "staff-login";
    }

    // 2. ตรวจสอบรหัสผ่านและแยกบทบาท (Staff vs Admin)
    @PostMapping({"/staff/login", "/admin/login"})
    public String doStaffLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session) {
        String user = username.trim().toLowerCase();

        // บัญชีพนักงานครัว / เสิร์ฟ
        if ("staff".equals(user) && "1234".equals(password)) {
            session.setAttribute("staffUser", "พนักงานครัว (Staff)");
            session.setAttribute("staffRole", "STAFF");
            return "redirect:/staff/queue";
        }

        // บัญชีผู้จัดการร้าน (Admin)
        if ("admin".equals(user) && "admin1234".equals(password)) {
            session.setAttribute("staffUser", "ผู้จัดการร้าน (Admin)");
            session.setAttribute("staffRole", "ADMIN");
            return "redirect:/admin";
        }

        return "redirect:/staff/login?error=true";
    }

    // 3. ออกจากระบบพนักงานและแอดมิน
    @GetMapping({"/staff/logout", "/admin/logout"})
    public String staffLogout(HttpSession session) {
        session.removeAttribute("staffUser");
        session.removeAttribute("staffRole");
        return "redirect:/staff/login?logout=true";
    }
}