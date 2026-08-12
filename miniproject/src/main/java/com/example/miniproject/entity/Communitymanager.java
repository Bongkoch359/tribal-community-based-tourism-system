package com.example.miniproject.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import com.example.miniproject.entity.enums.ManagerStatus;

@Entity
@Table(name = "Communitymanager")
public class Communitymanager {

    @Id
    @Column(name = "managerid", length = 10)
    private String managerid;

    @Column(length = 100)
    private String password;

    @Column(length = 100)
    private String firstname;

    @Column(length = 100)
    private String lastname;

    @Column(length = 150)
    private String email;

    @Column(length = 15)
    private String phone;

    @Column(length = 100)
    private String tribe;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "account_name", length = 200)
    private String accountName;

    @Column(name = "account_number", length = 30)
    private String accountNumber;

    @Column(name = "bank_branch", length = 100)
    private String bankBranch;

    // วันที่สร้างบัญชี
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
	private String signatureImageUrl;

    @Enumerated(EnumType.STRING)
    private ManagerStatus accountstatus;

    @OneToMany(mappedBy = "communitymanager", cascade = CascadeType.ALL)
    private List<Tour> tours = new ArrayList<>();

    @OneToMany(mappedBy = "communitymanager")
    private List<Activitypost> activityPosts;

    public Communitymanager() {}

    // ใส่วันที่อัตโนมัติตอนสร้าง
    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
    }

    // Getter / Setter

    public String getManagerid() {
        return managerid;
    }

    public void setManagerid(String managerid) {
        this.managerid = managerid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTribe() {
        return tribe;
    }

    public void setTribe(String tribe) {
        this.tribe = tribe;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBankBranch() {
    return bankBranch;
    }

    public void setBankBranch(String bankBranch) {
        this.bankBranch = bankBranch;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public ManagerStatus getAccountstatus() {
        return accountstatus;
    }

    public void setAccountstatus(ManagerStatus accountstatus) {
        this.accountstatus = accountstatus;
    }

    public List<Tour> getTours() {
        return tours;
    }

    public void setTours(List<Tour> tours) {
        this.tours = tours;
    }

    public List<Activitypost> getActivityPosts() {
        return activityPosts;
    }

    public void setActivityPosts(List<Activitypost> activityPosts) {
        this.activityPosts = activityPosts;
    }

    public String getSignatureImageUrl() {
        return signatureImageUrl;
    }

    public void setSignatureImageUrl(String signatureImageUrl) {
        this.signatureImageUrl = signatureImageUrl;
    }
    
}