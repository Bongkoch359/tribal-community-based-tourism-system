package com.example.miniproject.repository.Homestay;

import com.example.miniproject.entity.Facilities;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilitiesRepository extends JpaRepository<Facilities, String> {
    // กันชื่อซ้ำ ไม่สนตัวพิมพ์เล็ก/ใหญ่
    Optional<Facilities> findByFacilitiesnameIgnoreCase(String facilitiesname);

    // หาเลขที่มากที่สุดจาก id รูปแบบ f001, f002 ... (ตัด 'f' ออกแล้วแปลงเป็นเลข)
    @Query(value = "SELECT MAX(CAST(SUBSTRING(facilitiesid, 2) AS UNSIGNED)) FROM facilities", nativeQuery = true)
    Integer findMaxFacilitiesNumericId();
}