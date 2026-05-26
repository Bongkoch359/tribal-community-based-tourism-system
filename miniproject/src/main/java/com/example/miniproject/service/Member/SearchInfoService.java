package com.example.miniproject.service.Member;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.miniproject.entity.Activitypost;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Tour;
import com.example.miniproject.repository.Member.ActivitypostRepository;
import com.example.miniproject.repository.Homestay.HomestayRepository;
import com.example.miniproject.repository.Member.TourRepository;

@Service
public class SearchInfoService {

    @Autowired
    private ActivitypostRepository activitypostRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private HomestayRepository homestayRepository;

    public List<Tour> getToursByManagerId(String managerId) {
    // สมมติว่าใน TourRepository ของพี่มีฟังก์ชันสำหรับหาด้วยไอดีผู้จัดการ
    // (ลองเช็คชื่อฟังก์ชันใน TourRepository อีกทีนะครับ ว่าเขียนไว้แบบไหน)
    return tourRepository.findByCommunitymanagerManagerid(managerId);
}


    // step 5.1-5.2: searchActivity()
    public List<Activitypost> searchActivity(String keyword) {
        try {
            if (keyword == null || keyword.isBlank()) {
                return activitypostRepository.findAll();
            }
            return activitypostRepository.findByTitleContainingIgnoreCase(keyword);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // step 6.1-6.2: searchTour()
    public List<Tour> searchTour(String keyword, Integer numGuest) {
        try {
            if (keyword == null || keyword.isBlank()) {
                return tourRepository.findAll();
            }
            return tourRepository.findByTourmnameContainingIgnoreCase(keyword);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // step 7.1-7.2: searchHomestay()
    public List<Homestay> searchHomestay(String keyword) {
        try {
            if (keyword == null || keyword.isBlank()) {
                return homestayRepository.findAll();
            }
            return homestayRepository.findByHomestaynameContainingIgnoreCase(keyword);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}