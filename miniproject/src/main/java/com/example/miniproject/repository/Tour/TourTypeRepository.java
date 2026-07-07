package com.example.miniproject.repository.Tour;

import java.util.Optional;
 
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.miniproject.entity.TourType;
 
public interface TourTypeRepository extends JpaRepository<TourType, String> {
 
    // ใช้เช็คว่ามีชื่อประเภททัวร์นี้อยู่แล้วหรือยัง (กันสร้างซ้ำ)
    Optional<TourType> findByTypename(String typename);
}
 
