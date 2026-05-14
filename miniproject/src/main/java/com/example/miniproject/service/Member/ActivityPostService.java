package com.example.miniproject.service.Member;

import com.example.miniproject.entity.Activitypost;
import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.enums.ActivityStatus;
import com.example.miniproject.repository.Member.ActivitypostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ActivityPostService {
 
    @Autowired
    private ActivitypostRepository activityPostRepository;
 
    // ─── ดึงโพสต์ทั้งหมด ───
    public List<Activitypost> getAllPosts() {
        return activityPostRepository.findAllByOrderByCreateddateDesc();
    }
 
    // ─── ดึงโพสต์ตาม ID ───
    public Activitypost getPostById(String activityId) {
        return activityPostRepository.findById(activityId).orElse(null);
    }
 
    
    // ─── สร้างโพสต์ใหม่ ───
    public Activitypost createPost(
            String title,
            String location,
            String description,
            String status,
            String images,         // base64 หลายรูป คั่นด้วย ||
            Communitymanager manager) {
 
        Activitypost post = new Activitypost();
 
        // สร้าง ID อัตโนมัติ (ACT + 7 ตัวอักษร)
        String id = "ACT" + UUID.randomUUID().toString().replace("-", "").substring(0, 7).toUpperCase();
        post.setActivityid(id);
 
        post.setTitle(title);
        post.setLocation(location);
        post.setDescription(description);
        post.setCreateddate(new Date());
        post.setCommunitymanager(manager);
 
        // แปลง status string → enum
        try {
            post.setStatus(ActivityStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            post.setStatus(ActivityStatus.DRAFT);
        }
 
        // เก็บ base64 รูปภาพ (หลายรูปคั่นด้วย ||)
        post.setImages(images != null && !images.isBlank() ? images : null);
 
        return activityPostRepository.save(post);
    }
}
 
