package  com.example.miniproject.entity;

import jakarta.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "Bookingroomdetail")
public class Bookingroomdetail {

    @EmbeddedId
    private Bookingroomdetailid id;

    @Column(name = "checkindate")
    private Date checkindate;

    @Column(name = "checkoutdate")
    private Date checkoutdate;

    private Integer numofadults;

    private Integer numofChcldren;

    private Integer numofrooms;

    private Double subtotalroom;

    @ManyToOne
    @MapsId("bookingid")
    @JoinColumn(name = "bookingid", insertable = false, updatable = false)
    private Booking booking;

    @ManyToOne
    @MapsId("roomtypeid")
    @JoinColumn(name = "roomtypeid", insertable = false, updatable = false)
    private Roomtype roomtype;

    public Bookingroomdetail() {}

    public Bookingroomdetailid getId() { return id; }
    public void setId(Bookingroomdetailid id) { this.id = id; }

    public Date getCheckindate() { return checkindate; }
    public void setCheckindate(Date checkindate) { this.checkindate = checkindate; }

    public Date getCheckoutdate() { return checkoutdate; }
    public void setCheckoutdate(Date checkoutdate) { this.checkoutdate = checkoutdate; }

    public Integer getNumofadults() { return numofadults; }
    public void setNumofadults(Integer numofadults) { this.numofadults = numofadults; }

    public Integer getNumofChcldren() { return numofChcldren; }
    public void setNumofChcldren(Integer numofChcldren) { this.numofChcldren = numofChcldren; }

    public Integer getNumofrooms() { return numofrooms; }
    public void setNumofrooms(Integer numofrooms) { this.numofrooms = numofrooms; }

    public Double getSubtotalroom() { return subtotalroom; }
    public void setSubtotalroom(Double subtotalroom) { this.subtotalroom = subtotalroom; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public Roomtype getRoomtype() { return roomtype; }
    public void setRoomtype(Roomtype roomtype) { this.roomtype = roomtype; }
}