package com.example.miniproject;


import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import com.example.miniproject.entity.Admin;
import com.example.miniproject.repository.Member.AdminRepository;

public class DataInitializer {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(MiniprojectApplication.class, args);

        AdminRepository adminRepository = context.getBean(AdminRepository.class);

        // --- Admin ---
        Admin admin = new Admin();
        admin.setAdminId(1);
        admin.setUsername("admin");
        admin.setPassword("1234");

        adminRepository.save(admin);

        System.out.println("บันทึกข้อมูล Admin เรียบร้อย");
    }
}