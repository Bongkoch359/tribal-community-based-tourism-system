package com.example.miniproject.dto.Tour;

public class BookingRowDTO {

    private String bookingid;
    private String memberName;
    private String bookingType;
    private String bookingdate;
    private double totalamount;
    private String status;

    public String getBookingid() { return bookingid; }
    public void setBookingid(String bookingid) { this.bookingid = bookingid; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getBookingType() { return bookingType; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }

    public String getBookingdate() { return bookingdate; }
    public void setBookingdate(String bookingdate) { this.bookingdate = bookingdate; }

    public double getTotalamount() { return totalamount; }
    public void setTotalamount(double totalamount) { this.totalamount = totalamount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}