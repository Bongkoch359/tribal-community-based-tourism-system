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
    // ─────────────────────────────────────────────────────────
    private TourType resolveTourType(String typeNameFromForm, Integer numberOfDays) {
        String name = (numberOfDays != null && numberOfDays == 1)
                ? DAILY_TOUR_TYPE_NAME
                : (typeNameFromForm == null ? null : typeNameFromForm.trim());

        if (name == null || name.isBlank()) {
            if (numberOfDays != null && numberOfDays > 1) {
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
    // ✅ แก้ใหม่: ดึงทัวร์ที่ "จองได้จริง" (สำหรับหน้าค้นหาของผู้ใช้)
    //    เดิมกรองจาก t.status == 'เปิดจอง' → ตอนนี้กรองจาก repository query
    //    ที่เช็ค EXISTS Tourschedule status = 'เปิดรับจอง' แทน
    // ─────────────────────────────────────────────────────────
    public List<Tour> getAllActiveTours() {
        List<Tour> tours = tourRepository.search(null, null); // ใช้ query ที่กรองรอบเปิดรับจองอยู่แล้ว


        return tours;
    }

    // เพิ่ม method นี้
    public void injectBookedSeats(List<Tour> tours) {
        
    }

    // ─────────────────────────────────────────────────────────
    // ✅ แก้ใหม่: ดึงทัวร์ตาม ID — เฉพาะที่ "จองได้จริง" (สำหรับผู้ใช้ทั่วไป)
    //    เดิมกรอง t.getStatus() == "เปิดจอง" → ตัดออก เพราะไม่มี field นี้แล้ว
    //    ถ้าต้องการกันไม่ให้เข้าดูทัวร์ที่ไม่มีรอบเปิดเลย ให้ใช้ getTourByIdAny()
    //    แล้วเช็คที่ชั้น controller ว่ามีรอบเปิดอยู่จริงไหมแทน
    // ─────────────────────────────────────────────────────────
    public Optional<Tour> getTourById(String tourid) {
        return tourRepository.findById(tourid);
    }

    // ใช้เฉพาะหน้าจองทัวร์ (fetch bookings มาด้วย)
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
    // อัปเดตเฉพาะรูปภาพ
    // ─────────────────────────────────────────────────────────
    @Transactional
    public void updateImages(String tourid, String images) {
        tourRepository.findById(tourid).ifPresent(t -> {
            t.setImages(images);
            tourRepository.save(t);
        });
    }

    // ─────────────────────────────────────────────────────────
    // ✅ อัปเดตทัวร์ — ตัด existing.setStatus(...) ออก เพราะไม่มี field status แล้ว
    // ─────────────────────────────────────────────────────────
    @Transactional
    public Tour updateTour(String tourid, Tour updated, String tourTypeName) {
        Tour existing = tourRepository.findById(tourid)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบทัวร์ ID: " + tourid));

        existing.setTourmname(updated.getTourmname());
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
    // ✅ แก้ใหม่: นับจำนวนทัวร์ที่เปิดอยู่ (จองได้จริง) — ใช้ query ใหม่แทน countByStatus
    // ─────────────────────────────────────────────────────────
    public long countActiveTours() {
        return tourRepository.countActivePublished();
    }

    // ─────────────────────────────────────────────────────────
    // คำนวณที่นั่งคงเหลือ — จุดเดียวที่ใช้ทั้งระบบ (single source of truth)
    // ─────────────────────────────────────────────────────────
    public int getAvailableSeats(Tour tour) {
        if (tour.getMaxSeatstour() == null) {
            return Integer.MAX_VALUE;
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
    // ระดับสถานะที่นั่ง — ใช้ % แทนเลขตายตัว
    // ─────────────────────────────────────────────────────────
    public String getSeatStatusLevel(Tour tour, int availableSeats) {
        if (availableSeats <= 0) return "full";
        if (tour.getMaxSeatstour() == null || tour.getMaxSeatstour() <= 0) return "open";
        double ratio = (double) availableSeats / tour.getMaxSeatstour();
        return (ratio <= 0.2) ? "low" : "open";
    }
}