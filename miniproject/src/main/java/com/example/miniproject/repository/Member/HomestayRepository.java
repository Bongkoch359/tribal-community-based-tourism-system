package com.example.miniproject.repository.Member;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.miniproject.entity.Homestay;

public interface HomestayRepository extends JpaRepository<Homestay, String> {
    // step 7.1: ค้นหาโฮมสเตย์จากชื่อ
    List<Homestay> findByHomestaynameContainingIgnoreCase(String homestayname);
    List<Homestay> findByAddressContainingIgnoreCase(String address);
}