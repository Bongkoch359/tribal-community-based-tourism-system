package com.example.miniproject.entity;

import jakarta.persistence.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Tourschedule = "รอบ/วันที่เปิดทัวร์" ของ Tour หนึ่งตัว
 */
@Entity
@Table(name = "Tourschedule")
public class Tourschedule {

    @Id
    @Column(name = "scheduleid", length = 20)
    private String scheduleid;

    @ManyToOne
    @JoinColumn(name = "tourid", nullable = false)
    private Tour tour;

    @Column(name = "opendate", nullable = false)
    private Date opendate;

    @Column(name = "enddate", nullable = false)
    private Date enddate;

    // ค่าที่ใช้: "เปิดรับจอง" | "เต็ม" | "ปิด"
    @Column(length = 20)
    private String status;

    @OneToMany(mappedBy = "tourschedule")
    @Fetch(FetchMode.SUBSELECT)
    private List<Bookingtourdetail> bookingtourdetails = new ArrayList<>();

    public Tourschedule() {
    }

    public String getScheduleid() {
        return scheduleid;
    }

    public void setScheduleid(String scheduleid) {
        this.scheduleid = scheduleid;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public Date getOpendate() {
        return opendate;
    }

    public void setOpendate(Date opendate) {
        this.opendate = opendate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Bookingtourdetail> getBookingtourdetails() {
        return bookingtourdetails;
    }

    public void setBookingtourdetails(List<Bookingtourdetail> bookingtourdetails) {
        this.bookingtourdetails = bookingtourdetails;
    }

    // ─────────────────────────────────────────────────────────
    // ที่นั่งที่จองไปแล้วของรอบนี้ (คำนวณสด ไม่เก็บลง DB)
    // นับเฉพาะ booking ที่ยังไม่ถูกยกเลิก — เช็คสถานะที่ชั้น Service
    // (ใส่ไว้ที่นี่เป็น convenience method เผื่อใช้ตรงๆ ใน view)
    // ─────────────────────────────────────────────────────────
    @Transient
    public int getBookedSeatsRaw() {
        if (bookingtourdetails == null)
            return 0;
        return bookingtourdetails.stream()
                .filter(b -> b.getBooking() != null
                        && b.getBooking().getBookingStatus() != null
                        && b.getBooking()
                                .getBookingStatus() != com.example.miniproject.entity.enums.BookingStatus.CANCEL)
                .mapToInt(b -> {
                    int adult = b.getNumofadult() != null ? b.getNumofadult() : 0;
                    int child = b.getNumofchild() != null ? b.getNumofchild() : 0;
                    return adult + child;
                })
                .sum();
    }

    public Date getEnddate() {
        return enddate;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }
}
