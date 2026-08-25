package com.example.miniproject.service.Tour;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.miniproject.entity.Tour;
import com.example.miniproject.entity.Tourschedule;
import com.example.miniproject.repository.Tour.TourScheduleRepository;

@Service
public class TourScheduleService {

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    // ─────────────────────────────────────────────────────────
    //
    // สถานะที่ manager ตั้งเองได้ด้วยมือ — "เต็ม" คำนวณอัตโนมัติจากการจอง
    // เท่านั้น ไม่ให้ตั้งเองตรงนี้
    // ─────────────────────────────────────────────────────────
    public static final Set<String> ALLOWED_MANUAL_STATUS = Set.of("เปิดรับจอง", "ปิด");

    // ─────────────────────────────────────────────────────────
    // คำนวณสถานะรวมของทัวร์จากสถานะของ
    // "รอบทัวร์" (Tourschedule) ทั้งหมด — ใช้ร่วมกันทั้ง TourController
    // และ TourScheduleController
    // - มีอย่างน้อย 1 รอบ "เปิดรับจอง" → เปิดรับจอง
    // - ไม่มีรอบเปิดรับจอง แต่มีรอบ "เต็ม" → เต็ม
    // - มีรอบแต่ทุกรอบถูกปิดหมด → ปิด
    // - ยังไม่มีรอบทัวร์เลย → ยังไม่เปิดรอบ
    // ─────────────────────────────────────────────────────────
    public String computeOverallStatus(List<Tourschedule> schedules, Tour tour, Map<String, Integer> bookedMap) {
        if (schedules == null || schedules.isEmpty()) {
            return "ปิด"; // หรือ "ยังไม่เปิดรอบ"
        }

        int maxSeats = (tour != null && tour.getMaxSeatstour() != null) ? tour.getMaxSeatstour() : 0;
        boolean hasOpen = false;
        boolean hasFull = false;

        for (Tourschedule s : schedules) {
            // ถ้ารอบถูกปิดด้วยตนเอง ข้ามไป
            if ("ปิด".equals(s.getStatus())) {
                continue;
            }

            // เช็คว่าที่นั่งถูกจองครบแล้วหรือไม่
            int booked = (bookedMap != null) ? bookedMap.getOrDefault(s.getScheduleid(), 0) : 0;
            boolean isSeatFull = (maxSeats > 0 && booked >= maxSeats);

            if ("เต็ม".equals(s.getStatus()) || isSeatFull) {
                hasFull = true;
            } else if ("เปิดรับจอง".equals(s.getStatus())) {
                hasOpen = true;
            }
        }

        // มีรอบที่ยังเปิดและยังมีที่นั่งเหลือ
        if (hasOpen) {
            return "เปิดรับจอง";
        }
        // ไม่มีรอบเปิดเหลือเลย แต่มีรอบที่เต็ม
        if (hasFull) {
            return "เต็ม";
        }

        return "ปิด";
    }

    // ─────────────────────────────────────────────────────────
    // ดึงรอบทั้งหมดของทัวร์ (ใช้ในหน้า manager ดู/จัดการวันที่เปิดทัวร์)
    // ─────────────────────────────────────────────────────────
    public List<Tourschedule> getSchedulesByTour(String tourid) {
        return tourScheduleRepository.findByTourTouridOrderByOpendateAsc(tourid);
    }

    // ─────────────────────────────────────────────────────────
    // ดึงเฉพาะรอบที่ยังเปิดรับจองและยังไม่ผ่านวันไป (ใช้หน้า member เลือกวันจอง)
    // ─────────────────────────────────────────────────────────
    public List<Tourschedule> getBookableSchedules(String tourid) {
        return tourScheduleRepository.findBookableSchedules(tourid, Date.valueOf(LocalDate.now()));
    }

