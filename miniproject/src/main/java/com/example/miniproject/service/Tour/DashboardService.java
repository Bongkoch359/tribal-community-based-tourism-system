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
import java.util.ArrayList;
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

    // รวมสถิติภาพรวม
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO dto = new DashboardStatsDTO();
        dto.setTotalTours(tourRepository.count());
        dto.setTotalRevenue(paymentRepository.sumPaidAmount());
        dto.setPendingBookings(bookingRepository.countPendingBookings());
        return dto;
    }

    // 5 การจองล่าสุด
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

    // โพสต์กิจกรรมล่าสุด (ไม่กรองสถานะแล้ว)
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

    // รายได้รายเดือนของปีปัจจุบัน (12 เดือนครบ แม้บางเดือนเป็น 0)
    public List<MonthlyRevenueDTO> getMonthlyRevenue() {
        int year = LocalDate.now().getYear();

        // ดึงจาก DB: Object[]{ month(Integer), revenue(Double) }
        List<Object[]> rows = paymentRepository.findMonthlyRevenue(year);

        // แปลงเป็น map month->revenue
        Map<Integer, Double> revenueMap = rows.stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r[0]).intValue(),
                        r -> ((Number) r[1]).doubleValue()
                ));

        // สร้างครบ 12 เดือน (เดือนที่ไม่มีข้อมูลใส่ 0)
        List<MonthlyRevenueDTO> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            result.add(new MonthlyRevenueDTO(m, revenueMap.getOrDefault(m, 0.0)));
        }
        return result;
    }
}