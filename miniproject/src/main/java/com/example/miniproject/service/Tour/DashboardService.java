package com.example.miniproject.service.Tour;

import com.example.miniproject.dto.Tour.DashboardStatsDTO;
import com.example.miniproject.dto.Tour.BookingRowDTO;
import com.example.miniproject.dto.Tour.PostRowDTO;
import com.example.miniproject.dto.Tour.MonthlyRevenueDTO;
import com.example.miniproject.repository.Member.BookingRepository;
import com.example.miniproject.repository.Member.TourRepository;
import com.example.miniproject.repository.Member.ActivitypostRepository;
import com.example.miniproject.repository.Member.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private ActivitypostRepository activitypostRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private static final String[] THAI_MONTHS = {
            "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
            "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."
    };

    // รวมสถิติภาพรวม
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO dto = new DashboardStatsDTO();
        dto.setTotalTours(tourRepository.count());
        dto.setActiveTours(tourRepository.countActivePublished());
        dto.setTotalRevenue(paymentRepository.sumPaidAmount());
        dto.setPendingBookings(bookingRepository.countPendingBookings());
        return dto;
    }

    // จำนวนโพสต์กิจกรรมทั้งหมด (ไม่ใช่แค่ล่าสุด)
    public long getTotalPostCount() {
        return activitypostRepository.count();
    }

    // 5 การจองล่าสุด (เก็บ method ไว้เผื่อใช้หน้าอื่น แม้ dashboard จะไม่แสดงแล้ว)
    public List<BookingRowDTO> getRecentBookings(int limit) {
        return bookingRepository.findTop5ByOrderByBookingdateDesc()
                .stream()
                .limit(limit)
                .map(b -> {
                    BookingRowDTO row = new BookingRowDTO();
                    row.setBookingid(b.getBookingid());
                    row.setMemberName(b.getMember() != null
                            ? b.getMember().getFirstname() + " " + b.getMember().getLastname()
                            : "-");
                    row.setBookingType(b.getBookingType() != null ? b.getBookingType().name() : "-");
                    row.setBookingdate(b.getBookingdate() != null ? b.getBookingdate().toString() : "-");
                    row.setTotalamount(b.getTotalamount() != null ? b.getTotalamount() : 0.0);
                    row.setStatus(b.getBookingStatus() != null ? b.getBookingStatus().name() : "-");
                    return row;
                })
                .collect(Collectors.toList());
    }

    // โพสต์กิจกรรมล่าสุด (เก็บ method ไว้เผื่อใช้หน้าอื่น แม้ dashboard จะไม่แสดงแล้ว)
    public List<PostRowDTO> getPublishedPosts() {
        return activitypostRepository
                .findAllByOrderByCreateddateDesc()
                .stream()
                .map(p -> {
                    PostRowDTO row = new PostRowDTO();
                    row.setActivityid(p.getActivityid());
                    row.setTitle(p.getTitle());
                    row.setLocation(p.getLocation());
                    return row;
                })
                .collect(Collectors.toList());
    }

    // รายได้รายเดือนของปีปัจจุบัน (12 เดือนครบ) — เก็บไว้เผื่อใช้ที่อื่น
    public List<MonthlyRevenueDTO> getMonthlyRevenue() {
        int year = LocalDate.now().getYear();
        List<Object[]> rows = paymentRepository.findMonthlyRevenue(year);
        Map<Integer, Double> revenueMap = rows.stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r[0]).intValue(),
                        r -> ((Number) r[1]).doubleValue()));
        List<MonthlyRevenueDTO> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            result.add(new MonthlyRevenueDTO(m, revenueMap.getOrDefault(m, 0.0)));
        }
        return result;
    }

    // ─── ใหม่: แนวโน้มรายได้ทัวร์รายเดือน แบบเลือกช่วงได้ ───
    public List<Map<String, Object>> getTourRevenueTrend(YearMonth startYM, YearMonth endYM) {
        java.sql.Date startDate = java.sql.Date.valueOf(startYM.atDay(1));
        java.sql.Date endDate = java.sql.Date.valueOf(endYM.atEndOfMonth());

        Map<YearMonth, Double> byMonth = new LinkedHashMap<>();
        YearMonth cursor = startYM;
        while (!cursor.isAfter(endYM)) {
            byMonth.put(cursor, 0.0);
            cursor = cursor.plusMonths(1);
        }

        List<Object[]> rows = bookingRepository.sumTourRevenueByMonthRange(startDate, endDate);
        for (Object[] row : rows) {
            int yr = ((Number) row[0]).intValue();
            int mo = ((Number) row[1]).intValue();
            double amount = ((Number) row[2]).doubleValue();
            byMonth.put(YearMonth.of(yr, mo), amount);
        }

        double maxVal = byMonth.values().stream().max(Double::compareTo).orElse(0.0);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<YearMonth, Double> e : byMonth.entrySet()) {
            YearMonth ym = e.getKey();
            Map<String, Object> point = new LinkedHashMap<>();
            int beYearShort = (ym.getYear() + 543) % 100;
            point.put("label", THAI_MONTHS[ym.getMonthValue() - 1] + " " + beYearShort);
            point.put("amount", e.getValue());
            int heightPct = maxVal > 0 ? (int) Math.round((e.getValue() / maxVal) * 100) : 0;
            point.put("heightPct", Math.max(heightPct, e.getValue() > 0 ? 6 : 2));
            result.add(point);
        }
        return result;
    }

    // ─── ใหม่: แนวโน้มยอดจองทัวร์รายเดือน แบบเลือกช่วงได้ ───
    public List<Map<String, Object>> getTourBookingCountTrend(YearMonth startYM, YearMonth endYM) {
        java.sql.Date startDate = java.sql.Date.valueOf(startYM.atDay(1));
        java.sql.Date endDate = java.sql.Date.valueOf(endYM.atEndOfMonth());

        Map<YearMonth, Long> byMonth = new LinkedHashMap<>();
        YearMonth cursor = startYM;
        while (!cursor.isAfter(endYM)) {
            byMonth.put(cursor, 0L);
            cursor = cursor.plusMonths(1);
        }

        List<Object[]> rows = bookingRepository.countTourBookingsByMonthRange(startDate, endDate);
        for (Object[] row : rows) {
            int yr = ((Number) row[0]).intValue();
            int mo = ((Number) row[1]).intValue();
            long cnt = ((Number) row[2]).longValue();
            byMonth.put(YearMonth.of(yr, mo), cnt);
        }

        long maxVal = byMonth.values().stream().max(Long::compareTo).orElse(0L);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<YearMonth, Long> e : byMonth.entrySet()) {
            YearMonth ym = e.getKey();
            Map<String, Object> point = new LinkedHashMap<>();
            int beYearShort = (ym.getYear() + 543) % 100;
            point.put("label", THAI_MONTHS[ym.getMonthValue() - 1] + " " + beYearShort);
            point.put("count", e.getValue());
            int heightPct = maxVal > 0 ? (int) Math.round((e.getValue() * 100.0) / maxVal) : 0;
            point.put("heightPct", Math.max(heightPct, e.getValue() > 0 ? 6 : 2));
            result.add(point);
        }
        return result;
    }

    // ─── ใหม่: ทัวร์ยอดจองสูงสุด (สำหรับ ranking list) ───
    public List<Map<String, Object>> getTopToursByBookingCount() {
        String[] colorPalette = { "#006e2f", "#22c55e", "#ff8e4d", "#735c00",
                "#6d28d9", "#0ea5e9", "#84cc16", "#ec4899" };

        List<Object[]> rows = tourRepository.countBookingsByTourName();
        long total = rows.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            String name = (String) row[0];
            long count = ((Number) row[1]).longValue();
            if (count == 0) continue; // ข้ามทัวร์ที่ยังไม่มีการจองเลย

            double pct = total > 0 ? Math.round((count * 1000.0) / total) / 10.0 : 0;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("count", count);
            item.put("percent", pct);
            item.put("color", colorPalette[i % colorPalette.length]);
            result.add(item);
        }
        return result;
    }
}