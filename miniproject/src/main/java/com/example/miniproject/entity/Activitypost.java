package com.example.miniproject.entity;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Activitypost")
public class Activitypost {

    @Id
    @Column(name = "activityid",length = 10)
    private String activityid;
    @Column(length = 100)
    private String title;
    @Lob
    @Column(columnDefinition = "LONGTEXT CHARACTER SET utf8mb4")
    private String description;
    @Column(length = 255)
    private String location;
    private Date createddate;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String images;


    // Many post belong to 1 manager
    @ManyToOne
    @JoinColumn(name = "managerid")
    private Communitymanager communitymanager;

    // Many post can promote 1 tour (nullable: บางโพสไม่เกี่ยวกับทัวร์ เช่น ข่าวสาร/ประกาศ)
    @ManyToOne
    @JoinColumn(name = "tourid",nullable = true)
    private Tour tour;

    public String getActivityid() {
        return activityid;
    }

    public void setActivityid(String activityid) {
        this.activityid = activityid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getCreateddate() {
        return createddate;
    }

    public void setCreateddate(Date createddate) {
        this.createddate = createddate;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }


    public Communitymanager getCommunitymanager() {
        return communitymanager;
    }

    public void setCommunitymanager(Communitymanager communitymanager) {
        this.communitymanager = communitymanager;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

   
}
