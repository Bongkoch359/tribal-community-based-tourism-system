package com.example.miniproject.repository.Homestay;

import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Homestayowner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface HomestayRepository extends JpaRepository<Homestay, Integer> {

    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END " +
           "FROM Homestay h WHERE h.homestayid = :homestayid " +
           "AND h.owner.ownerid = :ownerid")
    boolean existsByHomestayidAndOwnerOwnerid(
            @Param("homestayid") Integer homestayid,  
            @Param("ownerid") Integer ownerid
    );

    @Query("SELECT h FROM Homestay h WHERE h.owner = :owner")
    List<Homestay> findByOwner(@Param("owner") Homestayowner owner);

    // ดึง avg rating ของ homestay จาก Review → Booking → Bookingroomdetail → Roomtype → Homestay
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r " +
       "JOIN r.booking b JOIN b.roomDetails rd JOIN rd.roomtype rt " +
       "WHERE rt.homestay.homestayid = :homestayid")
    Double avgRatingByHomestayId(@Param("homestayid") Integer homestayid);

    @Query("SELECT COUNT(r) FROM Review r " +
       "JOIN r.booking b JOIN b.roomDetails rd JOIN rd.roomtype rt " +
       "WHERE rt.homestay.homestayid = :homestayid")
    Long countReviewByHomestayId(@Param("homestayid") Integer homestayid);

    @Query("SELECT COUNT(r) FROM Roomtype r WHERE r.homestay.homestayid = :homestayid")
   Long countRoomTypeByHomestayId(@Param("homestayid") Integer homestayid);

   @Query("SELECT COUNT(b) FROM Booking b JOIN b.roomDetails rd " +
       "JOIN rd.roomtype rt WHERE rt.homestay.homestayid = :homestayid " +
       "AND b.bookingStatus = 'pending'")
   Long countPendingBookingByHomestayId(@Param("homestayid") Integer homestayid);
}