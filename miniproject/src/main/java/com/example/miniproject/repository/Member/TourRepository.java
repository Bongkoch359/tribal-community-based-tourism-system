package com.example.miniproject.repository.Member;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.Tour;

public interface TourRepository extends JpaRepository<Tour, String> {

    List<Tour> findByTourmnameContainingIgnoreCase(String tourmname);
    List<Tour> findByCommunitymanagerManagerid(String managerId);
    List<Tour> findByStatus(String status);
    List<Tour> findByTourmnameContainingIgnoreCaseAndStatus(String tourmname, String status);
    List<Tour> findByCommunitymanager(Communitymanager communitymanager);

    @Query("""
    SELECT DISTINCT t FROM Tour t
    LEFT JOIN FETCH t.bookingTourDetails bd
    LEFT JOIN FETCH bd.booking b
    WHERE t.tourid = :tourid
""")
Optional<Tour> findByIdWithBookings(@Param("tourid") String tourid);

    @Query("""
        SELECT t FROM Tour t
        WHERE t.status = 'เปิดจอง'
        AND t.minSeatstour <= :guests
        AND t.maxSeatstour >= :guests
    """)
    List<Tour> findByAvailableSeats(@Param("guests") int guests);

    // ค้นหาแบบไม่มีวันที่
    @Query("""
        SELECT t FROM Tour t
        WHERE t.status = 'เปิดจอง'
        AND (:keyword IS NULL
             OR LOWER(t.tourmname) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:guests IS NULL
             OR :guests <= 1
             OR (t.minSeatstour <= :guests AND t.maxSeatstour >= :guests))
        ORDER BY t.tourmname ASC
    """)
    List<Tour> search(@Param("keyword") String keyword,
                      @Param("guests") Integer guests);

    long countByStatus(String status);

    @Query("SELECT t FROM Tour t LEFT JOIN t.bookingTourDetails d GROUP BY t ORDER BY COUNT(d) DESC")
    List<Tour> findTopToursByBookingCount(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT t FROM Tour t WHERE t.communitymanager.managerid = :managerid ORDER BY t.tourmname ASC")
    List<Tour> findByManagerId(@Param("managerid") String managerid);

    // ค้นหาแบบไม่มีวันที่ (เพิ่ม filter tourTypeId)
@Query("""
    SELECT t FROM Tour t
    WHERE t.status = 'เปิดจอง'
    AND (:keyword IS NULL
         OR LOWER(t.tourmname) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND (:tourTypeId IS NULL
         OR t.tourtype.typeId = :tourTypeId)
    AND (:guests IS NULL
         OR :guests <= 1
         OR (t.minSeatstour <= :guests AND t.maxSeatstour >= :guests))
    ORDER BY t.tourmname ASC
""")
List<Tour> search(@Param("keyword") String keyword,
                   @Param("guests") Integer guests,
                   @Param("tourTypeId") String tourTypeId);

// ค้นหาแบบมีวันที่ (เพิ่ม filter tourTypeId)
@Query("""
    SELECT t FROM Tour t
    WHERE t.status = 'เปิดจอง'
    AND (:keyword IS NULL
         OR LOWER(t.tourmname) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND (:tourTypeId IS NULL
         OR t.tourtype.typeId = :tourTypeId)
    AND (:guests IS NULL OR :guests <= 1
         OR (t.minSeatstour <= :guests AND t.maxSeatstour >= :guests))
    AND (:startDate IS NULL OR :endDate IS NULL OR (
        t.maxSeatstour - (
            SELECT COALESCE(SUM(d.numofadult + COALESCE(d.numofchild, 0)), 0)
            FROM Bookingtourdetail d
            WHERE d.tour = t
            AND d.startdate BETWEEN :startDate AND :endDate
        ) >= :guests
    ))
    ORDER BY t.tourmname ASC
""")
List<Tour> searchWithDate(
    @Param("keyword")    String keyword,
    @Param("guests")     Integer guests,
    @Param("startDate")  java.sql.Date startDate,
    @Param("endDate")    java.sql.Date endDate,
    @Param("tourTypeId") String tourTypeId
);
//คิวรีที่นั่ง
@Query("""
    SELECT t.tourid,
           COALESCE(SUM(
               CASE WHEN b.bookingStatus <> com.example.miniproject.entity.enums.BookingStatus.CANCEL
                    THEN d.numofadult + COALESCE(d.numofchild, 0)
                    ELSE 0 END
           ), 0)
    FROM Tour t
    LEFT JOIN t.bookingTourDetails d
    LEFT JOIN d.booking b
    GROUP BY t.tourid
""")
List<Object[]> findBookedSeatsAll();


}