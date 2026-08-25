package com.example.miniproject.entity;

import com.example.miniproject.entity.enums.BookingType;
import com.example.miniproject.entity.enums.BookingStatus;
import java.sql.Date;
import java.util.List;
import java.util.Set;
import java.sql.Timestamp;

import jakarta.persistence.*;

@Entity
@Table(name = "Booking")
public class Booking {
	@Id
	@Column(name = "bookingid", length = 10)
	private String bookingid;

	@Enumerated(EnumType.STRING)
	@Column(name = "booking_type", length = 50)
	private BookingType bookingType;

	private Integer numofguest;

	private Boolean isBookerGoing;
	@Column(name = "bookingdate")
	private Date bookingdate;

	// ★ เพิ่มใหม่ — เวลาที่ต้องชำระเงินให้เสร็จก่อน (bookingdate + 30 นาที)
	@Column(name = "payment_deadline")
	private Timestamp paymentDeadline;

	@Enumerated(EnumType.STRING)
	@Column(name = "booking_status", length = 50)
	private BookingStatus bookingStatus;

	@Column(name = "note")
	private String note;

	@Column(name = "pickuptype", length = 100)
	private String pickuptype;

	@Column(name = "pickuplocation", length = 255)
	private String pickuplocation;
	private Double totalamount;
	@Column(name = "cancel_reason", length = 500)
	private String cancelReason;

	// ── ประกัน (checkbox เดียว ครอบคลุมทุกคนในการจอง) ──
	@Column(name = "want_insurance")
	private Boolean wantInsurance = false;

	@Column(name = "insurance_fee_per_person")
	private Double insuranceFeePerPerson; // เก็บราคา ณ วันที่จอง กันราคาเปลี่ยนทีหลัง

	@Column(name = "subtotal_insurance")
	private Double subtotalInsurance; // = insuranceFeePerPerson * numofguest (ถ้าติ๊ก)

	// relationship
	@ManyToOne
	@JoinColumn(name = "memberid")
	private Member member;

	@OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
	private Payment payment;
	@OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
	private Review review;

	@OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
	private Set<Guest> guests;

	// ===== TOUR DETAILS =====
	@OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
	private List<Bookingtourdetail> tourDetails;

	// ===== ROOM DETAILS =====
	@OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
	private List<Bookingroomdetail> roomDetails;

	public Booking() {
		// TODO Auto-generated constructor stub
	}

	public String getBookingid() {
		return bookingid;
	}

	public void setBookingid(String bookingid) {
		this.bookingid = bookingid;
	}

	public Date getBookingdate() {
		return bookingdate;
	}

	public void setBookingdate(Date bookingdate) {
		this.bookingdate = bookingdate;
	}

	public Timestamp getPaymentDeadline() {
		return paymentDeadline;
	}

	public void setPaymentDeadline(Timestamp paymentDeadline) {
		this.paymentDeadline = paymentDeadline;
	}


	public BookingType getBookingType() {
		return bookingType;
	}

	public void setBookingType(BookingType bookingType) {
		this.bookingType = bookingType;
	}

	public BookingStatus getBookingStatus() {
		return bookingStatus;
	}

	public void setBookingStatus(BookingStatus bookingStatus) {
		this.bookingStatus = bookingStatus;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getPickuptype() {
		return pickuptype;
	}

	public void setPickuptype(String pickuptype) {
		this.pickuptype = pickuptype;
	}

	public String getPickuplocation() {
		return pickuplocation;
	}

	public void setPickuplocation(String pickuplocation) {
		this.pickuplocation = pickuplocation;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	}

	public Review getReview() {
		return review;
	}

	public void setReview(Review review) {
		this.review = review;
	}

	public Set<Guest> getGuests() {
		return guests;
	}

	public void setGuests(Set<Guest> guests) {
		this.guests = guests;
	}

	public List<Bookingtourdetail> getTourDetails() {
		return tourDetails;
	}

	public void setTourDetails(List<Bookingtourdetail> tourDetails) {
		this.tourDetails = tourDetails;
	}

	public List<Bookingroomdetail> getRoomDetails() {
		return roomDetails;
	}

	public void setRoomDetails(List<Bookingroomdetail> roomDetails) {
		this.roomDetails = roomDetails;
	}

	public Double getTotalamount() {
		return totalamount;
	}

	public void setTotalamount(Double totalamount) {
		this.totalamount = totalamount;
	}

	public Integer getNumofguest() {
		return numofguest;
	}

	public void setNumofguest(Integer numofguest) {
		this.numofguest = numofguest;
	}

	public Boolean getIsBookerGoing() {
		return isBookerGoing;
	}

	public void setIsBookerGoing(Boolean isBookerGoing) {
		this.isBookerGoing = isBookerGoing;
	}

	public Boolean getWantInsurance() {
		return wantInsurance;
	}

	public void setWantInsurance(Boolean wantInsurance) {
		this.wantInsurance = wantInsurance;
	}

	public Double getInsuranceFeePerPerson() {
		return insuranceFeePerPerson;
	}

	public void setInsuranceFeePerPerson(Double insuranceFeePerPerson) {
		this.insuranceFeePerPerson = insuranceFeePerPerson;
	}

	public Double getSubtotalInsurance() {
		return subtotalInsurance;
	}

	public void setSubtotalInsurance(Double subtotalInsurance) {
		this.subtotalInsurance = subtotalInsurance;
	}

	public String getCancelReason() {
		return cancelReason;
	}

	public void setCancelReason(String cancelReason) {
		this.cancelReason = cancelReason;
	}
	

}
