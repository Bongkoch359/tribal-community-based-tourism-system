package com.example.miniproject.controller.HomestayOwner;

import com.example.miniproject.dto.Homestay.AddRoomRequest;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Roomtype;
import com.example.miniproject.service.Homestay.HomestayService;
import com.example.miniproject.service.Homestay.RoomTypeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class RoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;

    @Autowired
    private HomestayService homestayService;

    // ─── GET: รายการห้องพักทั้งหมด ───────────────────────────
    @GetMapping("/owner/rooms")
    public String listRooms(
            @RequestParam(value = "homestayid", required = false) Integer homestayid,
            HttpSession session,
            Model model) {

        if (session.getAttribute("ownerid") == null) return "redirect:/owner/login";

        String ownername = (String) session.getAttribute("ownername");

        // ถ้าไม่ส่ง homestayid มา → ดึงจาก session
        if (homestayid == null) {
            homestayid = (Integer) session.getAttribute("homestayid");
        }

        String homestayname = "";
        if (homestayid != null) {
            Homestay hs = homestayService.getHomestayById(homestayid);
            if (hs != null) homestayname = hs.getHomestayname();
        }

        List<Roomtype> rooms = (homestayid != null)
                ? roomTypeService.getRoomTypesByHomestayId(homestayid)
                : Collections.emptyList();

        // คำนวณ firstImageUrl ให้แต่ละห้อง ส่งเป็น Map แทน
        List<Map<String, Object>> roomViews = rooms.stream().map(room -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("roomtypeid",   room.getRoomtypeid());
            m.put("typename",     room.getTypename());
            m.put("pricepernight", room.getPricepernight());
            m.put("maxguest",     room.getMaxguest());
            m.put("totalrooms",   room.getTotalrooms());
            m.put("status",       room.getStatus());

            // แยกรูปแรก: Base64 คั่น "||", path คั่น ","
            String firstImg = null;
            String imgs = room.getImages();
            if (imgs != null && !imgs.isBlank()) {
             // Base64 หลายรูปคั่นด้วย ",data:" — เอาแค่รูปแรก
                 int nextIdx = imgs.indexOf(",data:");
                 firstImg = (nextIdx > 0) ? imgs.substring(0, nextIdx) : imgs;
            }
            m.put("firstImageUrl", firstImg);
            return m;
        }).collect(java.util.stream.Collectors.toList());

        model.addAttribute("ownername",    ownername != null ? ownername : "Owner");
        model.addAttribute("homestayid",   homestayid);
        model.addAttribute("homestayname", homestayname);
        model.addAttribute("rooms",        roomViews);
        return "Homestay/listRoom";
    }

    // ─── GET: แสดงฟอร์มเพิ่มห้อง ─────────────────────────────
    @GetMapping("/addroom")
    public String showAddRoomForm(
            @RequestParam("homestayid") Integer homestayid,
            @RequestParam(value = "homestayname", defaultValue = "") String homestayname,
            @SessionAttribute(name = "ownername", required = false) String ownername,
            Model model) {

        model.addAttribute("homestayid",   homestayid);
        model.addAttribute("homestayname", homestayname);
        model.addAttribute("ownername",    ownername != null ? ownername : "Owner");
        return "Homestay/addRoom";
    }

    // ─── POST: รับ JSON + Base64 ──────────────────────────────
    @PostMapping("/addroom")
    @ResponseBody
    public ResponseEntity<?> addRoom(@RequestBody AddRoomRequest req,
                                     HttpSession session) {
        try {
            roomTypeService.addRoomType(req);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

   // ─── เพิ่มใน RoomTypeController.java ───────────────────────────────────────

// // GET: ดูรายละเอียดห้องพัก
    @GetMapping("/owner/room/view")
    public String viewRoom(
        @RequestParam("roomtypeid") String roomtypeid,
        HttpSession session,
        Model model) {

    if (session.getAttribute("ownerid") == null) return "redirect:/owner/login";

    Roomtype room = roomTypeService.getRoomTypeById(roomtypeid);
    if (room == null) return "redirect:/owner/rooms";

    // ✅ แยกรูปทุกรูปเป็น List<String>
    List<String> imageList = new java.util.ArrayList<>();
    String imgs = room.getImages();
    if (imgs != null && !imgs.isBlank()) {
        // Base64 หลายรูปคั่นด้วย ",data:" — split ให้ถูกต้อง
        String[] parts = imgs.split("(?=,data:)");
        for (String part : parts) {
            if (!part.isBlank()) imageList.add(part.startsWith(",") ? part.substring(1) : part);
        }
    }

    // ✅ ดึงชื่อ facilities
    List<String> facilities = java.util.Collections.emptyList();
    if (room.getFacilities() != null && !room.getFacilities().isEmpty()) {
        facilities = room.getFacilities().stream()
                .map(f -> f.getFacilitiesname())
                .collect(java.util.stream.Collectors.toList());
    }

    model.addAttribute("ownername",  session.getAttribute("ownername") != null ? session.getAttribute("ownername") : "Owner");
    model.addAttribute("room",       room);
    model.addAttribute("imageList",  imageList);   // ✅ เปลี่ยนจาก firstImageUrl → imageList
    model.addAttribute("facilities", facilities);

    return "Homestay/viewRoom";
    }
}