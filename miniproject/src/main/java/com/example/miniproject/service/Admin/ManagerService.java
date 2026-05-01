package com.example.miniproject.service.Admin;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.enums.ManagerStatus;
import com.example.miniproject.repository.Admin.CommunitymanagerRepository;

@Service
public class ManagerService {

    @Autowired
    private CommunitymanagerRepository managerRepository;

    // List Manager Account — step 4.1-4.2
    public List<Communitymanager> getAll() {
        return managerRepository.findAll();
    }

    public List<Communitymanager> getByStatus(ManagerStatus status) {
        return managerRepository.findByAccountstatus(status);
    }

    // Create Manager Account — step 5.1
    public boolean createManager(Communitymanager manager) {
        try {
            manager.setAccountstatus(ManagerStatus.ACTIVE);
            managerRepository.save(manager);
            return true;
        } catch (Exception e) {
             e.printStackTrace();  
            return false;
        }
    }

    // Suspend — เปลี่ยนสถานะเป็น INACTIVE
    public boolean suspend(String managerid) {
        Optional<Communitymanager> opt = managerRepository.findById(managerid);
        if (opt.isPresent()) {
            opt.get().setAccountstatus(ManagerStatus.INACTIVE);
            managerRepository.save(opt.get());
            return true;
        }
        return false;
    }

    // Activate — เปลี่ยนสถานะกลับเป็น ACTIVE
    public boolean activate(String managerid) {
        Optional<Communitymanager> opt = managerRepository.findById(managerid);
        if (opt.isPresent()) {
            opt.get().setAccountstatus(ManagerStatus.ACTIVE);
            managerRepository.save(opt.get());
            return true;
        }
        return false;
    }

    // ตรวจ email ซ้ำ
    public boolean emailExists(String email) {
        return managerRepository.existsByEmail(email);
    }
}