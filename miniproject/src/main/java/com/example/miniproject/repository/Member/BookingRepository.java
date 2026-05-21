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

    // BookingRepository.java
@Query("""
    SELECT b FROM Booking b
    LEFT JOIN FETCH b.member
    LEFT JOIN FETCH b.roomDetails rd
    LEFT JOIN FETCH rd.roomtype rt
    LEFT JOIN FETCH rt.homestay
    LEFT JOIN FETCH b.guests
    WHERE b.bookingid = :id
""")
Optional<Booking> findByIdWithDetails(@Param("id") String id);

@Query("SELECT COUNT(b) FROM Booking b " +
       "JOIN b.roomDetails rd " +
       "JOIN rd.roomtype rt " +
       "WHERE rt.homestay.homestayid = :homestayId " +
       "AND b.bookingStatus = :status")
long countByRoomHomestayIdAndStatus(
    @Param("homestayId") Integer homestayId,
    @Param("status") BookingStatus status);

    // // นับจำนวนการจองเฉพาะของ manager
    // long countByCommunitymanager_Managerid(String managerid);

    // // ดึงการจอง 5 รายการล่าสุดเฉพาะของ manager
    // @Query("SELECT b FROM Booking b WHERE b.communitymanager.managerid = :managerId ORDER BY b.bookingdate DESC")
    // List<Booking> findTopByManager(@Param("managerId") String managerId, Pageable pageable);
}