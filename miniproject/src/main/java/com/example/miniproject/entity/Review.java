package  com.example.miniproject.entity;
import jakarta.persistence.*;

import java.sql.Date;

@Entity
@Table(name="Review")
public class Review {
	@Id
	@Column(length = 10)
	private String reviewid;
    private Integer rating;
	@Column(length = 255)
    private String comment;
	@Column(length = 255)
	private	String reviewimage;
    private Date reviewdate;
    
     @OneToOne
    @JoinColumn(name="bookingid", unique = true)
    private Booking booking;

    
	public Review() {
		// TODO Auto-generated constructor stub

	}


	public String getReviewid() {
		return reviewid;
	}


	public void setReviewid(String reviewid) {
		this.reviewid = reviewid;
	}


	public Integer getRating() {
		return rating;
	}


	public void setRating(Integer rating) {
		this.rating = rating;
	}


	public String getComment() {
		return comment;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}


	public String getReviewimage() {
		return reviewimage;
	}


	public void setReviewimage(String reviewimage) {
		this.reviewimage = reviewimage;
	}


	public Date getReviewdate() {
		return reviewdate;
	}


	public void setReviewdate(Date reviewdate) {
		this.reviewdate = reviewdate;
	}


	public Booking getBooking() {
		return booking;
	}


	public void setBooking(Booking booking) {
		this.booking = booking;
	}
	
	

}
