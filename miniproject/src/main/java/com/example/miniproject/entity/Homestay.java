package com.example.miniproject.entity;

import com.example.miniproject.entity.enums.HomestayStatus;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "Homestay")
public class Homestay {

    @Id
    @Column(name = "homestayid", length = 10)
    private String homestayid;

    @Column(length = 100)
    private String homestayname;

    @Column(length = 255)
    private String address;

    @Column(length = 255)
    private String description;

    @Column(length = 50)
    private String images;

    @Enumerated(EnumType.STRING)
    private HomestayStatus status;

    @ManyToOne
    @JoinColumn(name = "ownerid")
    private Homestayowner owner;

    @OneToMany(mappedBy = "homestay")
    private List<Roomtype> roomtypes;

    public Homestay() {}

    public String getHomestayid() {
        return homestayid;
    }

    public void setHomestayid(String homestayid) {
        this.homestayid = homestayid;
    }

    public String getHomestayname() {
        return homestayname;
    }

    public void setHomestayname(String homestayname) {
        this.homestayname = homestayname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public HomestayStatus getStatus() {
        return status;
    }

    public void setStatus(HomestayStatus status) {
        this.status = status;
    }

    public Homestayowner getOwner() {
        return owner;
    }

    public void setOwner(Homestayowner owner) {
        this.owner = owner;
    }

    public List<Roomtype> getRoomtypes() {
        return roomtypes;
    }

    public void setRoomtypes(List<Roomtype> roomtypes) {
        this.roomtypes = roomtypes;
    }
}