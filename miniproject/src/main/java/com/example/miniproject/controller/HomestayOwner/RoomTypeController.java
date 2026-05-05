package com.example.miniproject.controller.HomestayOwner;

import com.example.miniproject.dto.Homestay.AddRoomRequest;
import com.example.miniproject.dto.Homestay.UpdateRoomRequest;
import com.example.miniproject.entity.Facilities;
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

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class RoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;

    @Autowired
    private HomestayService homestayService;

    // ─── GET: รายการห้องพักทั้งหมด ───────────────────────────────────────────
    @GetMapping("/owner/rooms")
    public String listRooms(
            @RequestParam(value = "homestayid", required = false) Integer homestayid,
            HttpSession session,
            Model model) {

        if (session.getAttribute("ownerid") == null) return "redirect:/owner/login";

        String ownername = (String) session.getAttribute("ownername");

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

        List<Map<String, Object>> roomViews = rooms.stream().map(room -> {
            Map<String, Object> m = new HashMap<>();
            m.put("roomtypeid",    room.getRoomtypeid());
            m.put("typename",      room.getTypename());
            m.put("pricepernight", room.getPricepernight());
            m.put("maxguest",      room.getMaxguest());
            m.put("totalrooms",    room.getTotalrooms());
            m.put("status",        room.getStatus());

            String firstImg = null;
            String imgs = room.getImages();
            if (imgs != null && !imgs.isBlank()) {
                int nextIdx = imgs.indexOf(",data:");
                firstImg = (nextIdx > 0) ? imgs.substring(0, nextIdx) : imgs;
            }
            m.put("firstImageUrl", firstImg);
            return m;
        }).collect(Collectors.toList());

        model.addAttribute("ownername",    ownername != null ? ownername : "Owner");
        model.addAttribute("homestayid",   homestayid);
        model.addAttribute("homestayname", homestayname);
        model.addAttribute("rooms",        roomViews);
        return "Homestay/listRoom";
    }

    // ─── GET: ฟอร์มเพิ่มห้องพัก ──────────────────────────────────────────────
    @GetMapping("/addroom")
    public String showAddRoomForm(
            @RequestParam("homestayid") Integer homestayid,
            @RequestParam(value = "homestayname", defaultValue = "") String homestayname,
            @SessionAttribute(name = "ownername", required = false) String ownername,
            Model model) {

        model.addAttribute("homestayid",   homestayid);
        model.addAttribute("homestayname", homestayname);
        model.addAttribute("ownername",    ownername != null ? ownername : "Owner");
        model.addAttribute("allFacilities", roomTypeService.getAllFacilities());
        return "Homestay/addRoom";
    }

    // ─── POST: บันทึกห้องพักใหม่ (JSON + Base64) ─────────────────────────────
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

    // ─── GET: ดูรายละเอียดห้องพัก ────────────────────────────────────────────
    @GetMapping("/owner/room/view")
    public String viewRoom(
            @RequestParam("roomtypeid") String roomtypeid,
            HttpSession session,
            Model model) {

        if (session.getAttribute("ownerid") == null) return "redirect:/owner/login";

        Roomtype room = roomTypeService.getRoomTypeById(roomtypeid);
        if (room == null) return "redirect:/owner/rooms";

        List<String> imageList = buildImageList(room.getImages());

        List<String> facilities = (room.getFacilities() == null)
                ? Collections.emptyList()
                : room.getFacilities().stream()
                        .map(Facilities::getFacilitiesname)
                        .collect(Collectors.toList());

        model.addAttribute("ownername",  orDefault(session, "ownername"));
        model.addAttribute("room",       room);
        model.addAttribute("imageList",  imageList);
        model.addAttribute("facilities", facilities);
        return "Homestay/viewRoom";
    }

    // ─── GET: ฟอร์มแก้ไขห้องพัก ──────────────────────────────────────────────
    @GetMapping("/owner/room/edit")
    public String showEditRoomForm(
            @RequestParam("roomtypeid") String roomtypeid,
            HttpSession session,
            Model model) {

        if (session.getAttribute("ownerid") == null) return "redirect:/owner/login";

        Roomtype room = roomTypeService.getRoomTypeById(roomtypeid);
        if (room == null) return "redirect:/owner/rooms";

        // รายการ facilities ทั้งหมด + id ที่ห้องนี้มีอยู่แล้ว
        List<Facilities> allFacilities = roomTypeService.getAllFacilities();
        Set<String> checkedIds = (room.getFacilities() == null)
                ? Collections.emptySet()
                : room.getFacilities().stream()
                        .map(Facilities::getFacilitiesid)
                        .collect(Collectors.toSet());

        // แยกรูปทุกรูปเป็น List<String>
        List<String> imageList = buildImageList(room.getImages());

        model.addAttribute("ownername",     orDefault(session, "ownername"));
        model.addAttribute("room",          room);
        model.addAttribute("allFacilities", allFacilities);
        model.addAttribute("checkedIds",    checkedIds);
        model.addAttribute("imageList",     imageList);
        model.addAttribute("homestayid",    room.getHomestay() != null
                                                ? room.getHomestay().getHomestayid() : null);
        return "Homestay/editRoom";
    }

    // ─── POST: บันทึกการแก้ไขห้องพัก (JSON + Base64) ─────────────────────────
    @PostMapping("/owner/room/edit")
    @ResponseBody
    public ResponseEntity<?> updateRoom(@RequestBody UpdateRoomRequest req,
                                        HttpSession session) {
        if (session.getAttribute("ownerid") == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "กรุณาเข้าสู่ระบบ"));
        }
        try {
            roomTypeService.updateRoomType(req);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ─── helper: split images string → List<String> ──────────────────────────
    private List<String> buildImageList(String imgs) {
        List<String> list = new ArrayList<>();
        if (imgs != null && !imgs.isBlank()) {
            String[] parts = imgs.split("(?=,data:)");
            for (String p : parts) {
                String trimmed = p.startsWith(",") ? p.substring(1) : p;
                if (!trimmed.isBlank()) list.add(trimmed);
            }
        }
        return list;
    }

    private String orDefault(HttpSession session, String key) {
        Object val = session.getAttribute(key);
        return val != null ? val.toString() : "Owner";
    }
}