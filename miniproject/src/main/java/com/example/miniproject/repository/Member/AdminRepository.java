package com.example.miniproject.repository.Member;


import com.example.miniproject.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.Optional;
 
public interface AdminRepository extends JpaRepository<Admin, Integer> {
 
    Optional<Admin> findByUsernameAndPassword(String username, String password);
 
    Optional<Admin> findByUsername(String username);
 
    boolean existsByUsername(String username);
}
 