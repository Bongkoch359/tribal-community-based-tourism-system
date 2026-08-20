package com.example.miniproject.repository.Member;

import java.sql.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.miniproject.entity.Bookingroomdetail;
import com.example.miniproject.entity.Bookingroomdetailid;

@Repository
public interface BookingroomdetailRepository
        extends JpaRepository<Bookingroomdetail, Bookingroomdetailid> {

    @Query("""
        SELECT COALESCE(SUM(b.numofrooms),0)
        FROM Bookingroomdetail b
        WHERE b.roomtype.roomtypeid = :roomtypeId
        AND b.checkindate <= :checkoutDate
        AND b.checkoutdate >= :checkinDate
    """)
    Integer countBookedRooms(
            @Param("roomtypeId") String roomtypeId,
            @Param("checkinDate") Date checkinDate,
            @Param("checkoutDate") Date checkoutDate
    );

     /**
     * นับจำนวนห้องที่ถูกจองไปแล้ว "ณ วันที่ระบุ" (เช่น วันนี้)
     * ใช้แสดงในการ์ด "ห้องที่ว่าง" บน Dashboard
     */
    @Query("SELECT COALESCE(SUM(brd.numofrooms), 0) " +
           "FROM Bookingroomdetail brd " +
           "WHERE brd.roomtype.roomtypeid = :roomtypeid " +
           "AND brd.booking.bookingStatus <> com.example.miniproject.entity.enums.BookingStatus.CANCEL " +
           "AND brd.checkindate <= :onDate " +
           "AND brd.checkoutdate > :onDate")
    Integer countBookedRoomsOnDate(@Param("roomtypeid") String roomtypeid,
                                    @Param("onDate") Date onDate);

    /**
     * นับจำนวนห้องที่ถูกจองไปแล้ว "ในช่วงวันที่" (สำหรับตอนสมาชิกจองห้อง เช็คว่าห้องพอไหม)
     */
    @Query("SELECT COALESCE(SUM(brd.numofrooms), 0) " +
           "FROM Bookingroomdetail brd " +
           "WHERE brd.roomtype.roomtypeid = :roomtypeid " +
           "AND brd.booking.bookingStatus <> com.example.miniproject.entity.enums.BookingStatus.CANCEL " +
           "AND brd.checkindate < :checkoutdate " +
           "AND brd.checkoutdate > :checkindate")
    Integer countBookedRoomsInRange(@Param("roomtypeid") String roomtypeid,
                                     @Param("checkindate") Date checkindate,
                                     @Param("checkoutdate") Date checkoutdate);

    // /** ดึงรายละเอียดห้องที่จองทั้งหมดของ roomtype นั้น (เผื่อใช้ทำปฏิทิน) */
    // List<Bookingroomdetail> findByRoomtypeRoomtypeid(String roomtypeid);
}