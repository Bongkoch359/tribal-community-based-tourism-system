package com.example.miniproject.dto.Member;

import java.sql.Date;

public class RoomReceiptDTO {

    // ── Booking / Payment พื้นฐาน ─────────────────────────────
    private String bookingId;
    private Date bookingDate;
    private Double totalAmount;
    private String paymentStatus;

    private String paymentId;
    private Date paymentDate;
    private Double amount;          // ยอดที่จ่ายจริง
    private String paymentSlip;

    private String accountName;
    private String bankName;
    private String bankAccount;

    // ── ผู้จ่ายเงิน (จาก Booking.member) ─────────────────────
    private String memberFirstname;
    private String memberLastname;
    private String memberPhone;

    // ── จาก Bookingroomdetail ─────────────────────────────────
    private Date checkIn;           // checkindate
    private Date checkOut;          // checkoutdate
    private Integer numOfRooms;     // numofrooms
    private Integer numOfAdults;    // numofadults
    private Integer numOfChildren;  // numofChcldren (typo ใน entity)
    private Double roomSubtotal;    // subtotalroom

    // ── จาก Roomtype ────────────────────────────────────────────
    private String roomTypeName;    // typename
    private String roomImageUrl;    // images

    // ── จาก Homestay (ผ่าน Roomtype) ───────────────────────────
    private String homestayName;    // homestayname
    private String homestayAddress; // address

    // ── ประกันภัย (จาก Booking) ────────────────────────────────
    private Boolean wantInsurance;
    private Double subtotalInsurance;
    private Date paymentDeadline;

    public RoomReceiptDTO() {}

    // ── getters / setters ─────────────────────────────────────────

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public Date getBookingDate() { return bookingDate; }
    public void setBookingDate(Date bookingDate) { this.bookingDate = bookingDate; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getPaymentSlip() { return paymentSlip; }
    public void setPaymentSlip(String paymentSlip) { this.paymentSlip = paymentSlip; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public String getMemberFirstname() { return memberFirstname; }
    public void setMemberFirstname(String memberFirstname) { this.memberFirstname = memberFirstname; }

    public String getMemberLastname() { return memberLastname; }
    public void setMemberLastname(String memberLastname) { this.memberLastname = memberLastname; }

    public String getMemberPhone() { return memberPhone; }
    public void setMemberPhone(String memberPhone) { this.memberPhone = memberPhone; }

    public Date getCheckIn() { return checkIn; }
    public void setCheckIn(Date checkIn) { this.checkIn = checkIn; }

    public Date getCheckOut() { return checkOut; }
    public void setCheckOut(Date checkOut) { this.checkOut = checkOut; }

    public Integer getNumOfRooms() { return numOfRooms; }
    public void setNumOfRooms(Integer numOfRooms) { this.numOfRooms = numOfRooms; }

    public Integer getNumOfAdults() { return numOfAdults; }
    public void setNumOfAdults(Integer numOfAdults) { this.numOfAdults = numOfAdults; }

    public Integer getNumOfChildren() { return numOfChildren; }
    public void setNumOfChildren(Integer numOfChildren) { this.numOfChildren = numOfChildren; }

    public Double getRoomSubtotal() { return roomSubtotal; }
    public void setRoomSubtotal(Double roomSubtotal) { this.roomSubtotal = roomSubtotal; }

    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

    public String getRoomImageUrl() { return roomImageUrl; }
    public void setRoomImageUrl(String roomImageUrl) { this.roomImageUrl = roomImageUrl; }

    public String getHomestayName() { return homestayName; }
    public void setHomestayName(String homestayName) { this.homestayName = homestayName; }

    public String getHomestayAddress() { return homestayAddress; }
    public void setHomestayAddress(String homestayAddress) { this.homestayAddress = homestayAddress; }

    public Boolean getWantInsurance() { return wantInsurance; }
    public void setWantInsurance(Boolean wantInsurance) { this.wantInsurance = wantInsurance; }

    public Double getSubtotalInsurance() { return subtotalInsurance; }
    public void setSubtotalInsurance(Double subtotalInsurance) { this.subtotalInsurance = subtotalInsurance; }

public Date getPaymentDeadline() { return paymentDeadline; }
public void setPaymentDeadline(Date paymentDeadline) { this.paymentDeadline = paymentDeadline; }
}