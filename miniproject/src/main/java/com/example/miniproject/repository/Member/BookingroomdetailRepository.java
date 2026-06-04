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
}