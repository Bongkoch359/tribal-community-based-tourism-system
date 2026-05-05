package com.example.miniproject.service.Homestay;

import com.example.miniproject.dto.Homestay.AddRoomRequest;
import com.example.miniproject.dto.Homestay.UpdateRoomRequest;
import com.example.miniproject.entity.Facilities;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Roomtype;
import com.example.miniproject.repository.Homestay.FacilitiesRepository;
import com.example.miniproject.repository.Homestay.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RoomTypeService {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private FacilitiesRepository facilitiesRepository;

    //member เพิ่มมม
    public Roomtype getRoomById(String id){
    return roomTypeRepository.findById(id).orElse(null);
}

    // ─── Add ───────────────────────────────────────────────────────────────────
    public Roomtype addRoomType(AddRoomRequest req) {

        String id = "RT" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8).toUpperCase();

        Roomtype room = new Roomtype();
        room.setRoomtypeid(id);
        room.setTypename(req.getTypename());
        room.setBedtype(req.getBedtype());
        room.setPricepernight(req.getPricepernight());
        room.setMaxguest(req.getMaxguest());
        room.setTotalrooms(req.getTotalrooms());
        room.setDescription(req.getDescription());
        room.setRoomcondition(req.getRoomcondition());
        room.setStatus(req.getStatus());

        // ผูก Homestay
        Homestay homestay = new Homestay();
        homestay.setHomestayid(req.getHomestayid());
        room.setHomestay(homestay);

        // เก็บ Base64 คั่นด้วย ","
        if (req.getImages() != null && !req.getImages().isEmpty()) {
            room.setImages(String.join(",", req.getImages()));
        }

        // ผูก Facilities
        if (req.getFacilitiesIds() != null && !req.getFacilitiesIds().isEmpty()) {
            List<Facilities> facilitiesList = facilitiesRepository.findAllById(req.getFacilitiesIds());
            room.setFacilities(facilitiesList);
        }

        return roomTypeRepository.save(room);
    }

    // ─── Update ────────────────────────────────────────────────────────────────
    @Transactional
    public Roomtype updateRoomType(UpdateRoomRequest req) {

        Roomtype room = roomTypeRepository.findById(req.getRoomtypeid())
                .orElseThrow(() -> new RuntimeException("ไม่พบห้องพัก id: " + req.getRoomtypeid()));

        room.setTypename(req.getTypename());
        room.setBedtype(req.getBedtype());
        room.setPricepernight(req.getPricepernight());
        room.setMaxguest(req.getMaxguest());
        room.setTotalrooms(req.getTotalrooms());
        room.setDescription(req.getDescription());
        room.setRoomcondition(req.getRoomcondition());
        room.setStatus(req.getStatus());

        // ── รวมรูปเดิม + รูปใหม่ ──────────────────────────────────────────────
        List<String> allImages = new ArrayList<>();

        // รูปเดิมที่ยังเก็บอยู่ (ส่งมาจาก front-end ในรูป comma-separated base64)
        String existing = req.getExistingImages();
        if (existing != null && !existing.isBlank()) {
            // split ตาม ",data:" เหมือนเดิม
            String[] parts = existing.split("(?=,data:)");
            for (String p : parts) {
                String trimmed = p.startsWith(",") ? p.substring(1) : p;
                if (!trimmed.isBlank()) allImages.add(trimmed);
            }
        }

        // รูปใหม่ที่อัปโหลดเพิ่ม
        if (req.getImages() != null) {
            for (String img : req.getImages()) {
                if (img != null && !img.isBlank()) allImages.add(img);
            }
        }

        room.setImages(allImages.isEmpty() ? null : String.join(",", allImages));

        // ── อัปเดต Facilities ─────────────────────────────────────────────────
        if (req.getFacilitiesIds() != null) {
            List<Facilities> facilitiesList = req.getFacilitiesIds().isEmpty()
                    ? new ArrayList<>()
                    : facilitiesRepository.findAllById(req.getFacilitiesIds());
            room.setFacilities(facilitiesList);
        }

        return roomTypeRepository.save(room);
    }

    // ─── Read all by homestay ──────────────────────────────────────────────────
    public List<Roomtype> getRoomTypesByHomestayId(Integer homestayid) {
        return roomTypeRepository.findByHomestayId(homestayid);
    }

    // ─── Read one ─────────────────────────────────────────────────────────────
    @Transactional
    public Roomtype getRoomTypeById(String roomtypeid) {
        Roomtype room = roomTypeRepository.findById(roomtypeid).orElse(null);
        if (room != null && room.getFacilities() != null) {
            room.getFacilities().size(); // force lazy-load
        }
        return room;
    }

    // ─── Read all facilities ───────────────────────────────────────────────────
    public List<Facilities> getAllFacilities() {
        return facilitiesRepository.findAll();
    }

    // ─── Delete (soft: set status = "ปิดปรับปรุง") ───────────────────────────
    @Transactional
    public void softDeleteRoomType(String roomtypeid) {
        roomTypeRepository.findById(roomtypeid).ifPresent(room -> {
            room.setStatus("ปิดปรับปรุง");
            roomTypeRepository.save(room);
        });
    }
}