package com.example.miniproject.repository.Member;

import com.example.miniproject.entity.Review;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    @Query("SELECT r FROM Review r " +
           "JOIN r.booking b JOIN b.roomDetails rd JOIN rd.roomtype rt " +
           "WHERE rt.homestay.homestayid = :homestayid")
    List<Review> findByHomestayId(@Param("homestayid") Integer homestayid);
}