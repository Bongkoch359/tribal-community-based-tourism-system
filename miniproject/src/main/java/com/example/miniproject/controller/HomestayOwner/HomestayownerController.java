package com.example.miniproject.controller.HomestayOwner;

import com.example.miniproject.dto.Homestay.RegisterOwnerRequest;
import com.example.miniproject.dto.Homestay.HomestayDetailDto;
import com.example.miniproject.dto.Homestay.HomestayDto;
import com.example.miniproject.dto.Homestay.LoginRequest;
import com.example.miniproject.entity.Homestayowner;
import com.example.miniproject.repository.Homestay.HomestayOwnerRepository;
import com.example.miniproject.service.Homestay.HomestayOwnerService;
import com.example.miniproject.service.Homestay.HomestayService;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class HomestayownerController {

    @Autowired
    private HomestayOwnerRepository ownerRepository;
    @Autowired
    private HomestayOwnerService ownerService;
    @Autowired
    private HomestayService homestayService;

    // ───── Pages ─────

    @GetMapping("/owner/login")
    public String loginPage() {
        return "Homestay/loginowner";
    }

    @PostMapping(value = "/owner/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpSession session) {
        try {
            Homestayowner owner = ownerService.login(req.getEmail(), req.getPassword());

            if ("SUSPENDED".equals(owner.getAccountstatus())) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "บัญชีของคุณถูกระงับการใช้งาน กรุณาติดต่อผู้ดูแลระบบ"));
            }

            session.setAttribute("ownerLogin", owner);
            session.setAttribute("ownerid", owner.getOwnerid());
            session.setAttribute("ownername", owner.getFirstname() + " " + owner.getLastname());

            if (owner.getHomestays() != null && !owner.getHomestays().isEmpty()) {
                session.setAttribute("homestayid", owner.getHomestays().get(0).getHomestayid());
                session.setAttribute("homestayname", owner.getHomestays().get(0).getHomestayname());
            } else {
                session.setAttribute("homestayid", null);
                session.setAttribute("homestayname", "ยังไม่มีโฮมสเตย์");
            }

            return ResponseEntity.ok(Map.of("success", true));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/owner/register")
    public String registerPage() {

        return "Homestay/register";
    }

    @GetMapping("/owner/check-email")
    @ResponseBody
    public Map<String, Boolean> checkEmail(@RequestParam String email) {
        boolean exists = ownerRepository.existsByEmail(email); // ปรับตาม repository จริง
        return Map.of("exists", exists);
    }

    // ───── Edit Profile Page ─────

    @GetMapping("/owner/profile-edit")
    public String editProfile(HttpSession session, Model model) {
        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null)
            return "redirect:/owner/login";

        Homestayowner owner = ownerService.getProfile(ownerid);
        model.addAttribute("loggedInOwner", owner);
        return "Homestay/editprofile";
    }

    // ═══════════════════════════════════════════════════
    // ───── Register — form-submit (multipart) ─────
    // ═══════════════════════════════════════════════════

    @PostMapping(value = "/owner/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<?> register(RegisterOwnerRequest req) {
        try {
            List<Integer> homestayIds = ownerService.register(req);

            List<RegisterOwnerRequest.HomestayItem> items = req.getHomestays();
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    RegisterOwnerRequest.HomestayItem item = items.get(i);
                    if (item.getImages() == null || item.getImages().isEmpty())
                        continue;

                    Integer hsId = homestayIds.get(i);
                    String uploadDir = System.getProperty("user.dir") + "/uploads/homestays/" + hsId + "/";
                    Files.createDirectories(Paths.get(uploadDir));

                    List<String> savedPaths = new ArrayList<>();
                    for (MultipartFile file : item.getImages()) {
                        if (file == null || file.isEmpty())
                            continue;
                        String ext = getExtension(file.getOriginalFilename());
                        String filename = UUID.randomUUID().toString() + ext;
                        Path dest = Paths.get(uploadDir + filename);
                        file.transferTo(dest);
                        savedPaths.add("/uploads/homestays/" + hsId + "/" + filename);
                    }

                    if (!savedPaths.isEmpty()) {
                        Map<String, String> imgUpdate = new HashMap<>();
                        imgUpdate.put("images", String.join(",", savedPaths));
                        homestayService.updateImages(hsId, imgUpdate);
                    }
                }
            }

            return ResponseEntity.ok(Map.of("success", true));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", "บันทึกรูปภาพไม่สำเร็จ: " + e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════
    // ───── Login — form-submit ─────
    // ═══════════════════════════════════════════════════

    @PostMapping("/owner/login")
    public String login(@RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Homestayowner owner = ownerService.login(email, password);

            if ("SUSPENDED".equals(owner.getAccountstatus())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "บัญชีของคุณถูกระงับการใช้งาน กรุณาติดต่อผู้ดูแลระบบ");
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/owner/login";
            }

            session.setAttribute("ownerLogin", owner);
            session.setAttribute("ownerid", owner.getOwnerid());
            session.setAttribute("ownername", owner.getFirstname() + " " + owner.getLastname());

            if (owner.getHomestays() != null && !owner.getHomestays().isEmpty()) {
                session.setAttribute("homestayid", owner.getHomestays().get(0).getHomestayid());
                session.setAttribute("homestayname", owner.getHomestays().get(0).getHomestayname());
            } else {
                session.setAttribute("homestayid", null);
                session.setAttribute("homestayname", "ยังไม่มีโฮมสเตย์");
            }

            return "redirect:/owner/dashboard";

        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("ยังไม่ได้รับการอนุมัติ")) {
                redirectAttributes.addFlashAttribute("pendingMessage", msg);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", msg);
            }
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/owner/login";
        }
    }

    // ───── Logout ─────

    @GetMapping("/owner/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/owner/login";
    }

    // ═══════════════════════════════════════════════════
    // ───── Update Profile (ข้อมูลส่วนตัว) — form-submit ─────
    // ═══════════════════════════════════════════════════

    @PostMapping("/owner/profile/update")
    public String updateProfile(@RequestParam String firstname,
            @RequestParam String lastname,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null)
            return "redirect:/owner/login";

        try {
            Homestayowner updated = ownerService.updateProfile(ownerid, firstname, lastname, email, phone);
            session.setAttribute("ownername", updated.getFirstname() + " " + updated.getLastname());
            redirectAttributes.addFlashAttribute("successMessage", "บันทึกข้อมูลส่วนตัวสำเร็จ");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/profile-edit";
    }

    // ═══════════════════════════════════════════════════
    // ───── Update Bank Info — form-submit ─────
    // ═══════════════════════════════════════════════════

    @PostMapping("/owner/profile/update-bank")
    public String updateBankInfo(@RequestParam String bankName,
            @RequestParam String accountName,
            @RequestParam String accountNumber,
            @RequestParam(required = false) String bankBranch,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null)
            return "redirect:/owner/login";

        try {
            ownerService.updateBankInfo(ownerid, bankName, accountName, accountNumber, bankBranch);
            redirectAttributes.addFlashAttribute("successMessage", "บันทึกข้อมูลธนาคารสำเร็จ");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/profile-edit";
    }

    // ═══════════════════════════════════════════════════
    // ───── Update Signature — form-submit (multipart) ─────
    // ═══════════════════════════════════════════════════

    @PostMapping(value = "/owner/profile/update-signature", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateSignature(@RequestParam("signatureFile") MultipartFile signatureFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null)
            return "redirect:/owner/login";

        if (signatureFile == null || signatureFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "กรุณาเลือกไฟล์ลายเซ็น");
            return "redirect:/owner/profile-edit";
        }

        String contentType = signatureFile.getContentType();
        boolean validType = contentType != null &&
                (contentType.equals("image/png") || contentType.equals("image/jpeg"));
        if (!validType) {
            redirectAttributes.addFlashAttribute("errorMessage", "รองรับเฉพาะไฟล์ PNG หรือ JPG เท่านั้น");
            return "redirect:/owner/profile-edit";
        }
        if (signatureFile.getSize() > 2 * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("errorMessage", "ขนาดไฟล์ต้องไม่เกิน 2MB");
            return "redirect:/owner/profile-edit";
        }

        try {
            String base64 = Base64.getEncoder().encodeToString(signatureFile.getBytes());
            String dataUrl = "data:" + contentType + ";base64," + base64;
            ownerService.updateSignature(ownerid, dataUrl);
            redirectAttributes.addFlashAttribute("successMessage", "อัปโหลดลายเซ็นสำเร็จ");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "อัปโหลดลายเซ็นไม่สำเร็จ: " + e.getMessage());
        }
        return "redirect:/owner/profile-edit";
    }

    // ═══════════════════════════════════════════════════
    // ───── Change Password — form-submit ─────
    // ═══════════════════════════════════════════════════

    @PostMapping("/owner/change-password")
    public String changePassword(@RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null)
            return "redirect:/owner/login";

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "รหัสผ่านใหม่และยืนยันรหัสผ่านไม่ตรงกัน");
            return "redirect:/owner/profile-edit";
        }

        try {
            ownerService.changePassword(ownerid, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "เปลี่ยนรหัสผ่านสำเร็จ");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/profile-edit";
    }

    // ───── Homestays List ─────

    @GetMapping("/owner/homestays")
    public String homestaysPage(HttpSession session, Model model) {
        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null)
            return "redirect:/owner/login";

        Homestayowner owner = ownerService.getProfile(ownerid);
        List<HomestayDto> homestays = homestayService.getHomestaysByOwner(owner);

        model.addAttribute("ownername", session.getAttribute("ownername"));
        model.addAttribute("homestays", homestays);
        return "Homestay/homestays";
    }

    // ───── View Homestay Detail ─────

    @GetMapping("/owner/homestays/{id}")
    public String viewHomestay(@PathVariable Integer id,
            HttpSession session,
            Model model) {
        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null)
            return "redirect:/owner/login";

        HomestayDetailDto detail = homestayService.getHomestayDetail(id);
        if (detail == null)
            return "redirect:/owner/homestays";

        model.addAttribute("ownername", session.getAttribute("ownername"));
        model.addAttribute("detail", detail);
        return "Homestay/viewHomestay";
    }

    // ───── Edit Homestay Page ─────

    @GetMapping("/owner/homestays/{id}/edit")
    public String editHomestayPage(@PathVariable Integer id,
            HttpSession session,
            Model model) {
        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null)
            return "redirect:/owner/login";

        if (!homestayService.isOwnedBy(id, ownerid))
            return "redirect:/owner/homestays";

        HomestayDetailDto detail = homestayService.getHomestayDetail(id);
        if (detail == null)
            return "redirect:/owner/homestays";

        model.addAttribute("ownername", session.getAttribute("ownername"));
        model.addAttribute("detail", detail);
        return "Homestay/editHomestay";
    }

    // ───── Update Homestay (multipart) ─────

    @PutMapping(value = "/owner/homestays/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<?> updateHomestay(
            @PathVariable Integer id,
            @RequestParam("homestayname") String homestayname,
            @RequestParam("address") String address,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "keepImages", required = false) String keepImages,
            @RequestParam(value = "newImages", required = false) List<MultipartFile> newImages,
            HttpSession session) {

        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null)
            return ResponseEntity.status(401).body(Map.of("message", "กรุณาเข้าสู่ระบบก่อน"));

        if (!homestayService.isOwnedBy(id, ownerid))
            return ResponseEntity.status(403).body(Map.of("message", "ไม่มีสิทธิ์แก้ไขโฮมสเตย์นี้"));

        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/homestays/" + id + "/";
            Files.createDirectories(Paths.get(uploadDir));

            List<String> allPaths = new ArrayList<>();

            if (keepImages != null && !keepImages.isBlank()) {
                for (String p : keepImages.split(",")) {
                    String trimmed = p.trim();
                    if (!trimmed.isBlank())
                        allPaths.add(trimmed);
                }
            }

            if (newImages != null) {
                for (MultipartFile file : newImages) {
                    if (file == null || file.isEmpty())
                        continue;
                    String ext = getExtension(file.getOriginalFilename());
                    String filename = UUID.randomUUID().toString() + ext;
                    Path dest = Paths.get(uploadDir + filename);
                    file.transferTo(dest);
                    allPaths.add("/uploads/homestays/" + id + "/" + filename);
                }
            }

            String imagesValue = allPaths.isEmpty() ? null : String.join(",", allPaths);

            Map<String, String> req = new HashMap<>();
            req.put("homestayname", homestayname);
            req.put("address", address);
            req.put("description", description);
            req.put("images", imagesValue);
            homestayService.updateHomestay(id, req);

            return ResponseEntity.ok(Map.of("success", true, "message", "บันทึกข้อมูลสำเร็จ"));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "บันทึกรูปภาพไม่สำเร็จ: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ───── Helpers ดึงนามสกุลไฟล์ ─────
    private String getExtension(String filename) {
        if (filename == null || !filename.contains("."))
            return ".jpg";
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }
}