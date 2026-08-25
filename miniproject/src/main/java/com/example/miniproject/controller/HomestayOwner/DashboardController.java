package com.example.miniproject.controller.HomestayOwner;

import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Homestayowner;
import com.example.miniproject.entity.Roomtype;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.service.Homestay.HomestayService;
import com.example.miniproject.service.Homestay.RoomTypeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.miniproject.repository.Member.BookingRepository;
import com.example.miniproject.repository.Member.BookingroomdetailRepository;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

@Controller
public class DashboardController {

        @Autowired
        private RoomTypeService roomTypeService;

        @Autowired
        private BookingRepository bookingRepository;

        @Autowired
        private BookingroomdetailRepository bookingRoomDetailRepository;
        @Autowired
        private HomestayService homestayService;

        @Autowired
        private com.example.miniproject.service.Homestay.HomestayOwnerService ownerService;

        @GetMapping("/owner/dashboard")
        public String dashboard(
                        @RequestParam(value = "homestayid", required = false) Integer homestayid,
                        HttpSession session,
                        Model model) {

                String ownerid = (String) session.getAttribute("ownerid");
                if (ownerid == null)
                        return "redirect:/owner/login";

                // ดึงโฮมสเตย์ทั้งหมดของเจ้าของคนนี้
                List<Homestay> myHomestays = homestayService.getHomestaysByOwnerId(ownerid);

                // ถ้าไม่ได้ส่ง homestayid มา → ใช้อันแรกของรายการ
                if (homestayid == null) {
                        if (!myHomestays.isEmpty()) {
                                homestayid = myHomestays.get(0).getHomestayid();
                        }
                }

                // ดึงชื่อโฮมสเตย์ที่เลือก
                String homestayname = "";
                if (homestayid != null) {
                        final Integer selectedId = homestayid;
                        homestayname = myHomestays.stream()
                                        .filter(hs -> selectedId.equals(hs.getHomestayid()))
                                        .map(Homestay::getHomestayname)
                                        .findFirst()
                                        .orElse("");
                }

                // ─── ห้องพัก ───
                List<Roomtype> rooms = (homestayid != null)
                                ? roomTypeService.getRoomTypesByHomestayId(homestayid)
                                : new java.util.ArrayList<>();

                long totalRoomTypes = rooms.size();
                java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

                long availableRooms = rooms.stream()
                                .filter(r -> "เปิดจอง".equals(r.getStatus()))
                                .mapToLong(r -> {
                                        int total = r.getTotalrooms() != null ? r.getTotalrooms() : 0;
                                        Integer booked = bookingRoomDetailRepository
                                                        .countBookedRoomsOnDate(r.getRoomtypeid(), today);
                                        long remain = total - (booked != null ? booked : 0);
                                        return Math.max(remain, 0); // กันติดลบ
                                })
                                .sum();

                // ─── การจองรอตรวจสอบ ───
                long pendingBookings = (homestayid != null)
                                ? bookingRepository.countByRoomHomestayIdAndStatus(homestayid,
                                                BookingStatus.WAITING_APPROVAL)
                                : 0;

                // ─── รายได้รวม (CONFIRMED) ───
                double totalRevenue = (homestayid != null)
                                ? bookingRepository.sumConfirmedRevenueByHomestayId(homestayid)
                                : 0.0;

                // ─── การจอง "รอตรวจสอบ" ล่าสุด 5 รายการ (สำหรับตารางแถวล่าง) ───
                List<Booking> recentPendingBookings = (homestayid != null)
                                ? bookingRepository.findTop5ByHomestayIdAndStatus(homestayid,
                                                BookingStatus.WAITING_APPROVAL)
                                : new java.util.ArrayList<>();
                // ─── แนวโน้มรายได้ต่อเดือน (6 เดือนล่าสุด, เติม 0 ในเดือนที่ไม่มีรายได้) ───
                java.time.YearMonth currentYM = java.time.YearMonth.now();
                java.time.YearMonth startYM = currentYM.minusMonths(5);
                java.sql.Date startOfRange = java.sql.Date.valueOf(startYM.atDay(1));

                Map<java.time.YearMonth, Double> revenueByMonth = new LinkedHashMap<>();
                for (int i = 5; i >= 0; i--) {
                        revenueByMonth.put(currentYM.minusMonths(i), 0.0);
                }
                if (homestayid != null) {
                        List<Object[]> rows = bookingRepository.sumRevenueByMonthByHomestayId(homestayid,
                                        startOfRange);
                        for (Object[] row : rows) {
                                int yr = ((Number) row[0]).intValue();
                                int mo = ((Number) row[1]).intValue();
                                double amount = ((Number) row[2]).doubleValue();
                                revenueByMonth.put(java.time.YearMonth.of(yr, mo), amount);
                        }
                }

                String[] thaiMonths = { "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
                                "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค." };

                List<Map<String, Object>> revenueTrend = new ArrayList<>();
                double maxMonthlyRevenue = revenueByMonth.values().stream()
                                .max(Double::compareTo).orElse(0.0);
                for (Map.Entry<java.time.YearMonth, Double> e : revenueByMonth.entrySet()) {
                        java.time.YearMonth ym = e.getKey();
                        Map<String, Object> point = new LinkedHashMap<>();
                        int beYearShort = (ym.getYear() + 543) % 100;
                        point.put("label", thaiMonths[ym.getMonthValue() - 1] + " " + beYearShort);
                        point.put("amount", e.getValue());
                        int heightPct = maxMonthlyRevenue > 0
                                        ? (int) Math.round((e.getValue() / maxMonthlyRevenue) * 100)
                                        : 0;
                        point.put("heightPct", Math.max(heightPct, e.getValue() > 0 ? 6 : 2));
                        revenueTrend.add(point);
                }
                // ─── สัดส่วนยอดจองตามประเภทห้อง (Donut Chart) ───
                String[] donutColorPalette = { "#2563eb", "#22c55e", "#f59e0b", "#a855f7",
                                "#ef4444", "#0ea5e9", "#84cc16", "#ec4899" };

                List<Object[]> roomTypeBookingRows = (homestayid != null)
                                ? bookingRoomDetailRepository.countBookingsByRoomType(homestayid)
                                : new java.util.ArrayList<>();

                long totalBookingsForDonut = roomTypeBookingRows.stream()
                                .mapToLong(row -> ((Number) row[1]).longValue())
                                .sum();

                List<Map<String, Object>> bookingRoomTypeDonut = new ArrayList<>();
                StringBuilder gradient = new StringBuilder("conic-gradient(");
                double cursor = 0;
                for (int i = 0; i < roomTypeBookingRows.size(); i++) {
                        Object[] row = roomTypeBookingRows.get(i);
                        String typeName = (String) row[0];
                        long count = ((Number) row[1]).longValue();
                        double pct = totalBookingsForDonut > 0
                                        ? Math.round((count * 1000.0 / totalBookingsForDonut)) / 10.0
                                        : 0;
                        String color = donutColorPalette[i % donutColorPalette.length];

                        Map<String, Object> seg = new LinkedHashMap<>();
                        seg.put("label", typeName);
                        seg.put("count", count);
                        seg.put("percent", pct);
                        seg.put("color", color);
                        bookingRoomTypeDonut.add(seg);

                        double start = cursor;
                        double end = cursor + pct;
                        gradient.append(color).append(" ").append(start).append("% ").append(end).append("%");
                        if (i < roomTypeBookingRows.size() - 1)
                                gradient.append(", ");
                        cursor = end;
                }
                gradient.append(")");
                String donutGradientStyle = totalBookingsForDonut > 0
                                ? gradient.toString()
                                : "conic-gradient(#e5e2d8 0% 100%)";

                model.addAttribute("ownername", session.getAttribute("ownername"));
                model.addAttribute("homestayname", homestayname);
                model.addAttribute("homestayid", homestayid);
                model.addAttribute("myHomestays", myHomestays);
                model.addAttribute("rooms", rooms);
                model.addAttribute("totalRoomTypes", totalRoomTypes);
                model.addAttribute("availableRooms", availableRooms);
                model.addAttribute("pendingBookings", pendingBookings);
                model.addAttribute("totalRevenue", totalRevenue);
                model.addAttribute("recentPendingBookings", recentPendingBookings);
                model.addAttribute("bookingRoomTypeDonut", bookingRoomTypeDonut);
                model.addAttribute("totalBookingsForDonut", totalBookingsForDonut);
                model.addAttribute("donutGradientStyle", donutGradientStyle);
                model.addAttribute("revenueTrend", revenueTrend);

                // ── ตรวจสอบข้อมูลธนาคาร + ลายเซ็น ──
                try {
                        Homestayowner owner = ownerService.getProfile(ownerid);

                        boolean bankInfoMissing = owner.getBankName() == null || owner.getBankName().isBlank()
                                        || owner.getAccountNumber() == null || owner.getAccountNumber().isBlank()
                                        || owner.getAccountName() == null || owner.getAccountName().isBlank();
                        model.addAttribute("bankInfoMissing", bankInfoMissing);

                        boolean signatureMissing = owner.getSignatureImageUrl() == null
                                        || owner.getSignatureImageUrl().isBlank();
                        model.addAttribute("signatureMissing", signatureMissing);

                        model.addAttribute("ownerEmail", owner.getEmail());
                } catch (Exception e) {
                        model.addAttribute("bankInfoMissing", false);
                        model.addAttribute("signatureMissing", false);
                }

                return "Homestay/dashboard";
        }
}