package com.example.miniproject.repository.Member;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.miniproject.entity.Tour;

public interface TourRepository extends JpaRepository<Tour, String> {
    // step 6.1: ค้นหาทัวร์จากชื่อ (tourmname ตาม entity)
    List<Tour> findByTourmnameContainingIgnoreCase(String tourmname);
}