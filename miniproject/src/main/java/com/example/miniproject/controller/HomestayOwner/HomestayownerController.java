package com.example.miniproject.controller.HomestayOwner;

import com.example.miniproject.dto.Homestay.RegisterOwnerRequest;
import com.example.miniproject.dto.Homestay.UpdateProfileRequest;
import com.example.miniproject.dto.Homestay.HomestayDetailDto;
import com.example.miniproject.dto.Homestay.HomestayDto;
import com.example.miniproject.entity.Homestayowner;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

    // ───── Register (multipart/form-data) ─────
    /*
     *  JS ส่งมาในรูป FormData:
     *    firstname, lastname, email, phone, password
     *    homestays[0].homestayname, homestays[0].address,
     *    homestays[0].description, homestays[0].images  (File)
     *    homestays[1].homestayname, ...
     */
    @PostMapping(value = "/owner/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<?> register(RegisterOwnerRequest req) {
        try {
            // 1) บันทึก owner + homestay (ยังไม่มีรูป) → ได้ List<Integer> homestayIds
            List<Integer> homestayIds = ownerService.register(req);

            // 2) บันทึกรูปภาพของแต่ละ homestay
            List<RegisterOwnerRequest.HomestayItem> items = req.getHomestays();
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    RegisterOwnerRequest.HomestayItem item = items.get(i);
                    if (item.getImages() == null || item.getImages().isEmpty()) continue;

                    Integer hsId      = homestayIds.get(i);
                    String  uploadDir = System.getProperty("user.dir") + "/uploads/homestays/" + hsId + "/";
                    Files.createDirectories(Paths.get(uploadDir));

                    List<String> savedPaths = new ArrayList<>();
                    for (MultipartFile file : item.getImages()) {
                        if (file == null || file.isEmpty()) continue;
                        String ext      = getExtension(file.getOriginalFilename());
                        String filename = UUID.randomUUID().toString() + ext;
                        Path   dest     = Paths.get(uploadDir + filename);
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
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "บันทึกรูปภาพไม่สำเร็จ: " + e.getMessage()));
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

    // ───── Get Profile ─────

    @GetMapping("/owner/profile")
    @ResponseBody
    public ResponseEntity<?> getProfile(HttpSession session) {
        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null)
            return ResponseEntity.status(401).body(Map.of("message", "กรุณาเข้าสู่ระบบก่อน"));
        try {
            Homestayowner owner = ownerService.getProfile(ownerid);
            return ResponseEntity.ok(toSafeMap(owner));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(errorBody(e.getMessage()));
        }
    }

    // ───── Update Profile ─────

    @PutMapping("/owner/profile")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest req,
                                           HttpSession session) {
        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null)
            return ResponseEntity.status(401).body(Map.of("message", "กรุณาเข้าสู่ระบบก่อน"));
        try {
            Homestayowner updated = ownerService.updateProfile(ownerid, req);
            session.setAttribute("ownername", updated.getFirstname() + " " + updated.getLastname());
            return ResponseEntity.ok(toSafeMap(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    // ───── Change Password ─────

    @PutMapping("/owner/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body,
                                            HttpSession session) {
        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null)
            return ResponseEntity.status(401).body(Map.of("message", "กรุณาเข้าสู่ระบบก่อน"));
        try {
            ownerService.changePassword(ownerid, body.get("currentPassword"), body.get("newPassword"));
            return ResponseEntity.ok(Map.of("success", true, "message", "เปลี่ยนรหัสผ่านสำเร็จ"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ───── Homestays List ─────

    @GetMapping("/owner/homestays")
    public String homestaysPage(HttpSession session, Model model) {
        Integer ownerid = (Integer) session.getAttribute("ownerid");
        if (ownerid == null) return "redirect:/owner/login";

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
        if (ownerid == null) return "redirect:/owner/login";

        HomestayDetailDto detail = homestayService.getHomestayDetail(id);
        if (detail == null) return "redirect:/owner/homestays";

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
        if (ownerid == null) return "redirect:/owner/login";

        if (!homestayService.isOwnedBy(id, ownerid)) return "redirect:/owner/homestays";

        HomestayDetailDto detail = homestayService.getHomestayDetail(id);
        if (detail == null) return "redirect:/owner/homestays";

        model.addAttribute("ownername", session.getAttribute("ownername"));
        model.addAttribute("detail", detail);
        return "Homestay/editHomestay";
    }

    // ───── Update Homestay (multipart) ─────

    @PutMapping(value = "/owner/homestays/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<?> updateHomestay(
            @PathVariable Integer id,
            @RequestParam("homestayname")                          String homestayname,
            @RequestParam("address")                               String address,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "keepImages",  required = false) String keepImages,
            @RequestParam(value = "newImages",   required = false) List<MultipartFile> newImages,
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
                    if (!trimmed.isBlank()) allPaths.add(trimmed);
                }
            }

            if (newImages != null) {
                for (MultipartFile file : newImages) {
                    if (file == null || file.isEmpty()) continue;
                    String ext      = getExtension(file.getOriginalFilename());
                    String filename = UUID.randomUUID().toString() + ext;
                    Path   dest     = Paths.get(uploadDir + filename);
                    file.transferTo(dest);
                    allPaths.add("/uploads/homestays/" + id + "/" + filename);
                }
            }

            String imagesValue = allPaths.isEmpty() ? null : String.join(",", allPaths);

            Map<String, String> req = new HashMap<>();
            req.put("homestayname", homestayname);
            req.put("address",      address);
            req.put("description",  description);
            req.put("images",       imagesValue);
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

    // ───── Helpers ─────

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }

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
}