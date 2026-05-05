package com.example.miniproject.service.Member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.miniproject.entity.Roomtype;
import com.example.miniproject.repository.Homestay.RoomTypeRepository;

@Service
public class BookingHomestayService {

    @Autowired
    private RoomTypeRepository roomtypeRepository;

    public Roomtype getRoomById(String roomId) {

        return roomtypeRepository.findById(roomId).orElse(null);

    }

}