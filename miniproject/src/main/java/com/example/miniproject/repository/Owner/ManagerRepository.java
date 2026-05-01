package com.example.miniproject.repository.Owner;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.miniproject.entity.Communitymanager;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagerRepository extends JpaRepository<Communitymanager, String> {
    // สร้าง Method สำหรับ Query ข้อมูลด้วย Email และ Password
    Communitymanager findByEmailAndPassword(String email, String password);
}