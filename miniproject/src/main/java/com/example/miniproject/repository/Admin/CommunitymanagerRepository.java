package com.example.miniproject.repository.Admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.enums.ManagerStatus;

public interface CommunitymanagerRepository extends JpaRepository<Communitymanager, String> {

   
    List<Communitymanager> findAll();

    // กรองตามสถานะ
    List<Communitymanager> findByAccountstatus(ManagerStatus status);

    // ตรวจ email ซ้ำ
    boolean existsByEmail(String email);

    Optional<Communitymanager> findByEmail(String email);
}