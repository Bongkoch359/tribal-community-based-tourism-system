package com.example.miniproject.dto.Homestay;

import java.util.List;

public class UpdateRoomRequest {

    private String  roomtypeid;
    private String  typename;
    private String  bedtype;
    private Double  pricepernight;
    private Integer maxguest;
    private Integer totalrooms;
    private String  description;
    private String  roomcondition;
    private String  status;
    private List<String> facilitiesIds;
    private List<String> images;       // Base64 strings (รูปใหม่ที่อัปโหลด)
    private String  existingImages;    // รูปเดิม (Base64 ที่ยังเก็บอยู่ คั่นด้วย ",")

    public String getRoomtypeid()      { return roomtypeid; }
    public void setRoomtypeid(String roomtypeid) { this.roomtypeid = roomtypeid; }

    public String getTypename()        { return typename; }
    public void setTypename(String typename) { this.typename = typename; }

    public String getBedtype()         { return bedtype; }
    public void setBedtype(String bedtype) { this.bedtype = bedtype; }

    public Double getPricepernight()   { return pricepernight; }
    public void setPricepernight(Double pricepernight) { this.pricepernight = pricepernight; }

    public Integer getMaxguest()       { return maxguest; }
    public void setMaxguest(Integer maxguest) { this.maxguest = maxguest; }

    public Integer getTotalrooms()     { return totalrooms; }
    public void setTotalrooms(Integer totalrooms) { this.totalrooms = totalrooms; }

    public String getDescription()     { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRoomcondition()   { return roomcondition; }
    public void setRoomcondition(String roomcondition) { this.roomcondition = roomcondition; }

    public String getStatus()          { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getFacilitiesIds() { return facilitiesIds; }
    public void setFacilitiesIds(List<String> facilitiesIds) { this.facilitiesIds = facilitiesIds; }

    public List<String> getImages()    { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getExistingImages()  { return existingImages; }
    public void setExistingImages(String existingImages) { this.existingImages = existingImages; }
}