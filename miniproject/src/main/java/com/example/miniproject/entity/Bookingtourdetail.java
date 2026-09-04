package  com.example.miniproject.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Bookingtourdetail")
public class Bookingtourdetail {

    @EmbeddedId
    private Bookingtourdetailid id;

    private Integer numofadult;
    private Integer numofchild;
    private Double subtotaltour;

    @ManyToOne
    @MapsId("bookingid")
    @JoinColumn(name = "bookingid", insertable = false, updatable = false)
    private Booking booking;

    // ผูกการจองนี้เข้ากับ "รอบ/วันที่เปิดทัวร์" ที่เลือกไว้ — วันที่จองอ่านได้จาก
    // tourschedule.getOpendate() โดยตรง เป็นส่วนหนึ่งของ primary key ด้วย (bookingid + scheduleid)
    @ManyToOne
    @MapsId("scheduleid")
    @JoinColumn(name = "scheduleid", nullable = false)
    private Tourschedule tourschedule;

    public Bookingtourdetail() {}

    public Bookingtourdetailid getId() { return id; }
    public void setId(Bookingtourdetailid id) { this.id = id; }

    public Tourschedule getTourschedule() { return tourschedule; }
    public void setTourschedule(Tourschedule tourschedule) { this.tourschedule = tourschedule; }

    public Integer getNumofadult() {
        return numofadult;
    }

    public void setNumofadult(Integer numofadult) {
        this.numofadult = numofadult;
    }

    public Integer getNumofchild() {
        return numofchild;
    }

    public void setNumofchild(Integer numofchild) {
        this.numofchild = numofchild;
    }

    public Double getSubtotaltour() { return subtotaltour; }
    public void setSubtotaltour(Double subtotaltour) { this.subtotaltour = subtotaltour; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    // ดึง Tour ผ่าน schedule แทน (ไม่มี FK ตรงไป Tour แล้ว)
    public Tour getTour() {
        return tourschedule != null ? tourschedule.getTour() : null;
    }

}