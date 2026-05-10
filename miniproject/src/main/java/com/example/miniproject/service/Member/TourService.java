package com.example.miniproject.service.Member;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.repository.Member.TourRepository;

@Service
public class TourService {

    @Autowired
    private TourRepository tourRepository;

    // ─────────────────────────────────────────────────────────
    // ดึงทัวร์ทั้งหมดของ manager คนนั้น (ใช้ใน listTour)
    // ─────────────────────────────────────────────────────────
    public List<Tour> getToursByManager(Communitymanager manager) {
        return tourRepository.findByCommunitymanager(manager);
    }

    // ─────────────────────────────────────────────────────────
    // ดึงทัวร์ทั้งหมด (ไม่กรอง manager — ใช้กรณีต้องการ admin view)
    // ─────────────────────────────────────────────────────────
    public List<Tour> getAllTours() {
        return tourRepository.findAll();
    }

    // ─────────────────────────────────────────────────────────
    // ดึงเฉพาะทัวร์ที่ "เปิดจอง" (สำหรับหน้าค้นหาของผู้ใช้)
    // ─────────────────────────────────────────────────────────
    public List<Tour> getAllActiveTours() {
        return tourRepository.findByStatus("เปิดจอง");
    }

    // ─────────────────────────────────────────────────────────
    // ดึงทัวร์ตาม ID — เฉพาะที่ยังเปิดอยู่ (สำหรับผู้ใช้ทั่วไป)
    // ─────────────────────────────────────────────────────────
    public Optional<Tour> getTourById(String tourid) {
        return tourRepository.findById(tourid)
                .filter(t -> "เปิดจอง".equalsIgnoreCase(t.getStatus()));
    }

    // ─────────────────────────────────────────────────────────
    // ดึงทัวร์ตาม ID — ไม่กรองสถานะ (สำหรับ manager ดูรายละเอียด)
    // ─────────────────────────────────────────────────────────
    public Optional<Tour> getTourByIdAny(String tourid) {
        return tourRepository.findById(tourid);
    }

    // ─────────────────────────────────────────────────────────
    // สร้างทัวร์ใหม่
    // ─────────────────────────────────────────────────────────
    @Transactional
    public Tour createTour(Tour tour, Communitymanager manager) {
        String newId = "T" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 9).toUpperCase();
        tour.setTourid(newId);
        tour.setCommunitymanager(manager);
        return tourRepository.save(tour);
    }

    // ─────────────────────────────────────────────────────────
    // อัปเดตทัวร์
    // ─────────────────────────────────────────────────────────
    @Transactional
    public Tour updateTour(String tourid, Tour updated) {
        Tour existing = tourRepository.findById(tourid)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบทัวร์ ID: " + tourid));

        existing.setTourmname(updated.getTourmname());
        existing.setStatus(updated.getStatus());
        existing.setTourdetail(updated.getTourdetail());
        existing.setConditiontour(updated.getConditiontour());
        existing.setMinSeatstour(updated.getMinSeatstour());
        existing.setMaxSeatstour(updated.getMaxSeatstour());
        existing.setAdultprice(updated.getAdultprice());
        existing.setChildprice(updated.getChildprice());
        if (updated.getImages() != null && !updated.getImages().isBlank()) {
            existing.setImages(updated.getImages());
        }
        return tourRepository.save(existing);
    }

    // ─────────────────────────────────────────────────────────
    // ปิดรับการจอง (ไม่ลบ เพื่อรักษาประวัติ)
    // ─────────────────────────────────────────────────────────
    @Transactional
    public Tour closeBooking(String tourid) {
        Tour tour = tourRepository.findById(tourid)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบทัวร์ ID: " + tourid));
        tour.setStatus("ปิดรับการจอง");
        return tourRepository.save(tour);
    }

    // ─────────────────────────────────────────────────────────
    // เปลี่ยนสถานะ
    // ─────────────────────────────────────────────────────────
    @Transactional
    public Tour changeStatus(String tourid, String newStatus) {
        Tour tour = tourRepository.findById(tourid)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบทัวร์ ID: " + tourid));
        tour.setStatus(newStatus);
        return tourRepository.save(tour);
    }

    // ─────────────────────────────────────────────────────────
    // ค้นหาทัวร์ (keyword + จำนวนที่นั่ง)
    // ─────────────────────────────────────────────────────────
    public List<Tour> searchTours(String keyword, Integer numGuest) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return tourRepository.search(kw, numGuest);
    }

    // ─────────────────────────────────────────────────────────
    // นับจำนวนทัวร์ที่เปิดอยู่
    // ─────────────────────────────────────────────────────────
    public long countActiveTours() {
        return tourRepository.findByStatus("เปิดจอง").size();
    }
}