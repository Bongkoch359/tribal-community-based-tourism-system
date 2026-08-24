package com.example.miniproject.service.Homestay;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.miniproject.dto.Homestay.HomestayDetailDto;
import com.example.miniproject.dto.Homestay.HomestayDto;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Homestayowner;
import com.example.miniproject.entity.Review;
import com.example.miniproject.repository.Homestay.HomestayRepository;
import com.example.miniproject.repository.Member.ReviewRepository;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class HomestayService {

    @Autowired
    private HomestayRepository homestayRepository;

    @Autowired
    private ReviewRepository reviewRepository;
    

    public List<Homestay> getHomestaysByOwnerId(String ownerid) {
        return homestayRepository.findByOwner_Ownerid(ownerid);
    }

    public boolean isOwnedBy(Integer homestayid, String ownerid) {  
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
    public void updateHomestay(Integer homestayid, Map<String, String> req) {
    Homestay h = homestayRepository.findById(homestayid)
            .orElseThrow(() -> new RuntimeException("ไม่พบโฮมสเตย์"));

    String name = req.get("homestayname");
    String addr = req.get("address");
    String desc = req.get("description");
    String imgs = req.get("images");

    if (name != null && !name.isBlank())
        h.setHomestayname(name.trim());

    if (addr != null && !addr.isBlank())
        h.setAddress(addr.trim());

    h.setDescription(desc != null ? desc.trim() : "");

    if (imgs != null)
        h.setImages(imgs);

    homestayRepository.save(h);
}
    
@Transactional
public Homestay getHomestayDetailForMember(Integer homestayid) {
    Homestay h = homestayRepository.findById(homestayid).orElse(null);
    if (h == null) return null;
    h.getRoomtypes().forEach(r -> r.getFacilities().size());
    return h;
}

public List<Review> getReviewsByHomestay(Integer homestayid) {
    return reviewRepository.findByHomestayId(homestayid);
}

public Double getAvgRating(Integer homestayid) {
    return homestayRepository.avgRatingByHomestayId(homestayid);
}

public Long getReviewCount(Integer homestayid) {
    return homestayRepository.countReviewByHomestayId(homestayid);
}
public void updateImages(Integer homestayid, Map<String, String> req) {
    Homestay h = homestayRepository.findById(homestayid)
            .orElseThrow(() -> new RuntimeException("ไม่พบโฮมสเตย์"));
    h.setImages(req.get("images"));
    homestayRepository.save(h);
}

}