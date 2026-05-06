package com.example.miniproject.repository.Member;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.miniproject.entity.Activitypost;
import com.example.miniproject.entity.enums.ActivityStatus;

public interface ActivitypostRepository extends JpaRepository<Activitypost, String> {
    //ค้นหาจากชื่อกิจกรรม
    List<Activitypost> findByTitleContainingIgnoreCase(String title);
    //ค้นหาจากสถานที่
    List<Activitypost> findByLocationContainingIgnoreCase(String location);
    //ดึง 3 รายการล่าสุด
    List<Activitypost> findTop3ByOrderByCreateddateDesc();

    // ดึงทั้งหมดเรียงจากใหม่ไปเก่า
    List<Activitypost> findAllByOrderByCreateddateDesc();
 
    // ดึงตามสถานะ
    List<Activitypost> findByStatusOrderByCreateddateDesc(ActivityStatus status);
 
    // ดึงของ manager คนใดคนหนึ่ง
    List<Activitypost> findByCommunitymanagerManageridOrderByCreateddateDesc(String managerid);
}