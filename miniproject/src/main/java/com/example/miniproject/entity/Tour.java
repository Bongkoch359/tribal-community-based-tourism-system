package com.example.miniproject.entity;

import com.example.miniproject.entity.enums.TourStatus;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Tour")
public class Tour {

    @Id
    @Column(name = "tourid", length = 10)
    private String tourid;

    @Column(length = 100)
    private String tourname;

    @Enumerated(EnumType.STRING)
    private TourStatus status;

    @Column(length = 255)
    private String tourdetail;

    @Column(length = 255)
    private String conditiontour;

    private Integer minSeatstour;
    private Integer maxSeatstour;
    private Double adultprice;
    private Double childprice;

    @ElementCollection
    private List<String> images = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "managerid")
    private Communitymanager communitymanager;

    @OneToMany(mappedBy = "tour")
    private List<Bookingtourdetail> bookingTourDetails = new ArrayList<>();

    public Tour() {}

    public String getTourid() { return tourid; }
    public void setTourid(String tourid) { this.tourid = tourid; }

    public String getTourmname() { return tourname; }
    public void setTourmname(String tourmname) { this.tourname = tourmname; }

    public TourStatus getStatus() { return status; }
    public void setStatus(TourStatus status) { this.status = status; }

    public String getTourdetail() { return tourdetail; }
    public void setTourdetail(String tourdetail) { this.tourdetail = tourdetail; }

    public String getConditiontour() { return conditiontour; }
    public void setConditiontour(String conditiontour) { this.conditiontour = conditiontour; }

    public Integer getMinSeatstour() { return minSeatstour; }
    public void setMinSeatstour(Integer minSeatstour) { this.minSeatstour = minSeatstour; }

    public Integer getMaxSeatstour() { return maxSeatstour; }
    public void setMaxSeatstour(Integer maxSeatstour) { this.maxSeatstour = maxSeatstour; }

    public Double getAdultprice() { return adultprice; }
    public void setAdultprice(Double adultprice) { this.adultprice = adultprice; }

    public Double getChildprice() { return childprice; }
    public void setChildprice(Double childprice) { this.childprice = childprice; }
    public List<String> getImages() {
    return images;
}

    public void setImages(List<String> images) {
    this.images = images;
}
    public Communitymanager getCommunitymanager() { return communitymanager; }
    public void setCommunitymanager(Communitymanager communitymanager) { this.communitymanager = communitymanager; }

    public List<Bookingtourdetail> getBookingTourDetails() { return bookingTourDetails; }
    public void setBookingTourDetails(List<Bookingtourdetail> bookingTourDetails) { this.bookingTourDetails = bookingTourDetails; }
}