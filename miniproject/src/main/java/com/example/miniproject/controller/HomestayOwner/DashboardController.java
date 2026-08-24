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
                // ─── การจองล่าสุด 5 รายการ ───
                List<Booking> recentBookings = (homestayid != null)
                                ? bookingRepository.findTop5ByHomestayId(homestayid)
                                : new java.util.ArrayList<>();

                // ─── กิจกรรมวันนี้: เช็คอิน / เช็คเอาท์ / ทำความสะอาด ───
                long checkinsToday = (homestayid != null)
                                ? bookingRepository.countCheckinsTodayByHomestayId(homestayid, today)
                                : 0;
                long checkoutsToday = (homestayid != null)
                                ? bookingRepository.countCheckoutsTodayByHomestayId(homestayid, today)
                                : 0;
                // ห้องที่ต้องทำความสะอาด = ห้องที่เพิ่งเช็คเอาท์วันนี้
                long cleaningToday = checkoutsToday;

                // ─── แนวโน้มรายได้ 7 วันล่าสุด (เติม 0 ในวันที่ไม่มีรายได้) ───
                java.sql.Date sevenDaysAgo = java.sql.Date.valueOf(
                                java.time.LocalDate.now().minusDays(6));

                Map<java.time.LocalDate, Double> revenueByDate = new LinkedHashMap<>();
                for (int i = 6; i >= 0; i--) {
                        revenueByDate.put(java.time.LocalDate.now().minusDays(i), 0.0);
                }
                if (homestayid != null) {
                        List<Object[]> rows = bookingRepository.sumRevenueByDayByHomestayId(homestayid,
                                        sevenDaysAgo);
                        for (Object[] row : rows) {
                                java.sql.Date d = (java.sql.Date) row[0];
                                Double amount = ((Number) row[1]).doubleValue();
                                revenueByDate.put(d.toLocalDate(), amount);
                        }
                }

                List<Map<String, Object>> revenueTrend = new ArrayList<>();
                double maxDailyRevenue = revenueByDate.values().stream()
                                .max(Double::compareTo).orElse(0.0);
                java.time.format.DateTimeFormatter dayFmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
                for (Map.Entry<java.time.LocalDate, Double> e : revenueByDate.entrySet()) {
                        Map<String, Object> point = new LinkedHashMap<>();
                        point.put("label", e.getKey().format(dayFmt));
                        point.put("amount", e.getValue());
                        int heightPct = maxDailyRevenue > 0
                                        ? (int) Math.round((e.getValue() / maxDailyRevenue) * 100)
                                        : 0;
                        point.put("heightPct", Math.max(heightPct, e.getValue() > 0 ? 6 : 2));
                        revenueTrend.add(point);
                }

                model.addAttribute("ownername", session.getAttribute("ownername"));
                model.addAttribute("homestayname", homestayname);
                model.addAttribute("homestayid", homestayid);
                model.addAttribute("myHomestays", myHomestays);
                model.addAttribute("rooms", rooms);
                model.addAttribute("totalRoomTypes", totalRoomTypes);
                model.addAttribute("availableRooms", availableRooms);
                model.addAttribute("pendingBookings", pendingBookings);
                model.addAttribute("totalRevenue", totalRevenue);
                model.addAttribute("recentBookings", recentBookings);
                model.addAttribute("checkinsToday", checkinsToday);
                model.addAttribute("checkoutsToday", checkoutsToday);
                model.addAttribute("cleaningToday", cleaningToday);
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