package com.example.miniproject.service.Tour;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    public Tourschedule createSchedule(Tour tour, Date opendate) {
        Tourschedule schedule = new Tourschedule();
        schedule.setScheduleid("SCH" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 9).toUpperCase());
        schedule.setTour(tour);
        schedule.setOpendate(opendate);
        schedule.setStatus("เปิดรับจอง");
        return tourScheduleRepository.save(schedule);
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