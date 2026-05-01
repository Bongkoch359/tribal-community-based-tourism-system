package com.example.miniproject.service.Homestay;

import com.example.miniproject.dto.Homestay.AddRoomRequest;
import com.example.miniproject.entity.Facilities;
import com.example.miniproject.entity.Homestay;
import com.example.miniproject.entity.Roomtype;
import com.example.miniproject.repository.Homestay.FacilitiesRepository;
import com.example.miniproject.repository.Homestay.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class RoomTypeService {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private FacilitiesRepository facilitiesRepository; // ✅ เพิ่ม

  

    // ─── Add ───
    public Roomtype addRoomType(AddRoomRequest req) {

        String id = "RT" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8).toUpperCase();

        Roomtype room = new Roomtype();
        room.setRoomtypeid(id);
        room.setTypename(req.getTypename());
        room.setBedtype(req.getBedtype());
        room.setPricepernight(req.getPricepernight());
        room.setMaxguest(req.getMaxguest());
        room.setTotalrooms(req.getTotalrooms());
        room.setDescription(req.getDescription());
        room.setRoomcondition(req.getRoomcondition());
        room.setStatus(req.getStatus());

        // ผูก Homestay
        Homestay homestay = new Homestay();
        homestay.setHomestayid(req.getHomestayid());
        room.setHomestay(homestay);

        // ✅ เก็บ Base64 คั่นด้วย ","
        if (req.getImages() != null && !req.getImages().isEmpty()) {
            room.setImages(String.join(",", req.getImages()));
        }

        // ✅ ผูก Facilities
        if (req.getFacilitiesIds() != null && !req.getFacilitiesIds().isEmpty()) {
            List<Facilities> facilitiesList = facilitiesRepository.findAllById(req.getFacilitiesIds());
            room.setFacilities(facilitiesList);
        }

        return roomTypeRepository.save(room);
    }

    // ─── Read ───
    public List<Roomtype> getRoomTypesByHomestayId(Integer homestayid) {
        return roomTypeRepository.findByHomestayId(homestayid);
    }

      // ─── Read ───
    @Transactional  // ✅ เพิ่มตรงนี้
    public Roomtype getRoomTypeById(String roomtypeid) {
    Roomtype room = roomTypeRepository.findById(roomtypeid).orElse(null);
    if (room != null && room.getFacilities() != null) {
        room.getFacilities().size(); // ✅ force load facilities
    }
    return room;
}

    // ─── Update ───
    public Roomtype updateRoomType(Roomtype roomType) {
        return roomTypeRepository.save(roomType);
    }

    // ─── Delete ───
    // public void deleteRoomType(String roomtypeid) {
    //     roomTypeRepository.deleteById(roomtypeid);
    // }
}