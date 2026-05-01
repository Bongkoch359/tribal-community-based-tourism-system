package  com.example.miniproject.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class Bookingroomdetailid implements Serializable {

    private String bookingid;
    private String roomtypeid;

    public Bookingroomdetailid() {}

    public Bookingroomdetailid(String bookingid, String roomtypeid) {
        this.bookingid = bookingid;
        this.roomtypeid = roomtypeid;
    }

    public String getBookingid() { return bookingid; }
    public void setBookingid(String bookingid) { this.bookingid = bookingid; }

    public String getRoomtypeid() { return roomtypeid; }
    public void setRoomtypeid(String roomtypeid) { this.roomtypeid = roomtypeid; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bookingroomdetailid)) return false;
        Bookingroomdetailid that = (Bookingroomdetailid) o;
        return Objects.equals(bookingid, that.bookingid) &&
               Objects.equals(roomtypeid, that.roomtypeid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingid, roomtypeid);
    }
}