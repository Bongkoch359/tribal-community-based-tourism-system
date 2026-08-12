package com.example.miniproject.service.Tour;

import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.repository.Tour.ManagerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommunityManagerService {

    @Autowired
    private ManagerRepository managerRepository;

    public Communitymanager login(String email, String password) {
        return managerRepository.findByEmailAndPassword(email, password);
    }

    // ─── ดึงข้อมูล Manager ตาม ID ───
    public Communitymanager getById(String managerId) {
        return managerRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบผู้จัดการ ID: " + managerId));
    }

    // ─── อัปเดตข้อมูลส่วนตัว (ไม่เปลี่ยนรหัสผ่าน) ───
    @Transactional
    public void updateProfile(String managerId, String firstname, String lastname,
                              String email, String phone) {
        Communitymanager manager = getById(managerId);
        manager.setFirstname(firstname);
        manager.setLastname(lastname);
        manager.setEmail(email);
        manager.setPhone(phone);
        managerRepository.save(manager);
    }

    // ─── อัปเดตข้อมูลธนาคาร ───
    @Transactional
    public void updateBankInfo(String managerId, String bankName, String accountName,
                               String accountNumber, String bankBranch) {
        Communitymanager manager = getById(managerId);
        manager.setBankName(bankName);
        manager.setAccountName(accountName);
        manager.setAccountNumber(accountNumber);
        manager.setBankBranch(bankBranch);
        managerRepository.save(manager);
    }

    // ─── อัปเดตข้อมูลส่วนตัว พร้อมเปลี่ยนรหัสผ่าน ───
    @Transactional
    public void updateProfileWithPassword(String managerId, String firstname, String lastname,
                                          String email, String phone,
                                          String currentPassword, String newPassword) {
        Communitymanager manager = getById(managerId);

        if (!manager.getPassword().equals(currentPassword)) {
            throw new IllegalArgumentException("รหัสผ่านปัจจุบันไม่ถูกต้อง");
        }

        manager.setFirstname(firstname);
        manager.setLastname(lastname);
        manager.setEmail(email);
        manager.setPhone(phone);
        manager.setPassword(newPassword);
        managerRepository.save(manager);
    }

    // ─── อัปเดตลายเซ็น ───
@Transactional
public void updateSignature(String managerId, String signatureBase64) {
    Communitymanager manager = getById(managerId);
    manager.setSignatureImageUrl(signatureBase64);
    managerRepository.save(manager);
}
}