package com.example.miniproject.entity;

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
    private String tourmname;

    // @Column(length = 50)
    // private String status;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String tourdetail;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String conditiontour;

    private Integer minSeatstour;
    private Integer maxSeatstour;
    private Double adultprice;
    private Double childprice;
    @Column(name = "insurance_price")
    private Double insurancePrice;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String images;

    // ===================== เพิ่มใหม่: ประเภททัวร์ / จำนวนวัน / จำนวนคืน
    // =====================
    // เดิม tourtype เป็น String ตรงนี้ ตอนนี้แยกออกไปเป็นคลาส TourType
    // แล้วผูกด้วย @ManyToOne (หลายทัวร์ ใช้ประเภทเดียวกันได้)
    @ManyToOne
    @JoinColumn(name = "typeId")
    private TourType tourtype;

    @Column(name = "number_of_days")
    private Integer numberOfDays; // จำนวนวัน เช่น 1, 3, 5

    @Column(name = "number_of_nights")
    private Integer numberOfNights; // จำนวนคืน เช่น 0, 2, 4
    // =======================================================================================

    @ManyToOne
    @JoinColumn(name = "managerid")
    private Communitymanager communitymanager;

    @OneToMany(mappedBy = "tour")
    private List<Bookingtourdetail> bookingTourDetails = new ArrayList<>();

    // 1 ทัวร์ ถูกโปรโมทได้หลายโพสกิจกรรม
    @OneToMany(mappedBy = "tour")
    private List<Activitypost> activityPosts = new ArrayList<>();

    // ✅ ใหม่: 1 ทัวร์ เปิดได้หลายรอบ/หลายวันที่ (Tourschedule)
    @OneToMany(mappedBy = "tour")
    private List<Tourschedule> tourSchedules = new ArrayList<>();

    @Transient
    private String overallStatus;

    public String getOverallStatus() {
        return overallStatus;
    }
    // "สถานะสรุป" ของทัวร์ที่คำนวณสดจากรอบทัวร์ทั้งหมด ใช้แค่โชว์บนหน้าเว็บ
    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public Tour() {
    }

    public String getTourid() {
        return tourid;
    }

    public void setTourid(String tourid) {
        this.tourid = tourid;
    }

    public String getTourmname() {
        return tourmname;
    }

    public void setTourmname(String tourmname) {
        this.tourmname = tourmname;
    }

    public String getTourdetail() {
        return tourdetail;
    }

    public void setTourdetail(String tourdetail) {
        this.tourdetail = tourdetail;
    }

    public String getConditiontour() {
        return conditiontour;
    }

    public void setConditiontour(String conditiontour) {
        this.conditiontour = conditiontour;
    }

    public Integer getMinSeatstour() {
        return minSeatstour;
    }

    public void setMinSeatstour(Integer minSeatstour) {
        this.minSeatstour = minSeatstour;
    }

    public Integer getMaxSeatstour() {
        return maxSeatstour;
    }

    public void setMaxSeatstour(Integer maxSeatstour) {
        this.maxSeatstour = maxSeatstour;
    }

    public Double getAdultprice() {
        return adultprice;
    }

    public void setAdultprice(Double adultprice) {
        this.adultprice = adultprice;
    }

    public Double getChildprice() {
        return childprice;
    }

    public void setChildprice(Double childprice) {
        this.childprice = childprice;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Communitymanager getCommunitymanager() {
        return communitymanager;
    }

    public void setCommunitymanager(Communitymanager communitymanager) {
        this.communitymanager = communitymanager;
    }

    public List<Bookingtourdetail> getBookingTourDetails() {
        return bookingTourDetails;
    }

    public void setBookingTourDetails(List<Bookingtourdetail> bookingTourDetails) {
        this.bookingTourDetails = bookingTourDetails;
    }

    public TourType getTourtype() {
        return tourtype;
    }

    public void setTourtype(TourType tourtype) {
        this.tourtype = tourtype;
    }

    public Integer getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(Integer numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public Integer getNumberOfNights() {
        return numberOfNights;
    }

    public void setNumberOfNights(Integer numberOfNights) {
        this.numberOfNights = numberOfNights;
    }

    public List<Activitypost> getActivityPosts() {
        return activityPosts;
    }

    public void setActivityPosts(List<Activitypost> activityPosts) {
        this.activityPosts = activityPosts;
    }

    public List<Tourschedule> getTourSchedules() {
        return tourSchedules;
    }

    public void setTourSchedules(List<Tourschedule> tourSchedules) {
        this.tourSchedules = tourSchedules;
    }

    @PrePersist
    @PreUpdate
    public void updateTourtype() {
        if (numberOfDays != null && numberOfDays == 1 && numberOfNights == null) {
            this.numberOfNights = 0; // ทัวร์รายวัน = 0 คืน
        }
    }

    /** แสดงผลรูปแบบ "x วัน y คืน" หรือ "1 วัน" ถ้าเป็นทัวร์รายวัน */
    public String getTourDuration() {
        if (numberOfDays == null)
            return null;
        if (numberOfNights == null || numberOfNights == 0) {
            return numberOfDays + " วัน";
        }
        return numberOfDays + " วัน " + numberOfNights + " คืน";
    }

    // =======================================================================================
    @Transient
    private int bookedSeats = 0; // ไม่ map ลง DB

    public int getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    @Transient
    public int getRemainingSeats() {
        if (maxSeatstour == null)
            return 0;
        return Math.max(0, maxSeatstour - bookedSeats);
    }

    @Transient
    public boolean isFull() {
        return getRemainingSeats() == 0;
    }

    @Transient
    public Double getStartingPrice() {
        if (adultprice == null && childprice == null) {
            return 0.0;
        }
        if (adultprice == null) {
            return childprice;
        }
        if (childprice == null) {
            return adultprice;
        }
        return Math.min(adultprice, childprice);
    }

    public Double getInsurancePrice() {
        return insurancePrice;
    }

    public void setInsurancePrice(Double insurancePrice) {
        this.insurancePrice = insurancePrice;
    }
}