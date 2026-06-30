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

        String id = "ACT" + UUID.randomUUID().toString().replace("-", "").substring(0, 7).toUpperCase();
        post.setActivityid(id);

        post.setTitle(title);
        post.setLocation(location);
        post.setDescription(description);
        post.setCreateddate(new Date());
        post.setCommunitymanager(manager);

        try {
            post.setStatus(ActivityStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            post.setStatus(ActivityStatus.DRAFT);
        }

        post.setImages(images != null && !images.isBlank() ? images : null);

        return activityPostRepository.save(post);
    }

    // ─── ลบโพสต์ ───
    public boolean deletePost(String activityId) {
        if (activityPostRepository.existsById(activityId)) {
            activityPostRepository.deleteById(activityId);
            return true;
        }
        return false;
    }

    // ─── อัปเดตโพสต์ ───
    public Activitypost updatePost(
            String activityId,
            String title,
            String location,
            String description,
            String status,
            String images) {

        Activitypost post = activityPostRepository.findById(activityId).orElse(null);
        if (post == null) return null;

        post.setTitle(title);
        post.setLocation(location);
        post.setDescription(description);

        try {
            post.setStatus(ActivityStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            post.setStatus(ActivityStatus.DRAFT);
        }

        // อัปเดตรูปเฉพาะเมื่อมีรูปใหม่ส่งมา
        // ถ้า images เป็น null หรือว่าง → คงรูปเดิมไว้ (ไม่ overwrite)
        if (images != null && !images.isBlank()) {
            post.setImages(images);
        }

        return activityPostRepository.save(post);
    }

    // ─── ดึงโพสต์ตามสถานะ ───
    public List<Activitypost> getPostsByStatus(ActivityStatus status) {
        return activityPostRepository.findByStatusOrderByCreateddateDesc(status);
    }

    // ─── ดึงโพสต์ของ manager คนนั้น ───
    public List<Activitypost> getPostsByManager(String managerId) {
        return activityPostRepository.findByCommunitymanagerManageridOrderByCreateddateDesc(managerId);
    }

    // ─── ค้นหาตามหัวข้อ ───
    public List<Activitypost> searchByTitle(String keyword) {
        return activityPostRepository.findByTitleContainingIgnoreCase(keyword);
    }

    // ─── ค้นหาตามสถานที่ ───
    public List<Activitypost> searchByLocation(String keyword) {
        return activityPostRepository.findByLocationContainingIgnoreCase(keyword);
    }

    // ─── ดึง 3 โพสต์ล่าสุด ───
    public List<Activitypost> getLatestPosts() {
        return activityPostRepository.findTop3ByOrderByCreateddateDesc();
    }
}