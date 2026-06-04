package com.example.miniproject.service.Member;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.miniproject.entity.Activitypost;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.repository.Member.ActivitypostRepository;
import com.example.miniproject.repository.Homestay.HomestayRepository;
import com.example.miniproject.repository.Member.TourRepository;

@Service
public class SearchInfoService {

    @Autowired
    private ActivitypostRepository activitypostRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private HomestayRepository homestayRepository;

    /**
     * ดึงข้อมูลทัวร์ตาม ID ของผู้จัดการชุมชน
     */
    public List<Tour> getToursByManagerId(String managerId) {
        try {
            return tourRepository.findByManagerId(managerId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * step 5.1-5.2: searchActivity()
     * ค้นหากิจกรรมชุมชน (เนื่องจากเป็นแค่โพสต์ประชาสัมพันธ์ จึงกรองด้วย Keyword อย่างเดียว)
     */
    public List<Activitypost> searchActivity(String keyword) {
        try {
            // ถ้าไม่ระบุคำค้นหา ให้ดึงโพสต์กิจกรรมทั้งหมดขึ้นมาแสดง โดยเรียงจากใหม่ไปเก่า
            if (keyword == null || keyword.isBlank()) {
                return activitypostRepository.findAllByOrderByCreateddateDesc();
            }
            // ค้นหาโพสต์กิจกรรมที่มีชื่อตรงกับคีย์เวิร์ดที่ป้อน
            return activitypostRepository.findByTitleContainingIgnoreCase(keyword);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

   public List<Tour> searchTour(String keyword, Integer numGuest,
                              String startDate, String endDate) {
    try {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;

        // แปลง String → java.sql.Date (null-safe)
        java.sql.Date sd = parseDate(startDate);
        java.sql.Date ed = parseDate(endDate);

        // มีวันที่ → ใช้ query กรองช่วงเวลา
        if (sd != null && ed != null) {
            return tourRepository.searchWithDate(kw, numGuest, sd, ed);
        }
        // ไม่มีวันที่ → ใช้ query เดิม
        return tourRepository.search(kw, numGuest);

    } catch (Exception e) {
        return new ArrayList<>();
    }
}

public List<Homestay> searchHomestay(String keyword, Integer numGuest,
                                      String startDate, String endDate) {
    try {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword;

        java.sql.Date sd = parseDate(startDate);
        java.sql.Date ed = parseDate(endDate);

        if (sd != null && ed != null) {
            return homestayRepository.searchWithDate(kw, sd, ed);
        }
        // ไม่มีวันที่ → คืนทั้งหมด/กรองแค่ keyword
        if (kw == null) return homestayRepository.findAll();
        return homestayRepository.findByHomestaynameContainingIgnoreCase(kw);

    } catch (Exception e) {
        return new ArrayList<>();
    }
}

// helper แปลง String เป็น java.sql.Date
private java.sql.Date parseDate(String dateStr) {
    try {
        if (dateStr == null || dateStr.isBlank()) return null;
        return java.sql.Date.valueOf(dateStr); // รับ format yyyy-MM-dd ตรง ๆ
    } catch (Exception e) {
        return null;
    }
}
}