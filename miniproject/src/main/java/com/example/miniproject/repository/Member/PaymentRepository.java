package com.example.miniproject.repository.Member;

import com.example.miniproject.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
 
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
 
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = 'PAID'")
    Double sumPaidAmount();

    
    Payment findByBooking_Bookingid(String bookingid);

}
