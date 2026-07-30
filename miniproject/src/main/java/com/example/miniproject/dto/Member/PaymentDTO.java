package com.example.miniproject.dto.Member;

import java.sql.Date;

public class PaymentDTO {

    private String bookingId;
    private Date bookingDate;
    private Double totalAmount;
    private String paymentStatus;
    private Date paymentDeadline;

    // จาก Booking
    private Integer numOfGuests;

    private String accountName; // ชื่อบัญชี
    private String bankName;
    private String bankAccount;


    // จาก Bookingroomdetail
    private Date checkIn;           // checkindate
    private Date checkOut;          // checkoutdate
    private Integer numOfRooms;     // numofrooms
    private Integer numOfAdults;    // numofadults
    private Integer numOfChildren;  // numofChcldren (typo ใน entity)

    // จาก Roomtype
    private String roomTypeName;    // typename
    private String roomImageUrl;    // images

    // จาก Homestay (ผ่าน Roomtype)
    private String homestayName;    // homestayname
    private String homestayAddress; // address

    // ── NEW: ใช้สำหรับหน้าใบเสร็จ (จาก Payment) ─────────────────────
    private String paymentId;       // paymentid
    private Date paymentDate;       // paymentdate
    private Double amount;          // amount ที่จ่ายจริง
    private String paymentSlip;     // ชื่อไฟล์สลิป

    // ── NEW: ใช้สำหรับหน้าใบเสร็จ - แยกรายการให้ยอดตรงกับ totalAmount ──
    private Double roomSubtotal;       // จาก Bookingroomdetail.subtotalroom
    private Boolean wantInsurance;     // จาก Booking.wantInsurance
    private Double subtotalInsurance;  // จาก Booking.subtotalInsurance

    // ── getters / setters ─────────────────────────────────────────

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public Date getBookingDate() { return bookingDate; }
    public void setBookingDate(Date bookingDate) { this.bookingDate = bookingDate; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }


    public Date getPaymentDeadline() { return paymentDeadline; }
    public void setPaymentDeadline(Date paymentDeadline) { this.paymentDeadline = paymentDeadline; }


    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public Integer getNumOfGuests() { return numOfGuests; }
    public void setNumOfGuests(Integer numOfGuests) { this.numOfGuests = numOfGuests; }

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

    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

    public String getRoomImageUrl() { return roomImageUrl; }
    public void setRoomImageUrl(String roomImageUrl) { this.roomImageUrl = roomImageUrl; }

    public String getHomestayName() { return homestayName; }
    public void setHomestayName(String homestayName) { this.homestayName = homestayName; }

    public String getHomestayAddress() { return homestayAddress; }
    public void setHomestayAddress(String homestayAddress) { this.homestayAddress = homestayAddress;}

    
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getPaymentSlip() { return paymentSlip; }
    public void setPaymentSlip(String paymentSlip) { this.paymentSlip = paymentSlip; }

    public Double getRoomSubtotal() { return roomSubtotal; }
    public void setRoomSubtotal(Double roomSubtotal) { this.roomSubtotal = roomSubtotal; }

    public Boolean getWantInsurance() { return wantInsurance; }
    public void setWantInsurance(Boolean wantInsurance) { this.wantInsurance = wantInsurance; }

    public Double getSubtotalInsurance() { return subtotalInsurance; }
    public void setSubtotalInsurance(Double subtotalInsurance) { this.subtotalInsurance = subtotalInsurance; }
}