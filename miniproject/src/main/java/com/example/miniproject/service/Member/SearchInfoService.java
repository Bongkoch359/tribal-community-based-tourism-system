package com.example.miniproject.service.Member;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(SearchInfoService.class);

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
            log.error("getToursByManagerId failed, managerId={}", managerId, e);
            return new ArrayList<>();
        }
    }

    /**
     * step 5.1-5.2: searchActivity()
     * ค้นหากิจกรรมชุมชน (เนื่องจากเป็นแค่โพสต์ประชาสัมพันธ์ จึงกรองด้วย Keyword อย่างเดียว)
     */
    public List<Activitypost> searchActivity(String keyword) {
        try {
            if (keyword == null || keyword.isBlank()) {
                return activitypostRepository.findAllByOrderByCreateddateDesc();
            }
            return activitypostRepository
                .findByTitleContainingIgnoreCaseOrLocationContainingIgnoreCaseOrderByCreateddateDesc(
                    keyword, keyword
                );
        } catch (Exception e) {
            log.error("searchActivity failed, keyword={}", keyword, e);
            return new ArrayList<>();
        }
    }

    /**
     * ค้นหาทัวร์ชุมชน
     * @throws IllegalArgumentException ถ้ากรอกวันที่มาแค่ฝั่งเดียว, วันที่ย้อนหลัง,
     *         วันสิ้นสุดก่อนวันเริ่มต้น หรือจำนวนคนติดลบ
     */
    public List<Tour> searchTour(String keyword, Integer numGuest,
                                  String startDate, String endDate,
                                  String tourTypeId) {
        try {
            String kw = (keyword == null || keyword.isBlank()) ? null : keyword;
            String tt = (tourTypeId == null || tourTypeId.isBlank()) ? null : tourTypeId;

            validateNumGuest(numGuest);

            boolean hasStartRaw = startDate != null && !startDate.isBlank();
            boolean hasEndRaw   = endDate != null && !endDate.isBlank();

            java.sql.Date sd = parseDate(startDate);
            java.sql.Date ed = parseDate(endDate);

            // กรอกมาแค่ฝั่งเดียว หรือ parse ไม่ผ่านทั้งที่มีค่า -> ถือเป็น input ผิด ไม่ fallback เงียบๆ
            if (hasStartRaw != hasEndRaw) {
                throw new IllegalArgumentException("กรุณาระบุวันที่เริ่มต้นและสิ้นสุดให้ครบ");
            }

            if (sd != null && ed != null) {
                validateDateRange(sd, ed);
                return tourRepository.searchWithDate(kw, numGuest, sd, ed, tt);
            }
            return tourRepository.search(kw, numGuest, tt);

        } catch (IllegalArgumentException e) {
            throw e; // ให้ controller จับแล้วแสดง error message ให้ผู้ใช้เห็น
        } catch (Exception e) {
            log.error("searchTour failed, keyword={}, numGuest={}, startDate={}, endDate={}, tourTypeId={}",
                    keyword, numGuest, startDate, endDate, tourTypeId, e);
            return new ArrayList<>();
        }
    }

    /**
     * ค้นหาโฮมสเตย์
     * @throws IllegalArgumentException ถ้ากรอกวันที่มาแค่ฝั่งเดียว, วันที่ย้อนหลัง,
     *         วันเช็คเอาท์ก่อนวันเช็คอิน หรือจำนวนคนติดลบ
     */
    public List<Homestay> searchHomestay(String keyword, Integer numGuest,
                                          String startDate, String endDate) {
        try {
            String kw = (keyword == null || keyword.isBlank()) ? null : keyword;

            validateNumGuest(numGuest);

            boolean hasStartRaw = startDate != null && !startDate.isBlank();
            boolean hasEndRaw   = endDate != null && !endDate.isBlank();

            java.sql.Date sd = parseDate(startDate);
            java.sql.Date ed = parseDate(endDate);

            if (hasStartRaw != hasEndRaw) {
                throw new IllegalArgumentException("กรุณาระบุวันที่เช็คอินและเช็คเอาท์ให้ครบ");
            }

            if (sd != null && ed != null) {
                validateDateRange(sd, ed);
                return homestayRepository.searchWithDate(kw, numGuest, sd, ed);
            }
            if (kw == null) return homestayRepository.findAll();
            return homestayRepository.findByHomestaynameContainingIgnoreCaseOrAddressContainingIgnoreCase(kw, kw);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("searchHomestay failed, keyword={}, numGuest={}, startDate={}, endDate={}",
                    keyword, numGuest, startDate, endDate, e);
            return new ArrayList<>();
        }
    }

    /**
     * ตรวจสอบว่าวันที่เริ่มต้น/สิ้นสุดไม่ย้อนหลัง และวันสิ้นสุด >= วันเริ่มต้น
     * ใช้ร่วมกันทั้งฝั่งทัวร์ (เริ่มต้น/สิ้นสุด) และโฮมสเตย์ (เช็คอิน/เช็คเอาท์)
     */
    private void validateDateRange(java.sql.Date sd, java.sql.Date ed) {
        java.sql.Date today = java.sql.Date.valueOf(java.time.LocalDate.now());

        if (sd.before(today)) {
            throw new IllegalArgumentException("วันที่เริ่มต้นต้องไม่เป็นวันที่ผ่านมาแล้ว");
        }
        if (ed.before(today)) {
            throw new IllegalArgumentException("วันที่สิ้นสุดต้องไม่เป็นวันที่ผ่านมาแล้ว");
        }
        if (ed.before(sd)) {
            throw new IllegalArgumentException("วันที่สิ้นสุดต้องมากกว่าหรือเท่ากับวันที่เริ่มต้น");
        }
    }

    /**
     * กันจำนวนคนติดลบหรือ 0 ที่อาจหลุดเข้ามาทาง URL query string โดยตรง
     * (ฝั่ง client จำกัดค่าผ่านปุ่ม +/- อยู่แล้ว แต่ URL bypass ได้)
     */
    private void validateNumGuest(Integer numGuest) {
        if (numGuest != null && numGuest < 1) {
            throw new IllegalArgumentException("จำนวนคนต้องไม่น้อยกว่า 1");
        }
    }

    // helper แปลง String เป็น java.sql.Date
    private java.sql.Date parseDate(String dateStr) {
        try {
            if (dateStr == null || dateStr.isBlank()) return null;
            return java.sql.Date.valueOf(dateStr); // รับ format yyyy-MM-dd ตรง ๆ
        } catch (Exception e) {
            log.warn("parseDate failed, dateStr={}", dateStr, e);
            return null;
        }
    }
}