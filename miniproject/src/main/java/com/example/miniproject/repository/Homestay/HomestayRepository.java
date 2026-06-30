package com.example.miniproject.repository.Homestay;

import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Homestayowner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface HomestayRepository extends JpaRepository<Homestay, Integer> {

   List<Homestay> findByOwner_Ownerid(Integer ownerid);

    List<Homestay> findByHomestaynameContainingIgnoreCase(String homestayname);
    List<Homestay> findByAddressContainingIgnoreCase(String address);

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


   List<Homestay> findByHomestaynameContainingIgnoreCaseOrAddressContainingIgnoreCase(
    String homestayname, String address);

   // ค้นหาโฮมสเตย์ที่มีห้องว่างในช่วงวันที่
@Query("""
    SELECT DISTINCT h FROM Homestay h
    JOIN h.roomtypes r
    WHERE (:keyword IS NULL
           OR LOWER(h.homestayname) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(h.address) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND r.status = 'เปิดจอง'
    AND (:startDate IS NULL OR :endDate IS NULL OR NOT EXISTS (
        SELECT d FROM Bookingroomdetail d
        WHERE d.roomtype = r
        AND NOT (d.checkoutdate <= :startDate OR d.checkindate >= :endDate)
    ))
    ORDER BY h.homestayname ASC
""")
List<Homestay> searchWithDate(
    @Param("keyword")   String keyword,
    @Param("startDate") java.sql.Date startDate,
    @Param("endDate")   java.sql.Date endDate
);
}