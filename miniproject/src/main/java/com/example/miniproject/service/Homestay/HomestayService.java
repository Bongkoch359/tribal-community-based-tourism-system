package com.example.miniproject.service.Homestay;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.miniproject.dto.Homestay.HomestayDetailDto;
import com.example.miniproject.dto.Homestay.HomestayDto;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Homestayowner;
import com.example.miniproject.repository.Homestay.HomestayRepository;
import java.util.List;

@Service
public class HomestayService {

    @Autowired
    private HomestayRepository homestayRepository;
    

    public boolean isOwnedBy(Integer homestayid, Integer ownerid) {  
        return homestayRepository.existsByHomestayidAndOwnerOwnerid(homestayid, ownerid);
    }

    public Homestay getHomestayById(Integer homestayid) {  
        return homestayRepository.findById(homestayid).orElse(null);
    }

    public List<HomestayDto> getHomestaysByOwner(Homestayowner owner) {
    List<Homestay> list = homestayRepository.findByOwner(owner);
    List<HomestayDto> result = new java.util.ArrayList<>();

    for (Homestay h : list) {
        Double avg   = homestayRepository.avgRatingByHomestayId(h.getHomestayid());
        Long   count = homestayRepository.countReviewByHomestayId(h.getHomestayid());
        result.add(new HomestayDto(h, avg, count));
    }
    return result;
}
    public HomestayDetailDto getHomestayDetail(Integer homestayid) {
    Homestay h = homestayRepository.findById(homestayid).orElse(null);
    if (h == null) return null;

    Double avg      = homestayRepository.avgRatingByHomestayId(homestayid);
    Long   reviews  = homestayRepository.countReviewByHomestayId(homestayid);
    Long   rooms    = homestayRepository.countRoomTypeByHomestayId(homestayid);
    Long   bookings = homestayRepository.countPendingBookingByHomestayId(homestayid);

    return new HomestayDetailDto(h, avg, reviews, rooms, bookings);
}
}