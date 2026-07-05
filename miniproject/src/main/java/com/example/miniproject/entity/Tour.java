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

    @Column(length = 50)
    private String status;

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
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String images;

    // ===================== เพิ่มใหม่: ประเภททัวร์ / จำนวนวัน / จำนวนคืน
    // =====================
    @Column(length = 100)
    private String tourtype; 

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTourtype() {
        return tourtype;
    }

    public void setTourtype(String tourtype) {
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

    /**
     * Auto-set tourtype เฉพาะกรณีทัวร์รายวัน (numberOfDays == 1)
     * ส่วนทัวร์หลายวัน (2 วัน 1 คืน, 3 วัน 2 คืน, ฯลฯ) ต้องกำหนด tourtype เอง
     * เพราะชื่อประเภทขึ้นกับธีม/เนื้อหาทัวร์ ไม่ใช่แค่จำนวนวัน
     */
    @PrePersist
    @PreUpdate
    public void updateTourtype() {
        if (numberOfDays != null) {
            if (numberOfDays == 1) {
                this.tourtype = "ทัวร์รายวัน"; 
                if (numberOfNights == null) {
                    this.numberOfNights = 0; // ทัวร์รายวัน = 0 คืน
                }
            } else if (this.tourtype == null || this.tourtype.isBlank()) {
                // ป้องกันกรณีลืม set tourtype เองสำหรับทัวร์หลายวัน
                throw new IllegalStateException(
                        "กรุณาระบุ tourtype สำหรับทัวร์ที่มากกว่า 1 วัน (เช่น ทัวร์วัฒนธรรมชนเผ่า, ทัวร์วิถีชีวิต)");
            }
            // ถ้า tourtype ถูก set มาแล้วสำหรับทัวร์หลายวัน จะไม่ไป override ทับ
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
}