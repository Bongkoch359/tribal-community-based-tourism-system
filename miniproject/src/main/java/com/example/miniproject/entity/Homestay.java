package com.example.miniproject.entity;

// import com.example.miniproject.entity.enums.HomestayStatus;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "Homestay")
public class Homestay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "homestayid", length = 10)
    private int homestayid;

    @Column(length = 100)
    private String homestayname;

    @Column(length = 255)
    private String address;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String images;

    // @Enumerated(EnumType.STRING)
    // private HomestayStatus status;
    @Column(length = 50)
    private String status;

    @ManyToOne
    @JoinColumn(name = "ownerid")
    private Homestayowner owner;

    @OneToMany(mappedBy = "homestay")
    private List<Roomtype> roomtypes;

    public Homestay() {}

    public int getHomestayid() {
        return homestayid;
    }

    public void setHomestayid(int homestayid) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}