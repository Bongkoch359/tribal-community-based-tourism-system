package com.example.miniproject.repository.Member;

import com.example.miniproject.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

    // ค้นหาสมาชิกด้วยอีเมล (ใช้ใน Login และตรวจสอบอีเมลซ้ำ)
    Optional<Member> findByEmail(String email);

    // ตรวจสอบว่าอีเมลนี้ลงทะเบียนแล้วหรือยัง
    boolean existsByEmail(String email);
}