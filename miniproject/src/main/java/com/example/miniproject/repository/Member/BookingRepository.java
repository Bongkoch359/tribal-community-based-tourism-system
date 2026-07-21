package com.example.miniproject.repository.Member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.entity.enums.BookingType;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    List<Booking> findTop5ByOrderByBookingdateDesc();
 
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingStatus = 'PENDING'")
    long countPendingBookings();

    /** ดึงการจองทั้งหมดของ Member คนนั้น */
    List<Booking> findByMemberMemberidOrderByBookingdateDesc(String memberid);

    /** ดึงการจองตาม type */
    List<Booking> findByBookingType(BookingType bookingType);

    /** ดึงตาม status */
    List<Booking> findByBookingStatus(BookingStatus bookingStatus);

    /** รายได้รวมทั้งระบบ */
    @Query("SELECT COALESCE(SUM(b.totalamount), 0) FROM Booking b WHERE b.bookingStatus = :status")
    double sumTotalRevenue(@Param("status") BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingStatus = :status")
    long countByBookingStatus(@Param("status") BookingStatus status);


@Query("""
    SELECT DISTINCT b FROM Booking b
    LEFT JOIN FETCH b.member
    LEFT JOIN FETCH b.roomDetails rd
    LEFT JOIN FETCH rd.roomtype rt
    LEFT JOIN FETCH rt.homestay
    LEFT JOIN FETCH b.guests
    WHERE b.bookingid = :id
""")
Optional<Booking> findByIdWithDetails(@Param("id") String id);
    /** นับการจองรอตรวจสอบของ homestay */
@Query("SELECT COUNT(b) FROM Booking b " +
       "JOIN b.roomDetails rd " +
       "JOIN rd.roomtype rt " +
       "WHERE rt.homestay.homestayid = :homestayId " +
       "AND b.bookingStatus = :status")
long countByRoomHomestayIdAndStatus(
    @Param("homestayId") Integer homestayId,
    @Param("status") BookingStatus status);
 
 
    /** รายได้รวมของ homestay (เฉพาะ CONFIRMED) */
    @Query("SELECT COALESCE(SUM(b.totalamount), 0) FROM Booking b " +
           "JOIN b.roomDetails rd " +
           "JOIN rd.roomtype rt " +
           "WHERE rt.homestay.homestayid = :homestayId " +
           "AND b.bookingStatus = :status")
    double sumRevenueByHomestayId(
        @Param("homestayId") Integer homestayId,
        @Param("status") BookingStatus status);
 
    /** การจองล่าสุด 5 รายการของ homestay */
    @Query("SELECT b FROM Booking b " +
           "JOIN b.roomDetails rd " +
           "JOIN rd.roomtype rt " +
           "WHERE rt.homestay.homestayid = :homestayId " +
           "ORDER BY b.bookingdate DESC " +
           "LIMIT 5")
    List<Booking> findTop5ByHomestayId(@Param("homestayId") Integer homestayId);
    
    // ───  ดึงการจองทั้งหมดของ homestay  ───────────
    /** ดึงการจองทั้งหมดของ homestay เรียงวันที่ล่าสุดก่อน */
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.member " +
           "LEFT JOIN FETCH b.roomDetails rd " +
           "LEFT JOIN FETCH rd.roomtype rt " +
           "WHERE rt.homestay.homestayid = :homestayId " +
           "ORDER BY b.bookingdate DESC")
    List<Booking> findAllByHomestayId(@Param("homestayId") Integer homestayId);
 
    /** ดึงการจองของ homestay กรอง status */
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.member " +
           "LEFT JOIN FETCH b.roomDetails rd " +
           "LEFT JOIN FETCH rd.roomtype rt " +
           "WHERE rt.homestay.homestayid = :homestayId " +
           "AND b.bookingStatus = :status " +
           "ORDER BY b.bookingdate DESC")
    List<Booking> findAllByHomestayIdAndStatus(
        @Param("homestayId") Integer homestayId,
        @Param("status") BookingStatus status);

        

}