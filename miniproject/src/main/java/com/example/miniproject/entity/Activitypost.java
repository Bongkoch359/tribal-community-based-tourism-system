package com.example.miniproject.entity;
import jakarta.persistence.*;
import java.util.Date;

import com.example.miniproject.entity.enums.ActivityStatus;

@Entity
@Table(name = "Activitypost")
public class Activitypost {

    @Id
    @Column(name = "activityid",length = 10)
    private String activityid;
    @Column(length = 100)
    private String title;
    @Column(length = 255)
    private String description;
    @Column(length = 255)
    private String location;
    private Date createddate;

    private String images;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private ActivityStatus status;

    // Many post belong to 1 manager
    @ManyToOne
    @JoinColumn(name = "managerid")
    private Communitymanager communitymanager;

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

    public ActivityStatus getStatus() {
        return status;
    }

    public void setStatus(ActivityStatus status) {
        this.status = status;
    }

   
}
