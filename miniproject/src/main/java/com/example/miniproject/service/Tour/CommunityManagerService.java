package com.example.miniproject.service.Tour;

import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.repository.Tour.ManagerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommunityManagerService {

    @Autowired
    private ManagerRepository repository;

    public Communitymanager login(String email, String password) {
        // คืนค่าข้อมูลผู้จัดการหากพบในฐานข้อมูล (ขั้นตอน 5.2 ใน Diagram)
        return repository.findByEmailAndPassword(email, password);
    }
}