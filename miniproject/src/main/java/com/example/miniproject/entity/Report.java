package com.example.miniproject.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Report")
public class Report {

    @Id
@Column(name = "reportid", length = 10)
private String reportid;

    @Column(length = 100)
    private String reason;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String evidenceImage;

    // PENDING / RESOLVED / REJECTED
    @Column(length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // เชื่อมกับ Tour หรือ Homestay อย่างใดอย่างหนึ่งเท่านั้น (nullable ทั้งคู่)
    @ManyToOne
    @JoinColumn(name = "tourid")
    private Tour tour;

    @ManyToOne
    @JoinColumn(name = "homestayid")
    private Homestay homestay;

    public Report() {
    }

    @PrePersist
    public void beforeInsert() {
        if (this.status == null) {
            this.status = "PENDING";
        }
        this.createdAt = LocalDateTime.now();
    }

    // ต้องมี tour หรือ homestay อย่างใดอย่างหนึ่งเท่านั้น ห้ามใส่ทั้งคู่หรือไม่ใส่เลย
    @Transient
    public boolean isValidTarget() {
        return (tour != null && homestay == null)
                || (tour == null && homestay != null);
    }

    public String getReportid() {
        return reportid;
    }

    public void setReportid(String reportid) {
        this.reportid = reportid;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvidenceImage() {
        return evidenceImage;
    }

    public void setEvidenceImage(String evidenceImage) {
        this.evidenceImage = evidenceImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public Homestay getHomestay() {
        return homestay;
    }

    public void setHomestay(Homestay homestay) {
        this.homestay = homestay;
    }
}