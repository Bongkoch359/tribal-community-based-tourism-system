package com.example.miniproject.service.Member;

import com.example.miniproject.entity.Member;
import com.example.miniproject.repository.Member.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    public String registerUser(Member member, String confirmPassword) {
        System.out.println("Registering: " + member.getFirstname() + " " + member.getLastname());

       
        if (member.getFirstname() == null || member.getFirstname().isBlank() ||
            member.getLastname()  == null || member.getLastname().isBlank()  ||
            member.getEmail()     == null || member.getEmail().isBlank()     ||
            member.getPassword()  == null || member.getPassword().isBlank()) {
            return "กรุณากรอกข้อมูลให้ถูกต้อง";
        }

        
        if (memberRepository.existsByEmail(member.getEmail())) {
            return "ข้อมูลผู้ใช้ซ้ำ กรุณาลองใหม่อีกครั้ง";
        }

        
        if (!member.getPassword().equals(confirmPassword)) {
            return "รหัสผ่านไม่ตรงกัน";
        }

        if (member.getMemberid() == null || member.getMemberid().isBlank()) {
            String generatedId = "M" + System.currentTimeMillis();
            member.setMemberid(generatedId);
        }

        try {
            memberRepository.save(member);
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return "เกิดข้อผิดพลาด กรุณาลองใหม่";
        }
    }

    
    public String loginMember(String email, String password) {
        if (email == null || email.isBlank() ||
            password == null || password.isBlank()) {
            return "กรุณากรอกอีเมลและรหัสผ่าน";
        }

        Optional<Member> memberOpt = memberRepository.findByEmail(email);

        if (memberOpt.isEmpty()) {
            return "อีเมลหรือรหัสผ่านไม่ถูกต้อง กรุณาลองใหม่อีกครั้ง";
        }

        Member member = memberOpt.get();

        if (!member.getPassword().equals(password)) {
            return "อีเมลหรือรหัสผ่านไม่ถูกต้อง กรุณาลองใหม่อีกครั้ง";
        }

        return "SUCCESS";
    }

   
    public Optional<Member> getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    // ค้นหาด้วย ID (String)
    public Optional<Member> getMemberById(String memberId) {
        return memberRepository.findById(memberId);
    }

   
    public boolean updateProfile(Member updatedData) {
        try {
          
            Optional<Member> opt = memberRepository.findById(updatedData.getMemberid());
            if (opt.isEmpty()) return false;

            Member existing = opt.get();

            
            if (updatedData.getFirstname() != null && !updatedData.getFirstname().isBlank()) {
                existing.setFirstname(updatedData.getFirstname());
            }
            if (updatedData.getLastname() != null && !updatedData.getLastname().isBlank()) {
                existing.setLastname(updatedData.getLastname());
            }
            if (updatedData.getPhone() != null) {
                existing.setPhone(updatedData.getPhone());
            }
            if (updatedData.getBirthdate() != null) {
                existing.setBirthdate(updatedData.getBirthdate());
            }
            if (updatedData.getAddress() != null) {
                existing.setAddress(updatedData.getAddress());
            }

            // รหัสผ่าน: อัปเดตเฉพาะเมื่อมีการกรอกมาใหม่
            if (updatedData.getPassword() != null && !updatedData.getPassword().isBlank()) {
                existing.setPassword(updatedData.getPassword());
            }

            // บันทึกลงฐานข้อมูล
            memberRepository.save(existing);

            //  ปรับปรุง: copy ค่าวัดผลล่าสุดกลับไปให้ครบถ้วน รวมไปถึง Password ด้วย
            // เพื่อป้องกันไม่ให้ข้อมูลใน Session ของ Controller ขัดแย้งกับข้อมูลจริงใน DB
            updatedData.setFirstname(existing.getFirstname());
            updatedData.setLastname(existing.getLastname());
            updatedData.setEmail(existing.getEmail());
            updatedData.setPhone(existing.getPhone());
            updatedData.setBirthdate(existing.getBirthdate());
            updatedData.setAddress(existing.getAddress());
            updatedData.setPassword(existing.getPassword()); 

            return true;

        } catch (Exception e) {
           
            e.printStackTrace();
            return false;
        }
    }
}