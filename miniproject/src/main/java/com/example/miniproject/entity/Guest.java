package  com.example.miniproject.entity;
import jakarta.persistence.*;

@Entity
@Table(name="Guest")
public class Guest {
	@Id
	  @Column(length = 10)
	private String guestid;
	 @Column(length = 100)
    private String firstname;
	 @Column(length = 100)
    private String lastname;
    
    @ManyToOne
    @JoinColumn(name="bookingid")
    private Booking booking;
	public Guest() {
		// TODO Auto-generated constructor stub
	}
	public String getGuestid() {
		return guestid;
	}
	public void setGuestid(String guestid) {
		this.guestid = guestid;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public Booking getBooking() {
		return booking;
	}
	public void setBooking(Booking booking) {
		this.booking = booking;
	}
	

}
