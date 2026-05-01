package  com.example.miniproject.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class Bookingtourdetailid implements Serializable {

    private String bookingid;
    private String tourid;

    public Bookingtourdetailid() {}

    public Bookingtourdetailid(String bookingid, String tourid) {
        this.bookingid = bookingid;
        this.tourid = tourid;
    }

    public String getBookingid() { return bookingid; }
    public void setBookingid(String bookingid) { this.bookingid = bookingid; }

    public String gettourid() { return tourid; }
    public void settourid(String tourid) { this.tourid = tourid; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bookingtourdetailid)) return false;
        Bookingtourdetailid that = (Bookingtourdetailid) o;
        return Objects.equals(bookingid, that.bookingid) &&
               Objects.equals(tourid, that.tourid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingid, tourid);
    }
}