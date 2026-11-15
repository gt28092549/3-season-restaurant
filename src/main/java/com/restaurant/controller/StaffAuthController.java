package com.restaurant.controller;

import com.restaurant.order.model.StaffAccount;
import com.restaurant.order.model.repository.StaffAccountRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class StaffAuthController {

    @Autowired
    private StaffAccountRepository staffAccountRepository;

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

        // บัญชีพนักงานครัว / เสิร์ฟ (บัญชีหลัก)
        if ("staff".equals(user) && "1234".equals(password)) {
            session.setAttribute("staffUser", "พนักงานครัว (Staff)");
            session.setAttribute("staffRole", "STAFF");
            return "redirect:/staff/queue";
        }

        // บัญชีผู้จัดการร้าน (Admin บัญชีหลัก)
        if ("admin".equals(user) && "admin1234".equals(password)) {
            session.setAttribute("staffUser", "ผู้จัดการร้าน (Admin)");
            session.setAttribute("staffRole", "ADMIN");
            return "redirect:/admin/dashboard";
        }

        // ตรวจสอบบัญชีพนักงานที่ Admin สร้างเพิ่มในฐานข้อมูล (FR-09)
        Optional<StaffAccount> dbStaff = staffAccountRepository.findByUsernameAndPassword(user, password);
        if (dbStaff.isPresent()) {
            StaffAccount account = dbStaff.get();
            session.setAttribute("staffUser", account.getFullName() + " (" + account.getPosition() + ")");
            session.setAttribute("staffRole", account.getRole());

            if ("ADMIN".equalsIgnoreCase(account.getRole())) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/staff/queue";
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