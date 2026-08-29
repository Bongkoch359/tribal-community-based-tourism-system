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

    @Autowired
    private ReportService reportService; // เชื่อมต่อเพื่อเคลียร์รายงานอัตโนมัติ

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

    // Suspend — เปลี่ยนสถานะเป็น INACTIVE พร้อมบันทึกเหตุผลและเคลียร์รายงานที่ค้างอยู่
    public boolean suspend(String managerid, String reason) {
        Optional<Communitymanager> opt = managerRepository.findById(managerid);
        if (opt.isPresent()) {
            Communitymanager manager = opt.get();
            manager.setAccountstatus(ManagerStatus.INACTIVE); 
            manager.setSuspensionReason(reason); 
            managerRepository.save(manager);

            // เคลียร์/อัปเดตสถานะรายงานที่เกี่ยวข้องกับผู้จัดการคนนี้ให้เป็น RESOLVED อัตโนมัติ
            try {
                reportService.resolveReportsForManager(managerid);
            } catch (Exception e) {
                System.err.println("ไม่สามารถอัปเดตสถานะรายงานได้: " + e.getMessage());
            }

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