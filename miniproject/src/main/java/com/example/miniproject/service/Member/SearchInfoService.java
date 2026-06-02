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

    /**
     * step 6.1-6.2: searchTour()
     * ค้นหาทัวร์ชุมชนขั้นสูง (กรองทั้งชื่อทัวร์, จำนวนคนที่รับได้ และช่วงเวลาเดินทาง)
     */
    public List<Tour> searchTour(String keyword, Integer numGuest, String startDate, String endDate) {
        try {
            // แปลงค่าว่างให้เป็น null เพื่อให้เอาไปคิวรีได้ถูกต้อง
            String kw = (keyword == null || keyword.isBlank()) ? null : keyword;

            // ✨ แก้ตรงนี้: เปลี่ยนจาก searchAdvanced มาเรียก search ธรรมดาที่รับแค่ 2 ตัวแปร
            return tourRepository.search(kw, numGuest);
            
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * step 7.1-7.2: searchHomestay()
     * ค้นหาโฮมสเตย์/ที่พักชุมชน
     */
    public List<Homestay> searchHomestay(String keyword, Integer numGuest, String startDate, String endDate) {
        try {
            // ถ้าไม่ระบุคีย์เวิร์ด คืนค่าโฮมสเตย์ทั้งหมด
            if (keyword == null || keyword.isBlank()) {
                return homestayRepository.findAll();
            }
            // ค้นหาโฮมสเตย์จากชื่อที่ใกล้เคียง
            return homestayRepository.findByHomestaynameContainingIgnoreCase(keyword);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}