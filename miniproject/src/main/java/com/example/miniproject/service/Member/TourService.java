package com.example.miniproject.service.Member;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.entity.TourType;
import com.example.miniproject.repository.Member.TourRepository;
import com.example.miniproject.repository.Tour.TourTypeRepository;


@Service
public class TourService {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private TourTypeRepository tourTypeRepository;

    // ชื่อประเภทที่บังคับใช้กับทัวร์รายวัน (numberOfDays == 1)
    private static final String DAILY_TOUR_TYPE_NAME = "ทัวร์รายวัน";

    // ─────────────────────────────────────────────────────────
    // หา TourType จากชื่อ ถ้ายังไม่มีในตาราง tourtype ให้สร้างใหม่ให้เลย
    // (เดิม logic นี้อยู่ใน Tour.updateTourtype() แบบ String,
    //  ย้ายมาไว้ที่ service เพราะต้องคุยกับ TourTypeRepository)
    // ─────────────────────────────────────────────────────────
    private TourType resolveTourType(String typeNameFromForm, Integer numberOfDays) {
        String name = (numberOfDays != null && numberOfDays == 1)
                ? DAILY_TOUR_TYPE_NAME
                : (typeNameFromForm == null ? null : typeNameFromForm.trim());

        if (name == null || name.isBlank()) {
            if (numberOfDays != null && numberOfDays > 1) {
                // ป้องกันกรณีลืม set tourtype เองสำหรับทัวร์หลายวัน (เดิมโยนใน entity)
                throw new IllegalStateException(
                        "กรุณาระบุ tourtype สำหรับทัวร์ที่มากกว่า 1 วัน (เช่น ทัวร์วัฒนธรรมชนเผ่า, ทัวร์วิถีชีวิต)");
            }
            return null;
        }

        return tourTypeRepository.findByTypename(name)
                .orElseGet(() -> {
                    TourType t = new TourType();
                    t.setTypeId("TT" + UUID.randomUUID().toString()
                            .replace("-", "").substring(0, 8).toUpperCase());
                    t.setTypename(name);
                    return tourTypeRepository.save(t);
                });
    }

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

    public List<TourType> getAllTourTypes() {
    return tourTypeRepository.findAll();
}

    // ─────────────────────────────────────────────────────────
    // ดึงเฉพาะทัวร์ที่ "เปิดจอง" (สำหรับหน้าค้นหาของผู้ใช้)
    // ─────────────────────────────────────────────────────────
   public List<Tour> getAllActiveTours() {
    List<Tour> tours = tourRepository.findByStatus("เปิดจอง");

    // map tourid → bookedSeats
    Map<String, Integer> bookedMap = tourRepository.findBookedSeatsAll()
        .stream()
        .collect(Collectors.toMap(
            row -> (String) row[0],
            row -> ((Number) row[1]).intValue()
        ));

    // inject ค่าเข้า tour แต่ละตัว
    tours.forEach(t -> {
        int booked = bookedMap.getOrDefault(t.getTourid(), 0);
        t.setBookedSeats(booked); // ← เพิ่ม field นี้ใน Tour
    });

    return tours;
}


// เพิ่ม method นี้
    public void injectBookedSeats(List<Tour> tours) {
    Map<String, Integer> bookedMap = tourRepository.findBookedSeatsAll()
        .stream()
        .collect(Collectors.toMap(
            row -> (String) row[0],
            row -> ((Number) row[1]).intValue()
        ));
    tours.forEach(t ->
        t.setBookedSeats(bookedMap.getOrDefault(t.getTourid(), 0))
    );
}

    // ─────────────────────────────────────────────────────────
    // ดึงทัวร์ตาม ID — เฉพาะที่ยังเปิดอยู่ (สำหรับผู้ใช้ทั่วไป)
    // ─────────────────────────────────────────────────────────
    public Optional<Tour> getTourById(String tourid) {
        return tourRepository.findById(tourid)
                .filter(t -> "เปิดจอง".equalsIgnoreCase(t.getStatus()));
    }

    //ใช้เฉพาะหน้าจองทัวร์ (fetch bookings มาด้วย)
    public Optional<Tour> getTourByIdWithBookings(String tourid) {
        return tourRepository.findByIdWithBookings(tourid);
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
    // tourTypeName: ชื่อประเภททัวร์ที่รับมาจากฟอร์ม (String) — service จะแปลงเป็น TourType ให้เอง
    @Transactional
    public Tour createTour(Tour tour, Communitymanager manager, String tourTypeName) {
        String newId = "T" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 9).toUpperCase();
        tour.setTourid(newId);
        tour.setCommunitymanager(manager);
        tour.setTourtype(resolveTourType(tourTypeName, tour.getNumberOfDays()));
        return tourRepository.save(tour);
    }

    // ─────────────────────────────────────────────────────────
    // อัปเดตเฉพาะรูปภาพ (ใช้หลัง createTour ตอนบันทึกรูปจาก base64
    // ไม่ต้องยุ่งกับ tourtype ซ้ำอีกรอบ)
    // ─────────────────────────────────────────────────────────
    @Transactional
    public void updateImages(String tourid, String images) {
        tourRepository.findById(tourid).ifPresent(t -> {
            t.setImages(images);
            tourRepository.save(t);
        });
    }

    // ─────────────────────────────────────────────────────────
    // อัปเดตทัวร์
    // ─────────────────────────────────────────────────────────
    // tourTypeName: ชื่อประเภททัวร์ที่รับมาจากฟอร์ม (String) — service จะแปลงเป็น TourType ให้เอง
    @Transactional
public Tour updateTour(String tourid, Tour updated, String tourTypeName) {
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
    existing.setNumberOfDays(updated.getNumberOfDays());      
    existing.setNumberOfNights(updated.getNumberOfNights());  
    existing.setTourtype(resolveTourType(tourTypeName, updated.getNumberOfDays()));
    if (updated.getImages() != null && !updated.getImages().isBlank()) {
        existing.setImages(updated.getImages());
    }
    return tourRepository.save(existing);
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

    // ─────────────────────────────────────────────────────────
// คำนวณที่นั่งคงเหลือ — จุดเดียวที่ใช้ทั้งระบบ (single source of truth)
// ─────────────────────────────────────────────────────────
public int getAvailableSeats(Tour tour) {
    if (tour.getMaxSeatstour() == null) {
        return Integer.MAX_VALUE; // ไม่จำกัดที่นั่ง
    }
    Map<String, Integer> bookedMap = tourRepository.findBookedSeatsAll()
        .stream()
        .collect(Collectors.toMap(
            row -> (String) row[0],
            row -> ((Number) row[1]).intValue()
        ));
    int bookedSeats = bookedMap.getOrDefault(tour.getTourid(), 0);
    return Math.max(0, tour.getMaxSeatstour() - bookedSeats);
}

// ─────────────────────────────────────────────────────────
// ระดับสถานะที่นั่ง — ใช้ % แทนเลขตายตัว (รองรับทัวร์เล็ก/ใหญ่เท่ากัน)
// คืนค่า: "full" | "low" | "open"
// ─────────────────────────────────────────────────────────
public String getSeatStatusLevel(Tour tour, int availableSeats) {
    if (availableSeats <= 0) return "full";
    if (tour.getMaxSeatstour() == null || tour.getMaxSeatstour() <= 0) return "open";
    double ratio = (double) availableSeats / tour.getMaxSeatstour();
    return (ratio <= 0.2) ? "low" : "open"; // เหลือ ≤20% ถือว่าใกล้เต็ม
}
}