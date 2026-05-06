package com.example.miniproject.service.Tour;

import com.example.miniproject.dto.Tour.DashboardStatsDTO;
import com.example.miniproject.dto.Tour.BookingRowDTO;
import com.example.miniproject.dto.Tour.TourRowDTO;
import com.example.miniproject.dto.Tour.PostRowDTO;
import com.example.miniproject.dto.Tour.ActivityLogDTO;
import com.example.miniproject.repository.Member.BookingRepository;
import com.example.miniproject.repository.Member.TourRepository;
import com.example.miniproject.repository.Member.ActivitypostRepository;
import com.example.miniproject.repository.Tour.PaymentRepository;
import com.example.miniproject.entity.enums.BookingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import java.util.List;
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
        dto.setTotalBookings(bookingRepository.count());
        dto.setActiveTours(tourRepository.countByStatus("OPEN"));
        dto.setTotalTours(tourRepository.count());
        dto.setTotalPosts(activitypostRepository.count());
        dto.setTotalRevenue(paymentRepository.sumPaidAmount());
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
                    row.setStatus(b.getBookingStatus() != null ? b.getBookingStatus().name() : "-");
                    return row;
                })
                .collect(Collectors.toList());
    }
 
    // ทัวร์ยอดนิยม (เรียงตามจำนวนการจอง)
    public List<TourRowDTO> getPopularTours(int limit) {
        return tourRepository.findTopToursByBookingCount(limit)
                .stream()
                .map(t -> {
                    TourRowDTO row = new TourRowDTO();
                    row.setTourid(t.getTourid());
                    row.setTourname(t.getTourmname());
                    row.setAdultprice(t.getAdultprice());
                    row.setStatus(t.getStatus());
                    row.setBookingCount(t.getBookingTourDetails() != null ? t.getBookingTourDetails().size() : 0);
                    return row;
                })
                .collect(Collectors.toList());
    }
 
    // โพสต์กิจกรรมล่าสุด
    public List<PostRowDTO> getRecentPosts(int limit) {
        return activitypostRepository.findTop3ByOrderByCreateddateDesc()
                .stream()
                .limit(limit)
                .map(p -> {
                    PostRowDTO row = new PostRowDTO();
                    row.setActivityid(p.getActivityid());
                    row.setTitle(p.getTitle());
                    row.setLocation(p.getLocation());
                    row.setStatus(p.getStatus() != null ? p.getStatus().name() : "-");
                    return row;
                })
                .collect(Collectors.toList());
    }
 
    // activity log จำลอง (หรือดึงจาก log table ถ้ามี)
    public List<ActivityLogDTO> getRecentActivityLog(int limit) {
        // ดึงการจองล่าสุดมาแปลงเป็น activity log
        return bookingRepository.findTop5ByOrderByBookingdateDesc()
                .stream()
                .limit(limit)
                .map(b -> {
                    ActivityLogDTO log = new ActivityLogDTO();
                    log.setMessage("การจอง " + b.getBookingid() + " - " +
                            (b.getBookingStatus() != null ? b.getBookingStatus().name() : "ไม่ระบุ"));
                    log.setType(resolveLogType(b.getBookingStatus()));
                    log.setTimestamp(b.getBookingdate() != null ? b.getBookingdate().toString() : "-");
                    return log;
                })
                .collect(Collectors.toList());
    }
 
    private String resolveLogType(BookingStatus status) {
        if (status == null) return "gray";
        return switch (status) {
            case CONFIRMED -> "green";
            case PENDING -> "amber";
            case CANCEL -> "red";
            default -> "gray";
        };
    }
}
 
