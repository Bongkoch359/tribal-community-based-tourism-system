package com.example.miniproject.controller.HomestayOwner;

import com.example.miniproject.dto.Homestay.AddRoomRequest;
import com.example.miniproject.dto.Homestay.UpdateRoomRequest;
import com.example.miniproject.entity.Facilities;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Homestayowner;
import com.example.miniproject.entity.Roomtype;
import com.example.miniproject.service.Homestay.HomestayService;
import com.example.miniproject.service.Homestay.HomestayOwnerService;
import com.example.miniproject.service.Homestay.RoomTypeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class RoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;

    @Autowired
    private HomestayService homestayService;

    @Autowired
    private HomestayOwnerService homestayOwnerService;

    /** โฟลเดอร์เก็บรูปห้องพัก (สร้างอัตโนมัติถ้ายังไม่มี) */
    private static final String ROOM_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/rooms/";

    // ─── GET: รายการห้องพักทั้งหมด ───────────────────────────────────────────
    @GetMapping("/owner/rooms")
    public String listRooms(
            @RequestParam(value = "homestayid", required = false) Integer homestayid,
            HttpSession session,
            Model model) {

        if (session.getAttribute("ownerid") == null)
            return "redirect:/owner/login";

        String ownername = (String) session.getAttribute("ownername");
        Integer ownerid = (Integer) session.getAttribute("ownerid");

        boolean bankInfoMissing = checkBankInfoMissing(ownerid);
        model.addAttribute("bankInfoMissing", bankInfoMissing);

        // ดึงโฮมสเตย์ทั้งหมดของเจ้าของคนนี้
        List<Homestay> myHomestays = homestayService.getHomestaysByOwnerId(ownerid);

        // ถ้าไม่ได้ส่ง homestayid มา → ใช้อันแรกของรายการ
        if (homestayid == null) {
            if (!myHomestays.isEmpty()) {
                homestayid = myHomestays.get(0).getHomestayid();
            }
        }

        String homestayname = "";
        if (homestayid != null) {
            Homestay hs = homestayService.getHomestayById(homestayid);
            if (hs != null)
                homestayname = hs.getHomestayname();
        }

        List<Roomtype> rooms = (homestayid != null)
                ? roomTypeService.getRoomTypesByHomestayId(homestayid)
                : Collections.emptyList();

        List<Map<String, Object>> roomViews = rooms.stream().map(room -> {
            Map<String, Object> m = new HashMap<>();
            m.put("roomtypeid", room.getRoomtypeid());
            m.put("typename", room.getTypename());
            m.put("pricepernight", room.getPricepernight());
            m.put("maxguest", room.getMaxguest());
            m.put("totalrooms", room.getTotalrooms());
            m.put("status", room.getStatus());

            String firstImg = null;
            String imgs = room.getImages();
            if (imgs != null && !imgs.isBlank()) {
                String[] parts = imgs.split(",");
                if (parts.length > 0 && !parts[0].isBlank()) {
                    firstImg = parts[0].trim();
                }
            }
            m.put("firstImageUrl", firstImg);
            return m;
        }).collect(Collectors.toList());

        model.addAttribute("ownername", ownername != null ? ownername : "Owner");
        model.addAttribute("homestayid", homestayid);
        model.addAttribute("homestayname", homestayname);
        model.addAttribute("myHomestays", myHomestays);
        model.addAttribute("rooms", roomViews);
        return "Homestay/listRoom";
    }

    // ─── GET: ฟอร์มเพิ่มห้องพัก ──────────────────────────────────────────────
    @GetMapping("/addroom")
    public String showAddRoomForm(
            @RequestParam("homestayid") Integer homestayid,
            @RequestParam(value = "homestayname", defaultValue = "") String homestayname,
            HttpSession session,
            Model model) {

        if (session.getAttribute("ownerid") == null)
            return "redirect:/owner/login";

        Integer ownerid = (Integer) session.getAttribute("ownerid");

        // ── กันฝั่ง server: ธนาคารยังไม่ครบ ห้ามเข้าหน้าเพิ่มห้องพัก ──
        if (checkBankInfoMissing(ownerid)) {
            return "redirect:/owner/rooms?error=bankInfoRequired";
        }

        String ownername = (String) session.getAttribute("ownername");

        model.addAttribute("homestayid", homestayid);
        model.addAttribute("homestayname", homestayname);
        model.addAttribute("ownername", ownername != null ? ownername : "Owner");
        model.addAttribute("allFacilities", roomTypeService.getAllFacilities());
        return "Homestay/addRoom";
    }

    // ─── POST: บันทึกห้องพักใหม่ (multipart/form-data) ───────────────────────
    @PostMapping(value = "/addroom", consumes = "multipart/form-data")
    @ResponseBody
    public ResponseEntity<?> addRoom(
            @RequestParam("homestayid") Integer homestayid,
            @RequestParam("typename") String typename,
            @RequestParam("bedtype") String bedtype,
            @RequestParam("pricepernight") double pricepernight,
            @RequestParam("maxguest") int maxguest,
            @RequestParam("totalrooms") int totalrooms,
            @RequestParam(value = "description", required = false, defaultValue = "") String description,
            @RequestParam(value = "roomcondition", required = false, defaultValue = "") String roomcondition,
            @RequestParam("status") String status,
            @RequestParam(value = "facilitiesIds", required = false) List<String> facilitiesIds,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpSession session) {

        if (session.getAttribute("ownerid") == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "กรุณาเข้าสู่ระบบ"));
        }

        Integer ownerid = (Integer) session.getAttribute("ownerid");

        // ── กันฝั่ง server: ธนาคารยังไม่ครบ ห้ามบันทึกห้องพัก ──
        if (checkBankInfoMissing(ownerid)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "กรุณากรอกข้อมูลบัญชีธนาคารให้ครบก่อนเพิ่มห้องพัก"));
        }

        try {
            // บันทึกรูปและเก็บ URL path
            String imageUrls = saveImages(images);

            AddRoomRequest req = new AddRoomRequest();
            req.setHomestayid(homestayid);
            req.setTypename(typename);
            req.setBedtype(bedtype);
            req.setPricepernight(pricepernight);
            req.setMaxguest(maxguest);
            req.setTotalrooms(totalrooms);
            req.setDescription(description);
            req.setRoomcondition(roomcondition);
            req.setStatus(status);
            req.setFacilitiesIds(facilitiesIds != null ? facilitiesIds : Collections.emptyList());
            req.setImages(imageUrls); // เก็บ path แทน Base64

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

        if (session.getAttribute("ownerid") == null)
            return "redirect:/owner/login";

        Roomtype room = roomTypeService.getRoomTypeById(roomtypeid);
        if (room == null)
            return "redirect:/owner/rooms";

        List<String> imageList = buildImageList(room.getImages());

        List<String> facilities = (room.getFacilities() == null)
                ? Collections.emptyList()
                : room.getFacilities().stream()
                        .map(Facilities::getFacilitiesname)
                        .collect(Collectors.toList());

        model.addAttribute("ownername", orDefault(session, "ownername"));
        model.addAttribute("room", room);
        model.addAttribute("imageList", imageList);
        model.addAttribute("facilities", facilities);
        return "Homestay/viewRoom";
    }

    // ─── GET: ฟอร์มแก้ไขห้องพัก ──────────────────────────────────────────────
    @GetMapping("/owner/room/edit")
    public String showEditRoomForm(
            @RequestParam("roomtypeid") String roomtypeid,
            HttpSession session,
            Model model) {

        if (session.getAttribute("ownerid") == null)
            return "redirect:/owner/login";

        Roomtype room = roomTypeService.getRoomTypeById(roomtypeid);
        if (room == null)
            return "redirect:/owner/rooms";

        List<Facilities> allFacilities = roomTypeService.getAllFacilities();
        Set<String> checkedIds = (room.getFacilities() == null)
                ? Collections.emptySet()
                : room.getFacilities().stream()
                        .map(Facilities::getFacilitiesid)
                        .collect(Collectors.toSet());

        List<String> imageList = buildImageList(room.getImages());

        model.addAttribute("ownername", orDefault(session, "ownername"));
        model.addAttribute("room", room);
        model.addAttribute("allFacilities", allFacilities);
        model.addAttribute("checkedIds", checkedIds);
        model.addAttribute("imageList", imageList);
        model.addAttribute("homestayid", room.getHomestay() != null
                ? room.getHomestay().getHomestayid()
                : null);
        return "Homestay/editRoom";
    }

    // ─── POST: บันทึกการแก้ไขห้องพัก (multipart/form-data) ──────────────────
    @PostMapping(value = "/owner/room/edit", consumes = "multipart/form-data")
    @ResponseBody
    public ResponseEntity<?> updateRoom(
            @RequestParam("roomtypeid") String roomtypeid,
            @RequestParam("typename") String typename,
            @RequestParam("bedtype") String bedtype,
            @RequestParam("pricepernight") double pricepernight,
            @RequestParam("maxguest") int maxguest,
            @RequestParam("totalrooms") int totalrooms,
            @RequestParam(value = "description", required = false, defaultValue = "") String description,
            @RequestParam(value = "roomcondition", required = false, defaultValue = "") String roomcondition,
            @RequestParam("status") String status,
            @RequestParam(value = "facilitiesIds", required = false) List<String> facilitiesIds,
            // รูปเดิมที่ยังคงไว้ (URL path คั่นด้วย comma)
            @RequestParam(value = "existingImages", required = false, defaultValue = "") String existingImages,
            // รูปใหม่ที่อัปโหลดเพิ่ม
            @RequestParam(value = "newImages", required = false) List<MultipartFile> newImages,
            HttpSession session) {

        if (session.getAttribute("ownerid") == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "กรุณาเข้าสู่ระบบ"));
        }
        try {
            // รวม path รูปเดิม + รูปใหม่ที่อัปโหลด
            String newImageUrls = saveImages(newImages);
            String allImages = mergeImagePaths(existingImages, newImageUrls);

            UpdateRoomRequest req = new UpdateRoomRequest();
            req.setRoomtypeid(roomtypeid);
            req.setTypename(typename);
            req.setBedtype(bedtype);
            req.setPricepernight(pricepernight);
            req.setMaxguest(maxguest);
            req.setTotalrooms(totalrooms);
            req.setDescription(description);
            req.setRoomcondition(roomcondition);
            req.setStatus(status);
            req.setFacilitiesIds(facilitiesIds != null ? facilitiesIds : Collections.emptyList());
            req.setImages(allImages);

            roomTypeService.updateRoomType(req);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ─── helper: บันทึกไฟล์รูป → คืน URL paths คั่นด้วย comma ──────────────
    private String saveImages(List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty())
            return "";

        Path uploadDir = Paths.get(ROOM_UPLOAD_DIR);
        if (!Files.exists(uploadDir))
            Files.createDirectories(uploadDir);

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty())
                continue;

            String original = Objects.requireNonNullElse(file.getOriginalFilename(), "img");
            String ext = original.contains(".")
                    ? original.substring(original.lastIndexOf('.'))
                    : ".jpg";
            String filename = UUID.randomUUID() + ext;

            Path dest = uploadDir.resolve(filename);
            file.transferTo(dest.toFile());

            urls.add("/uploads/rooms/" + filename);
        }
        return String.join(",", urls);
    }

    // ─── helper: รวม path รูปเดิม + รูปใหม่ ─────────────────────────────────
    private String mergeImagePaths(String existing, String newPaths) {
        List<String> result = new ArrayList<>();
        if (existing != null && !existing.isBlank()) {
            for (String p : existing.split(",")) {
                String t = p.trim();
                if (!t.isEmpty())
                    result.add(t);
            }
        }
        if (newPaths != null && !newPaths.isBlank()) {
            for (String p : newPaths.split(",")) {
                String t = p.trim();
                if (!t.isEmpty())
                    result.add(t);
            }
        }
        return String.join(",", result);
    }

    // ─── helper: split images string → List<String> ──────────────────────────
    private List<String> buildImageList(String imgs) {
        List<String> list = new ArrayList<>();
        if (imgs != null && !imgs.isBlank()) {
            for (String p : imgs.split(",")) {
                String t = p.trim();
                if (!t.isEmpty())
                    list.add(t);
            }
        }
        return list;
    }

    private String orDefault(HttpSession session, String key) {
        Object val = session.getAttribute(key);
        return val != null ? val.toString() : "Owner";
    }

    // ─── helper: เช็คว่าข้อมูลธนาคารของเจ้าของยังไม่ครบหรือไม่ ──────────────
    private boolean checkBankInfoMissing(Integer ownerid) {
        if (ownerid == null)
            return true;

        Homestayowner owner;
        try {
            owner = homestayOwnerService.getProfile(ownerid);
        } catch (IllegalArgumentException e) {
            return true; // ไม่พบ owner
        }

        return owner.getBankName() == null || owner.getBankName().isBlank()
                || owner.getAccountNumber() == null || owner.getAccountNumber().isBlank()
                || owner.getAccountName() == null || owner.getAccountName().isBlank();
    }
}