package com.example.miniproject.dto.Tour;

public class TourRowDTO {
    
    private String tourid;
    private String tourname;
    private double adultprice;
    private String status;
    private int bookingCount;
 
    public String getTourid() { return tourid; }
    public void setTourid(String tourid) { this.tourid = tourid; }
 
    public String getTourname() { return tourname; }
    public void setTourname(String tourname) { this.tourname = tourname; }
 
    public double getAdultprice() { return adultprice; }
    public void setAdultprice(double adultprice) { this.adultprice = adultprice; }
 
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
 
    public int getBookingCount() { return bookingCount; }
    public void setBookingCount(int bookingCount) { this.bookingCount = bookingCount; }
}
