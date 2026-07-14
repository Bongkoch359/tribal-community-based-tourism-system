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

    @ManyToOne
    @MapsId("tourid")
    @JoinColumn(name = "tourid", insertable = false, updatable = false)
    private Tour tour;

    // ผูกการจองนี้เข้ากับ "รอบ/วันที่เปิดทัวร์" ที่เลือกไว้ — วันที่จองอ่านได้จาก
    // tourschedule.getOpendate() โดยตรง
    @ManyToOne
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

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

}