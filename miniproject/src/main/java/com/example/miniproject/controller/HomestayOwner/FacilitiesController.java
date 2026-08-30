package com.example.miniproject.controller.HomestayOwner;

import com.example.miniproject.entity.Facilities;
import com.example.miniproject.repository.Homestay.FacilitiesRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
public class FacilitiesController {

    @Autowired
    private FacilitiesRepository facilitiesRepository;

    @PostMapping("/api/facilities")
    public ResponseEntity<?> createFacility(
            @RequestBody Map<String, String> body,
            HttpSession session) {

        // กันคนไม่ได้ login ยิง API ตรงๆ
        if (session.getAttribute("ownerid") == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "กรุณาเข้าสู่ระบบ"));
        }

        String name = body.get("facilitiesname");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "กรุณาระบุชื่อสิ่งอำนวยความสะดวก"));
        }
        name = name.trim();

        // 1) ถ้ามีชื่อนี้อยู่แล้ว → คืนของเดิม ไม่สร้างซ้ำ
        Optional<Facilities> existing = facilitiesRepository.findByFacilitiesnameIgnoreCase(name);
        if (existing.isPresent()) {
            Facilities f = existing.get();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "facilitiesid", f.getFacilitiesid(),
                    "facilitiesname", f.getFacilitiesname()
            ));
        }

        // 2) generate id ใหม่ต่อจากตัวล่าสุด 
        Integer maxNum = facilitiesRepository.findMaxFacilitiesNumericId();
        int nextNum = (maxNum != null ? maxNum : 0) + 1;
        String newId = String.format("f%03d", nextNum);

        Facilities newFac = new Facilities();
        newFac.setFacilitiesid(newId);
        newFac.setFacilitiesname(name);

        Facilities saved = facilitiesRepository.save(newFac);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "facilitiesid", saved.getFacilitiesid(),
                "facilitiesname", saved.getFacilitiesname()
        ));
    }
}