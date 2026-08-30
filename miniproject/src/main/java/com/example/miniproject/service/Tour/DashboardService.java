package com.example.miniproject.service.Tour;

import com.example.miniproject.dto.Tour.DashboardStatsDTO;
import com.example.miniproject.dto.Tour.BookingRowDTO;
import com.example.miniproject.dto.Tour.PostRowDTO;
import com.example.miniproject.dto.Tour.MonthlyRevenueDTO;
import com.example.miniproject.repository.Member.BookingRepository;
import com.example.miniproject.repository.Member.TourRepository;
import com.example.miniproject.repository.Tour.TourScheduleRepository;
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

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    private static final String[] THAI_MONTHS = {
            "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
            "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."
    };

    // รวมสถิติภาพรวม
    public DashboardStatsDTO getDashboardStats(String managerId) {
        DashboardStatsDTO dto = new DashboardStatsDTO();
        dto.setTotalTours(tourRepository.countByCommunitymanagerManagerid(managerId));
        dto.setActiveTours(tourRepository.countActivePublishedByManagerId(managerId));
        Double revenue = paymentRepository.sumPaidAmountByManagerId(managerId);
        dto.setTotalRevenue(revenue != null ? revenue : 0.0);
        dto.setPendingBookings(bookingRepository.countPendingTourBookingsByManagerId(managerId));
        return dto;
    }

    // จำนวนโพสต์กิจกรรมของ manager คนนั้น (ไม่ใช่ทั้งระบบ)
    public long getTotalPostCount(String managerId) {
        return activitypostRepository.countByCommunitymanagerManagerid(managerId);
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

    // ─── ใหม่: แนวโน้มรายได้ทัวร์รายเดือน แบบเลือกช่วงได้ (เฉพาะของ manager คนนั้น) ───
    public List<Map<String, Object>> getTourRevenueTrend(String managerId, YearMonth startYM, YearMonth endYM) {
        java.sql.Date startDate = java.sql.Date.valueOf(startYM.atDay(1));
        java.sql.Date endDate = java.sql.Date.valueOf(endYM.atEndOfMonth());

        Map<YearMonth, Double> byMonth = new LinkedHashMap<>();
        YearMonth cursor = startYM;
        while (!cursor.isAfter(endYM)) {
            byMonth.put(cursor, 0.0);
            cursor = cursor.plusMonths(1);
        }

        List<Object[]> rows = bookingRepository.sumTourRevenueByMonthRangeAndManager(managerId, startDate, endDate);
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

    // ─── ใหม่: อัตราการจองเต็มที่นั่งรายเดือน (Fill Rate) ของ manager คนนั้น ───
    public List<Map<String, Object>> getTourFillRateTrend(String managerId, YearMonth startYM, YearMonth endYM) {
        java.sql.Date startDate = java.sql.Date.valueOf(startYM.atDay(1));
        java.sql.Date endDate = java.sql.Date.valueOf(endYM.atEndOfMonth());

        Map<YearMonth, Long> bookedSeatsByMonth = new LinkedHashMap<>();
        Map<YearMonth, Long> capacityByMonth = new LinkedHashMap<>();
        YearMonth cursor = startYM;
        while (!cursor.isAfter(endYM)) {
            bookedSeatsByMonth.put(cursor, 0L);
            capacityByMonth.put(cursor, 0L);
            cursor = cursor.plusMonths(1);
        }

        List<Object[]> rows = tourScheduleRepository.findScheduleFillDataForManager(managerId, startDate, endDate);
        for (Object[] row : rows) {
            java.sql.Date opendate = (java.sql.Date) row[0];
            int maxSeats = row[1] != null ? ((Number) row[1]).intValue() : 0;
            long bookedSeats = ((Number) row[2]).longValue();

            YearMonth ym = YearMonth.from(opendate.toLocalDate());
            if (!bookedSeatsByMonth.containsKey(ym))
                continue; // กันเหนียวเผื่อ opendate หลุดช่วง

            bookedSeatsByMonth.merge(ym, bookedSeats, Long::sum);
            capacityByMonth.merge(ym, (long) maxSeats, Long::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (YearMonth ym : bookedSeatsByMonth.keySet()) {
            long booked = bookedSeatsByMonth.get(ym);
            long capacity = capacityByMonth.get(ym);

            double fillRatePct = capacity > 0
                    ? Math.round((booked * 1000.0) / capacity) / 10.0
                    : 0.0;
            fillRatePct = Math.min(fillRatePct, 100.0);

            Map<String, Object> point = new LinkedHashMap<>();
            int beYearShort = (ym.getYear() + 543) % 100;
            point.put("label", THAI_MONTHS[ym.getMonthValue() - 1] + " " + beYearShort);
            point.put("fillRatePct", fillRatePct);
            int heightPct = (int) Math.round(fillRatePct);
            point.put("heightPct", Math.max(heightPct, fillRatePct > 0 ? 6 : 2));
            result.add(point);
        }
        return result;
    }

    // ─── ใหม่: ทัวร์ยอดจองสูงสุด (สำหรับ ranking list) — เฉพาะของ manager คนนั้น ───
    public List<Map<String, Object>> getTopToursByBookingCount(String managerId) {
        String[] colorPalette = { "#006e2f", "#22c55e", "#ff8e4d", "#735c00",
                "#6d28d9", "#0ea5e9", "#84cc16", "#ec4899" };

        List<Object[]> rows = tourRepository.countBookingsByTourNameForManager(managerId);
        long total = rows.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            String name = (String) row[0];
            long count = ((Number) row[1]).longValue();
            if (count == 0)
                continue; // ข้ามทัวร์ที่ยังไม่มีการจองเลย

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