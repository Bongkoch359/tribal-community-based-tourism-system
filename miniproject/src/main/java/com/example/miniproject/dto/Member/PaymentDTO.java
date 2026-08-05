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


    
    // จาก Payment
    private String paymentId;
    private Date paymentDate;
    private Double amount;
    private String paymentSlip;
    
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

    
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getPaymentSlip() { return paymentSlip; }
    public void setPaymentSlip(String paymentSlip) { this.paymentSlip = paymentSlip; }
}