package com.example.miniproject.dto.Member;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class TourReceiptDTO {

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

    // ── จาก Bookingtourdetail ──────────────────────────────────
    private Integer numOfAdults;    // numofadult
    private Integer numOfChildren;  // numofchild
    private Double subtotalTour;    // subtotaltour

    // ── จาก Tour ────────────────────────────────────────────────
    private String tourName;        // tourmname
    private String tourDuration;    // Tour.getTourDuration() เช่น "3 วัน 2 คืน"
    private String tourImageUrl;
    private Double adultPrice;      // adultprice
    private Double childPrice;      // childprice

    // ── จาก Tourschedule (รอบ/วันที่เปิดทัวร์ที่เลือก) ──────────
    private Date scheduleOpenDate;  // opendate

    // ── จาก Communitymanager (ผู้ออกใบเสร็จฝั่งทัวร์) ───────────
    private String communityManagerName;
    private String communityManagerAddress;

    // ── ประกันภัย (จาก Booking) ────────────────────────────────
    private Boolean wantInsurance;
    private Double subtotalInsurance;

    // เพิ่มใน RoomReceiptDTO และ TourReceiptDTO
    private Timestamp paymentDeadline;

    private String signatureImageUrl;

    public TourReceiptDTO() {}

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

    public Integer getNumOfAdults() { return numOfAdults; }
    public void setNumOfAdults(Integer numOfAdults) { this.numOfAdults = numOfAdults; }

    public Integer getNumOfChildren() { return numOfChildren; }
    public void setNumOfChildren(Integer numOfChildren) { this.numOfChildren = numOfChildren; }

    public Double getSubtotalTour() { return subtotalTour; }
    public void setSubtotalTour(Double subtotalTour) { this.subtotalTour = subtotalTour; }

    public String getTourName() { return tourName; }
    public void setTourName(String tourName) { this.tourName = tourName; }

    public String getTourDuration() { return tourDuration; }
    public void setTourDuration(String tourDuration) { this.tourDuration = tourDuration; }

    public Double getAdultPrice() { return adultPrice; }
    public void setAdultPrice(Double adultPrice) { this.adultPrice = adultPrice; }

    public Double getChildPrice() { return childPrice; }
    public void setChildPrice(Double childPrice) { this.childPrice = childPrice; }

    public Date getScheduleOpenDate() { return scheduleOpenDate; }
    public void setScheduleOpenDate(Date scheduleOpenDate) { this.scheduleOpenDate = scheduleOpenDate; }

    public String getCommunityManagerName() { return communityManagerName; }
    public void setCommunityManagerName(String communityManagerName) { this.communityManagerName = communityManagerName; }

    public String getCommunityManagerAddress() { return communityManagerAddress; }
    public void setCommunityManagerAddress(String communityManagerAddress) { this.communityManagerAddress = communityManagerAddress; }

    public Boolean getWantInsurance() { return wantInsurance; }
    public void setWantInsurance(Boolean wantInsurance) { this.wantInsurance = wantInsurance; }

    public Double getSubtotalInsurance() { return subtotalInsurance; }
    public void setSubtotalInsurance(Double subtotalInsurance) { this.subtotalInsurance = subtotalInsurance; }

    public Timestamp getPaymentDeadline() { return paymentDeadline; }
    public void setPaymentDeadline(Timestamp paymentDeadline) { this.paymentDeadline = paymentDeadline; }

    // ★ ส่งเป็น ISO datetime string ให้ JS parse ได้ชัวร์ ไม่พึ่ง serialize อัตโนมัติ
    public String getPaymentDeadlineIso() {
        return paymentDeadline != null
                ? paymentDeadline.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;
    }

    public String getTourImageUrl() { return tourImageUrl; }
    public void setTourImageUrl(String tourImageUrl) { this.tourImageUrl = tourImageUrl; }

    public String getSignatureImageUrl() { return signatureImageUrl; }
    public void setSignatureImageUrl(String signatureImageUrl) { this.signatureImageUrl = signatureImageUrl; }
}