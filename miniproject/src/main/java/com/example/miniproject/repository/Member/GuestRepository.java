package com.example.miniproject.repository.Member;

import com.example.miniproject.entity.Guest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestRepository extends JpaRepository<Guest, String> {
     List<Guest> findByBooking_Bookingid(String bookingid);
    
}