package com.example.miniproject;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import com.example.miniproject.entity.Admin;
import com.example.miniproject.entity.Facilities;
import com.example.miniproject.repository.Member.AdminRepository;
import com.example.miniproject.repository.Homestay.FacilitiesRepository;

public class DataInitializer {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(MiniprojectApplication.class, args);

        AdminRepository adminRepository = context.getBean(AdminRepository.class);
        FacilitiesRepository facilitiesRepository = context.getBean(FacilitiesRepository.class);

        // ------------------ Admin ------------------
        Admin admin = new Admin();
        admin.setAdminId(1);
        admin.setUsername("admin");
        admin.setPassword("1234");

        adminRepository.save(admin);

        // ------------------ Facilities ------------------
        Facilities f1 = new Facilities();
        f1.setFacilitiesid("f001");
        f1.setFacilitiesname("Wi-Fi");

        Facilities f2 = new Facilities();
        f2.setFacilitiesid("f002");
        f2.setFacilitiesname("น้ำอุ่น");

        Facilities f3 = new Facilities();
        f3.setFacilitiesid("f003");
        f3.setFacilitiesname("พัดลม");

        Facilities f4 = new Facilities();
        f4.setFacilitiesid("f004");
        f4.setFacilitiesname("อาหารเช้า");

        Facilities f5 = new Facilities();
        f5.setFacilitiesid("f005");
        f5.setFacilitiesname("ชุดชนเผ่าสำหรับถ่ายรูป");

        facilitiesRepository.save(f1);
        facilitiesRepository.save(f2);
        facilitiesRepository.save(f3);
        facilitiesRepository.save(f4);
        facilitiesRepository.save(f5);

        System.out.println("บันทึกข้อมูล Admin และ Facilities เรียบร้อย");
    }
}