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
 
    public Optional<Admin> login(String username, String password) {
        return adminRepository.findByUsernameAndPassword(username, password);
    }
}
 