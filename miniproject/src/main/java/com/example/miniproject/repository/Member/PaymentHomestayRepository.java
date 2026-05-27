package com.example.miniproject.repository.Member;

import com.example.miniproject.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentHomestayRepository
extends JpaRepository<Payment, String> {

}