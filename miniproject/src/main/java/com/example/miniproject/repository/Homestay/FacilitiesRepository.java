package com.example.miniproject.repository.Homestay;

import com.example.miniproject.entity.Facilities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilitiesRepository extends JpaRepository<Facilities, String> {
}