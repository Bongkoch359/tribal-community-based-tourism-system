package com.example.miniproject.service.Member;

import com.example.miniproject.entity.Activitypost;
import com.example.miniproject.entity.Communitymanager;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.repository.Member.ActivitypostRepository;
import com.example.miniproject.repository.Member.TourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ActivityPostService {

    @Autowired
    private ActivitypostRepository activityPostRepository;

    @Autowired
    private TourRepository tourRepository;

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
            String images,         // base64 หลายรูป คั่นด้วย ||
            String tourId,         // ไม่บังคับ — ทัวร์ที่โพสนี้โปรโมท
            Communitymanager manager) {

        Activitypost post = new Activitypost();

        String id = "ACT" + UUID.randomUUID().toString().replace("-", "").substring(0, 7).toUpperCase();
        post.setActivityid(id);

        post.setTitle(title);
        post.setLocation(location);
        post.setDescription(description);
        post.setCreateddate(new Date());
        post.setCommunitymanager(manager);

        post.setImages(images != null && !images.isBlank() ? images : null);

        post.setTour(resolveTour(tourId));

        return activityPostRepository.save(post);
    }

    // ─── หาทัวร์จาก tourId (คืน null ถ้าไม่ได้เลือก) ───
    private Tour resolveTour(String tourId) {
        if (tourId == null || tourId.isBlank()) {
            return null;
        }
        return tourRepository.findById(tourId)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบทัวร์ที่เลือก (ID: " + tourId + ")"));
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
            String images,
            String tourId) {

        Activitypost post = activityPostRepository.findById(activityId).orElse(null);
        if (post == null) return null;

        post.setTitle(title);
        post.setLocation(location);
        post.setDescription(description);

        // อัปเดตรูปเฉพาะเมื่อมีรูปใหม่ส่งมา
        // ถ้า images เป็น null หรือว่าง → คงรูปเดิมไว้ (ไม่ overwrite)
        if (images != null && !images.isBlank()) {
            post.setImages(images);
        }

        // อัปเดตทัวร์ที่เชื่อมโยง — ถ้าไม่ได้เลือกทัวร์ ให้ตัดการเชื่อมโยงออก (setTour(null))
        post.setTour(resolveTour(tourId));

        return activityPostRepository.save(post);
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