package com.example.miniproject.repository.Member;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.entity.enums.BookingType;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    /** ดึงการจองทั้งหมดของ Member คนนั้น */
    List<Booking> findByMemberMemberidOrderByBookingdateDesc(String memberid);

    /** ดึงการจองตาม type */
    List<Booking> findByBookingType(BookingType bookingType);

    /** ดึงตาม status */
    List<Booking> findByBookingStatus(BookingStatus bookingStatus);
}