package com.example.miniproject.dto.Tour;

public class PostRowDTO {

    private String activityid;
    private String title;
    private String location;
    private String status;

    public String getActivityid()              { return activityid; }
    public void setActivityid(String v)        { this.activityid = v; }

    public String getTitle()                   { return title; }
    public void setTitle(String v)             { this.title = v; }

    public String getLocation()                { return location; }
    public void setLocation(String v)          { this.location = v; }

    public String getStatus()                  { return status; }
    public void setStatus(String v)            { this.status = v; }
}