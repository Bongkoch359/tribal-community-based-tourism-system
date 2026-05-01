package com.example.miniproject.entity;

import com.example.miniproject.entity.enums.RoomtypeStatus;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Roomtype")
public class Roomtype {

    @Id
    @Column(name = "roomtypeid")
    private String roomtypeid;

    @Column(length = 100)
    private String typename;

    @Column(length = 255)
    private String description;

    private Integer maxguest;

    private Double pricepernight;

    @Column(length = 255)
    private String roomcondition;

    private Integer totalrooms;

    @Enumerated(EnumType.STRING)
    private RoomtypeStatus status;

    @ManyToOne
    @JoinColumn(name = "homestayid")
    private Homestay homestay;

    @ManyToMany
    @JoinTable(
        name = "roomtypefacilities",
        joinColumns = @JoinColumn(name = "roomTypeid"),
        inverseJoinColumns = @JoinColumn(name = "facilitiesid")
    )
    private List<Facilities> facilities;

    @OneToMany(mappedBy = "roomtype")
    private List<Bookingroomdetail> bookingRoomDetails = new ArrayList<>();

    public Roomtype() {}

    public String getRoomtypeid() { return roomtypeid; }
    public void setRoomtypeid(String roomtypeid) { this.roomtypeid = roomtypeid; }

    public String getTypename() { return typename; }
    public void setTypename(String typename) { this.typename = typename; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getMaxguest() { return maxguest; }
    public void setMaxguest(Integer maxguest) { this.maxguest = maxguest; }

    public Double getPricepernight() { return pricepernight; }
    public void setPricepernight(Double pricepernight) { this.pricepernight = pricepernight; }

    public String getRoomcondition() { return roomcondition; }
    public void setRoomcondition(String roomcondition) { this.roomcondition = roomcondition; }

    public Integer getTotalrooms() { return totalrooms; }
    public void setTotalrooms(Integer totalrooms) { this.totalrooms = totalrooms; }

    public RoomtypeStatus getStatus() { return status; }
    public void setStatus(RoomtypeStatus status) { this.status = status; }

    public Homestay getHomestay() { return homestay; }
    public void setHomestay(Homestay homestay) { this.homestay = homestay; }

    public List<Facilities> getFacilities() { return facilities; }
    public void setFacilities(List<Facilities> facilities) { this.facilities = facilities; }

    public List<Bookingroomdetail> getBookingRoomDetails() { return bookingRoomDetails; }
    public void setBookingRoomDetails(List<Bookingroomdetail> bookingRoomDetails) { this.bookingRoomDetails = bookingRoomDetails; }
}