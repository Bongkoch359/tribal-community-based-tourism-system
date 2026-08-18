package com.example.miniproject.service.Homestay;

import com.example.miniproject.dto.Homestay.RegisterOwnerRequest;
import com.example.miniproject.dto.Homestay.UpdateProfileRequest;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Homestayowner;
import com.example.miniproject.repository.Homestay.HomestayOwnerRepository;
import com.example.miniproject.repository.Homestay.HomestayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class HomestayOwnerService {

    @Autowired
    private HomestayOwnerRepository ownerRepository;

    @Autowired
    private HomestayRepository homestayRepository;

    // ───── Register ─────

    // เช็คว่าอีเมลถูกใช้แล้วหรือยัง (สำหรับหน้าลงทะเบียน)
    public boolean isEmailTaken(String email) {
        return ownerRepository.existsByEmail(email);
    }

    @Transactional
    public List<Integer> register(RegisterOwnerRequest req) {

        if (ownerRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("อีเมลนี้ถูกใช้งานแล้ว");
        }

        Homestayowner owner = new Homestayowner();
        owner.setFirstname(req.getFirstname());
        owner.setLastname(req.getLastname());
        owner.setEmail(req.getEmail());
        owner.setPhone(req.getPhone());
        owner.setPassword(req.getPassword());
        owner.setVerificationstatus(false);
        owner.setAccountstatus("pending");

        Homestayowner saved = ownerRepository.save(owner);

        // เพิ่มตรงนี้
        List<Integer> homestayIds = new ArrayList<>();

        if (req.getHomestays() != null) {
            for (RegisterOwnerRequest.HomestayItem dto : req.getHomestays()) {

                Homestay h = new Homestay();
                h.setOwner(saved);
                h.setHomestayname(dto.getHomestayname());
                h.setDescription(dto.getDescription());
                h.setAddress(dto.getAddress());
                h.setStatus("pending");

                Homestay savedHomestay = homestayRepository.save(h);

                // เก็บ id
                homestayIds.add(savedHomestay.getHomestayid());
            }
        }

        return homestayIds;
    }

    // ───── Login ─────

    public Homestayowner login(String email, String rawPassword) {

        Optional<Homestayowner> opt = ownerRepository.findByEmail(email);

        if (opt.isEmpty()) {
            throw new IllegalArgumentException("ไม่พบบัญชีผู้ใช้นี้");
        }

        Homestayowner owner = opt.get();

        if (!owner.getPassword().equals(rawPassword)) {
            throw new IllegalArgumentException("รหัสผ่านไม่ถูกต้อง");
        }

        if (owner.getVerificationstatus() == null || !owner.getVerificationstatus()) {
            throw new IllegalArgumentException("บัญชียังไม่ได้รับการอนุมัติจาก Admin");
        }

        return owner;
    }

    // ───── Get Profile ─────

    public Homestayowner getProfile(int ownerid) {
        return ownerRepository.findById(ownerid)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบข้อมูลเจ้าของโฮมสเตย์"));
    }

    // ───── Update Profile ─────

    // ───── Update Profile (เฉพาะข้อมูลส่วนตัว) ─────

    @Transactional
    public Homestayowner updateProfile(int ownerid, String firstname, String lastname,
            String email, String phone) {

        Homestayowner owner = ownerRepository.findById(ownerid)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบข้อมูลเจ้าของโฮมสเตย์"));

        if (!owner.getEmail().equals(email) && ownerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("อีเมลนี้ถูกใช้งานแล้ว");
        }

        owner.setFirstname(firstname != null ? firstname.trim() : null);
        owner.setLastname(lastname != null ? lastname.trim() : null);
        owner.setEmail(email != null ? email.trim() : null);
        owner.setPhone(phone != null ? phone.trim() : null);

        return ownerRepository.save(owner);
    }

    // ───── Update Bank Info ─────

    @Transactional
    public Homestayowner updateBankInfo(int ownerid, String bankName, String accountName,
            String accountNumber, String bankBranch) {

        Homestayowner owner = ownerRepository.findById(ownerid)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบข้อมูลเจ้าของโฮมสเตย์"));

        owner.setBankName(bankName != null ? bankName.trim() : null);
        owner.setAccountName(accountName != null ? accountName.trim() : null);
        owner.setAccountNumber(accountNumber != null ? accountNumber.trim() : null);
        owner.setBankBranch(bankBranch != null ? bankBranch.trim() : null);

        return ownerRepository.save(owner);
    }

    // ───── Update Signature ─────

    @Transactional
    public Homestayowner updateSignature(int ownerid, String signatureBase64) {
        Homestayowner owner = ownerRepository.findById(ownerid)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบข้อมูลเจ้าของโฮมสเตย์"));
        owner.setSignatureImageUrl(signatureBase64);
        return ownerRepository.save(owner);
    }

    // ───── Change Password ─────

    @Transactional
    public void changePassword(int ownerid, String currentPassword, String newPassword) {

        // 1. ดึง owner
        Homestayowner owner = ownerRepository.findById(ownerid)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบข้อมูลเจ้าของโฮมสเตย์"));

        // 2. ตรวจสอบรหัสผ่านปัจจุบัน
        if (!owner.getPassword().equals(currentPassword)) {
            throw new IllegalArgumentException("รหัสผ่านปัจจุบันไม่ถูกต้อง");
        }

        // 3. ตรวจสอบความยาวรหัสผ่านใหม่
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("รหัสผ่านใหม่ต้องมีอย่างน้อย 6 ตัวอักษร");
        }

        // 4. บันทึกรหัสผ่านใหม่
        owner.setPassword(newPassword);
        ownerRepository.save(owner);
    }

}