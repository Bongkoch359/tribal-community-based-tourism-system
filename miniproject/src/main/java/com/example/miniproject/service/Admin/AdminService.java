package com.example.miniproject.service.Admin;


import java.util.Optional;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import com.example.miniproject.entity.Admin;
import com.example.miniproject.repository.Member.AdminRepository;
 
@Service
public class AdminService {
 
    @Autowired
    private AdminRepository adminRepository;
 
    // step 5.1: ค้นหาข้อมูลจากฐานข้อมูล
    // step 5.2: คืนค่า T (Admin) หรือ F (empty)
    public Optional<Admin> login(String username, String password) {
        return adminRepository.findByUsernameAndPassword(username, password);
    }
}
 