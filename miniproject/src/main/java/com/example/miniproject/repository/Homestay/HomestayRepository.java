package com.example.miniproject.repository.Homestay;

import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Homestayowner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface HomestayRepository extends JpaRepository<Homestay, Integer> {

   List<Homestay> findByOwner_Ownerid(String ownerid);

    List<Homestay> findByHomestaynameContainingIgnoreCase(String homestayname);
    List<Homestay> findByAddressContainingIgnoreCase(String address);

    /**top 5 รีวิวต่ำสุดทั้งระบบ */
    @Query("""
    SELECT rt.homestay.homestayid, rt.homestay.homestayname, AVG(r.rating)
    FROM Review r
    JOIN r.booking b JOIN b.roomDetails rd JOIN rd.roomtype rt
    GROUP BY rt.homestay.homestayid, rt.homestay.homestayname
    ORDER BY AVG(r.rating) ASC
""")
List<Object[]> findLowestRatedHomestays(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END " +
           "FROM Homestay h WHERE h.homestayid = :homestayid " +
           "AND h.owner.ownerid = :ownerid")
    boolean existsByHomestayidAndOwnerOwnerid(
            @Param("homestayid") Integer homestayid,  
            @Param("ownerid") String ownerid
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
// ค้นหาโฮมสเตย์ตาม keyword/guests เท่านั้น 
// (ไม่กรองเรื่องห้องว่าง/เต็ม เพราะต้องปล่อยให้ Controller เอา hsAvailability 
//  ไปตัดสินใจแสดง badge "ห้องเต็ม" แทนการซ่อนการ์ดทิ้งไปเลย)
@Query("""
    SELECT DISTINCT h FROM Homestay h
    JOIN h.roomtypes r
    WHERE h.owner.verificationstatus = true
    AND h.owner.accountstatus = 'ACTIVE'
    AND (:keyword IS NULL
           OR LOWER(h.homestayname) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(h.address) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND r.status = 'เปิดจอง'
    AND (:guests IS NULL OR :guests <= 1 OR r.maxguest >= :guests)
    ORDER BY h.homestayname ASC
""")
List<Homestay> searchWithDate(
    @Param("keyword")   String keyword,
    @Param("guests")    Integer guests,
    @Param("startDate") java.sql.Date startDate,
    @Param("endDate")   java.sql.Date endDate
);

@Query("""
    SELECT DISTINCT h FROM Homestay h
    JOIN h.roomtypes r
    WHERE h.owner.verificationstatus = true
    AND h.owner.accountstatus = 'ACTIVE'
    AND (:keyword IS NULL
           OR LOWER(h.homestayname) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(h.address) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND r.status = 'เปิดจอง'
    AND (:guests IS NULL OR :guests <= 1 OR r.maxguest >= :guests)
    ORDER BY h.homestayname ASC
""")
List<Homestay> searchByGuestOnly(
    @Param("keyword") String keyword,
    @Param("guests")  Integer guests
);
}