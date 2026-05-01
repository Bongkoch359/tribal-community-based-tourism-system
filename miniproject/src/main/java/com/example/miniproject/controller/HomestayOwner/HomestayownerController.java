package com.example.miniproject.controller.HomestayOwner;

import com.example.miniproject.dto.Homestay.RegisterOwnerRequest;
import com.example.miniproject.dto.Homestay.UpdateProfileRequest;
import com.example.miniproject.entity.Homestayowner;
import com.example.miniproject.service.Homestay.HomestayOwnerService;
import com.example.miniproject.service.Homestay.HomestayService;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.example.miniproject.dto.Homestay.HomestayDetailDto;
import com.example.miniproject.dto.Homestay.HomestayDto;

@Controller
public class HomestayownerController {

    @Autowired
    private HomestayOwnerService ownerService;
    @Autowired 
    private HomestayService homestayService;

    // ───── Pages ─────

    @GetMapping("/owner/login")
    public String loginPage() {
        return "Homestay/loginowner";
    }

    @GetMapping("/owner/register")
    public String registerPage() {
        return "Homestay/register";
    }

    @GetMapping("/owner/profile-edit")
    public String editProfile(HttpSession session) {
        if (session.getAttribute("ownerid") == null) return "redirect:/owner/login";
        return "Homestay/editprofile";
    }

    // ───── Register ─────

    @PostMapping("/owner/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody RegisterOwnerRequest req) {
        try {
            ownerService.register(req);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ───── Login ─────

    @PostMapping("/owner/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
                                   HttpSession session) {
        try {
            Homestayowner owner = ownerService.login(
                    body.get("email"),
                    body.get("password")
            );

            session.setAttribute("ownerLogin",   owner);
            session.setAttribute("ownerid",      owner.getOwnerid());
            session.setAttribute("ownername",    owner.getFirstname() + " " + owner.getLastname());

            if (owner.getHomestays() != null && !owner.getHomestays().isEmpty()) {
                session.setAttribute("homestayid",   owner.getHomestays().get(0).getHomestayid());
                session.setAttribute("homestayname", owner.getHomestays().get(0).getHomestayname());
            } else {
                session.setAttribute("homestayid",   null);
                session.setAttribute("homestayname", "ยังไม่มีโฮมสเตย์");
            }

            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ───── Logout ─────

    @GetMapping("/owner/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/owner/login";
    }

    // ───── Get Profile (ดึงจาก Session) ─────
    // GET /owner/profile
    @GetMapping("/owner/profile")
    @ResponseBody
    public ResponseEntity<?> getProfile(HttpSession session) {
        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null) {
            return ResponseEntity.status(401).body(Map.of("message", "กรุณาเข้าสู่ระบบก่อน"));
        }
        try {
            Homestayowner owner = ownerService.getProfile(ownerid);
            return ResponseEntity.ok(toSafeMap(owner));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(errorBody(e.getMessage()));
        }
    }

    // ───── Update Profile (ใช้ Session ownerid) ─────
    // PUT /owner/profile
    @PutMapping("/owner/profile")
    @ResponseBody
    public ResponseEntity<?> updateProfile(
            @RequestBody UpdateProfileRequest req,
            HttpSession session) {

        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null) {
            return ResponseEntity.status(401).body(Map.of("message", "กรุณาเข้าสู่ระบบก่อน"));
        }
        try {
            Homestayowner updated = ownerService.updateProfile(ownerid, req);

            // อัปเดต session ให้ตรงกับข้อมูลใหม่
            session.setAttribute("ownername", updated.getFirstname() + " " + updated.getLastname());

            return ResponseEntity.ok(toSafeMap(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    // ───── Change Password (ใช้ Session ownerid) ─────
    // PUT /owner/change-password
    @PutMapping("/owner/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> body,
            HttpSession session) {

        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null) {
            return ResponseEntity.status(401).body(Map.of("message", "กรุณาเข้าสู่ระบบก่อน"));
        }
        try {
            ownerService.changePassword(
                    ownerid,
                    body.get("currentPassword"),
                    body.get("newPassword")
            );
            return ResponseEntity.ok(Map.of("success", true, "message", "เปลี่ยนรหัสผ่านสำเร็จ"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ───── Helpers ─────

    private Map<String, Object> toSafeMap(Homestayowner o) {
        Map<String, Object> map = new HashMap<>();
        map.put("ownerid",            o.getOwnerid());
        map.put("firstname",          o.getFirstname());
        map.put("lastname",           o.getLastname());
        map.put("email",              o.getEmail());
        map.put("phone",              o.getPhone());
        map.put("verificationstatus", o.getVerificationstatus());
        map.put("accountstatus",      o.getAccountstatus());
        return map;
    }

    private Map<String, String> errorBody(String message) {
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        return body;
    }

      @GetMapping("/owner/homestays")
public String homestaysPage(HttpSession session, Model model) {
    // ✅ เปลี่ยนจาก ownerLogin → ownerid
    Integer ownerid = (Integer) session.getAttribute("ownerid");
    if (ownerid == null) return "redirect:/owner/login";

    // ดึง owner จาก DB ใหม่เลย
    Homestayowner owner = ownerService.getProfile(ownerid);
    List<HomestayDto> homestays = homestayService.getHomestaysByOwner(owner);
    
    model.addAttribute("ownername", session.getAttribute("ownername"));
    model.addAttribute("homestays", homestays);
    return "Homestay/homestays";
}

@GetMapping("/owner/homestays/{id}")
public String viewHomestay(@PathVariable Integer id,
                           HttpSession session,
                           Model model) {

    // ✅ เปลี่ยนจาก ownerLogin → ownerid
    Integer ownerid = (Integer) session.getAttribute("ownerid");
    if (ownerid == null) return "redirect:/owner/login";

    HomestayDetailDto detail = homestayService.getHomestayDetail(id);
    if (detail == null) return "redirect:/owner/homestays";

    model.addAttribute("ownername", session.getAttribute("ownername"));
    model.addAttribute("detail", detail);
    return "Homestay/viewHomestay";
}
}
