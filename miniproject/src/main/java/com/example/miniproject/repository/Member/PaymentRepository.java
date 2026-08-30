package com.example.miniproject.repository.Member;

import com.example.miniproject.entity.Payment;
import com.example.miniproject.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    // ── มีอยู่เดิม ──────────────────────────────────────────────
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = 'PAID'")
    Double sumPaidAmount();

    Payment findByBooking_Bookingid(String bookingid);

    // ── เพิ่มใหม่ ────────────────────────────────────────────────

    // ใช้ใน HomestayPaymentServiceImpl (Optional เพื่อ orElseGet)
    Optional<Payment> findOptionalByBooking_Bookingid(String bookingid);

    // ดึงทุก Payment ของโฮมสเตย์นั้น (เจ้าของโฮมสเตย์ดู dashboard)
    @Query("""
                SELECT p
                FROM Payment p
                JOIN p.booking.roomDetails rd
                WHERE rd.roomtype.homestay.homestayid = :homestayId
            """)
    List<Payment> findByHomestayId(@Param("homestayId") String homestayId);

    @Query("""
                SELECT p
                FROM Payment p
                JOIN p.booking.roomDetails rd
                WHERE rd.roomtype.homestay.homestayid = :homestayId
                AND p.paymentStatus = :status
            """)
    List<Payment> findByHomestayIdAndStatus(@Param("homestayId") String homestayId,
            @Param("status") PaymentStatus status);

    /**
     * รายได้รวมรายเดือนในปีที่กำหนด (เฉพาะ CONFIRMED)//หน้าแดชบอช
     * Return: Object[]{month (Integer), totalRevenue (Double)}
     */
    @Query("""
                SELECT MONTH(p.paymentdate) AS month,
                       COALESCE(SUM(p.amount), 0) AS totalRevenue
                FROM Payment p
                WHERE YEAR(p.paymentdate) = :year
                  AND p.paymentStatus = com.example.miniproject.entity.enums.PaymentStatus.PAID
                GROUP BY MONTH(p.paymentdate)
                ORDER BY MONTH(p.paymentdate)
            """)
    List<Object[]> findMonthlyRevenue(@Param("year") int year);

    @Query("""
                SELECT COALESCE(SUM(p.amount), 0)
                FROM Payment p
                JOIN p.booking b
                JOIN b.tourDetails td
                JOIN td.tour t
                WHERE t.communitymanager.managerid = :managerId
                AND p.paymentStatus = com.example.miniproject.entity.enums.PaymentStatus.PAID
            """)
    Double sumPaidAmountByManagerId(@Param("managerId") String managerId);
}