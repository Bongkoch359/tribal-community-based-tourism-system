package com.example.miniproject.repository.Tour;

import com.example.miniproject.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
 
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
 
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = 'PAID'")
    Double sumPaidAmount();
}
