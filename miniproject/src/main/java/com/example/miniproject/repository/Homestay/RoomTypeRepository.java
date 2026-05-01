package com.example.miniproject.repository.Homestay;

import com.example.miniproject.entity.Roomtype;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RoomTypeRepository extends JpaRepository<Roomtype, String> {

    @Query("SELECT r FROM Roomtype r WHERE r.homestay.homestayid = :homestayid")
    List<Roomtype> findByHomestayId(@Param("homestayid") Integer homestayid);
}