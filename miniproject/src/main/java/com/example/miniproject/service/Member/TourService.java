package com.example.miniproject.service.Member;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.miniproject.entity.Tour;
import com.example.miniproject.repository.Member.TourRepository;

@Service
public class TourService {

    @Autowired
    private TourRepository tourRepository;

    // ─── ดึงทัวร์ทั้งหมด (active) ─────────────────────────

    public List<Tour> getAllActiveTours() {
        return tourRepository.findByStatus("active");
    }

    // ─── ดึงทัวร์เดียวตาม ID ──────────────────────────────

    /**
     * ใช้ใน TourController GET /tour/{id}
     * @return Optional.empty() ถ้าไม่เจอหรือ inactive
     */
    public Optional<Tour> getTourById(String tourid) {
        return tourRepository.findById(tourid)
                .filter(t -> "active".equalsIgnoreCase(t.getStatus()));
    }

    // ─── ค้นหา ────────────────────────────────────────────

    /**
     * ค้นหาทัวร์ตาม keyword และ/หรือ จำนวนที่นั่ง
     * ถ้า keyword ว่างเปล่า → ดึงทั้งหมด
     */
    public List<Tour> searchTours(String keyword, Integer numGuest) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return tourRepository.search(kw, numGuest);
    }

    // ─── จำนวนรายการ (ใช้ใน search page) ─────────────────

    public long countActiveTours() {
        return tourRepository.findByStatus("active").size();
    }
}