    // ─────────────────────────────────────────────────────────
    // เพิ่มรอบ/วันที่เปิดทัวร์ใหม่ให้ทัวร์หนึ่งๆ
    // ─────────────────────────────────────────────────────────
   @Transactional
public Tourschedule createSchedule(Tour tour, Date opendate, Date enddate) {

    // ✅ กันรอบทัวร์ใหม่ทับซ้อนกับรอบเดิมของทัวร์เดียวกัน
    Date actualEnddate = (tour.getNumberOfDays() != null && tour.getNumberOfDays() == 1)
            ? opendate
            : enddate;
    validateNoOverlap(tour.getTourid(), opendate, actualEnddate, null);

    Tourschedule schedule = new Tourschedule();
    schedule.setScheduleid("SCH" + UUID.randomUUID().toString()
            .replace("-", "").substring(0, 9).toUpperCase());
    schedule.setTour(tour);
    schedule.setOpendate(opendate);
    schedule.setEnddate(actualEnddate);
    schedule.setStatus("เปิดรับจอง");
    return tourScheduleRepository.save(schedule);
}

// ─────────────────────────────────────────────────────────
// เช็คว่าช่วงวันที่ [start, end] ทับซ้อนกับรอบทัวร์อื่นของทัวร์เดียวกันหรือไม่
// ─────────────────────────────────────────────────────────
private void validateNoOverlap(String tourid, Date start, Date end, String excludeScheduleId) {
    LocalDate newStart = start.toLocalDate();
    LocalDate newEnd = end.toLocalDate();

    List<Tourschedule> existing = getSchedulesByTour(tourid);

    boolean overlaps = existing.stream().anyMatch(s -> {
        if (excludeScheduleId != null && excludeScheduleId.equals(s.getScheduleid())) {
            return false;
        }
        LocalDate existStart = s.getOpendate().toLocalDate();
        LocalDate existEnd = (s.getEnddate() != null) ? s.getEnddate().toLocalDate() : existStart;
        return !(newEnd.isBefore(existStart) || newStart.isAfter(existEnd));
    });

    if (overlaps) {
        throw new IllegalArgumentException(
            "ช่วงวันที่นี้ทับซ้อนกับรอบทัวร์อื่นที่มีอยู่แล้ว กรุณาเลือกช่วงวันที่อื่น");
    }
}
    // ─────────────────────────────────────────────────────────
    // แก้ไขสถานะรอบ (เช่น ปิดรับจองรอบนี้ด้วยมือ แม้ที่นั่งยังไม่เต็ม)
    // ─────────────────────────────────────────────────────────
    @Transactional
    public void updateStatus(String scheduleid, String status) {
        tourScheduleRepository.findById(scheduleid).ifPresent(s -> {
            s.setStatus(status);
            tourScheduleRepository.save(s);
        });
    }

    // ─────────────────────────────────────────────────────────
    // อัปเดตสถานะรอบทัวร์หลายรอบพร้อมกัน
    // ตามช่วงวันที่ (ใช้กับปฏิทินแบบลากเลือกช่วงในหน้า manager)
    // คืนค่าจำนวนรอบที่อัปเดตสำเร็จ
    // หมายเหตุ: การ "ปิด" รอบที่มีคนจองแล้วยังทำได้ (แค่ไม่รับจองเพิ่ม
    // ไม่ใช่การลบ) จึงไม่เช็คจำนวนที่จองแล้วในนี้ — ต่างจาก deleteSchedule
    // ที่ต้องเช็คที่ชั้น controller ก่อนเรียก
    // ─────────────────────────────────────────────────────────
    @Transactional
    public int bulkUpdateStatusByDateRange(String tourid, LocalDate startDate, LocalDate endDate, String status) {
        List<Tourschedule> schedules = getSchedulesByTour(tourid);

        int updated = 0;
        for (Tourschedule s : schedules) {
            LocalDate open = s.getOpendate().toLocalDate();
            if (!open.isBefore(startDate) && !open.isAfter(endDate)) {
                updateStatus(s.getScheduleid(), status);
                updated++;
            }
        }
        return updated;
    }

    // ─────────────────────────────────────────────────────────
    // ลบรอบ (เฉพาะกรณีที่ยังไม่มีใครจองรอบนั้น — เช็คที่ชั้น controller ก่อนเรียก)
    // ─────────────────────────────────────────────────────────
    @Transactional
    public void deleteSchedule(String scheduleid) {
        tourScheduleRepository.deleteById(scheduleid);
    }

    // ─────────────────────────────────────────────────────────
    // จำนวนที่นั่งที่จองไปแล้วของแต่ละ schedule ในทัวร์หนึ่งๆ
    // คืนค่า Map<scheduleid, bookedSeats>
    // ─────────────────────────────────────────────────────────
    public Map<String, Integer> getBookedSeatsMap(String tourid) {
        return tourScheduleRepository.findBookedSeatsByTour(tourid)
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).intValue()));
    }

    // ─────────────────────────────────────────────────────────
    // ที่นั่งคงเหลือของ schedule หนึ่งๆ (single source of truth ระดับรอบ)
    // ─────────────────────────────────────────────────────────
    public int getAvailableSeats(Tourschedule schedule) {
        Tour tour = schedule.getTour();
        if (tour == null || tour.getMaxSeatstour() == null) {
            return Integer.MAX_VALUE;
        }
        Map<String, Integer> bookedMap = getBookedSeatsMap(tour.getTourid());
        int booked = bookedMap.getOrDefault(schedule.getScheduleid(), 0);
        return Math.max(0, tour.getMaxSeatstour() - booked);
    }

}