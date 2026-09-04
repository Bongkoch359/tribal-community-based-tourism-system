package  com.example.miniproject.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class Bookingtourdetailid implements Serializable {

    private String bookingid;
    private String scheduleid;

    public Bookingtourdetailid() {}

    public Bookingtourdetailid(String bookingid, String scheduleid) {
        this.bookingid = bookingid;
        this.scheduleid = scheduleid;
    }

    public String getBookingid() { return bookingid; }
    public void setBookingid(String bookingid) { this.bookingid = bookingid; }

    public String getScheduleid() { return scheduleid; }
    public void setScheduleid(String scheduleid) { this.scheduleid = scheduleid; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bookingtourdetailid)) return false;
        Bookingtourdetailid that = (Bookingtourdetailid) o;
        return Objects.equals(bookingid, that.bookingid) &&
               Objects.equals(scheduleid, that.scheduleid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingid, scheduleid);
    }
}