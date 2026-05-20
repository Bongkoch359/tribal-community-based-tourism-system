package com.example.miniproject.dto.Homestay;

import java.util.List;

public class UpdateRoomRequest {

    private String  roomtypeid;
    private String  typename;
    private String  bedtype;
    private double  pricepernight;
    private int     maxguest;
    private int     totalrooms;
    private String  description;
    private String  roomcondition;
    private String  status;
    private List<String> facilitiesIds;

    /** comma-separated URL paths (รูปเดิม + รูปใหม่ รวมกัน) */
    private String images;

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public String  getRoomtypeid()              { return roomtypeid; }
    public void    setRoomtypeid(String v)      { this.roomtypeid = v; }

    public String  getTypename()                { return typename; }
    public void    setTypename(String v)        { this.typename = v; }

    public String  getBedtype()                 { return bedtype; }
    public void    setBedtype(String v)         { this.bedtype = v; }

    public double  getPricepernight()           { return pricepernight; }
    public void    setPricepernight(double v)   { this.pricepernight = v; }

    public int     getMaxguest()                { return maxguest; }
    public void    setMaxguest(int v)           { this.maxguest = v; }

    public int     getTotalrooms()              { return totalrooms; }
    public void    setTotalrooms(int v)         { this.totalrooms = v; }

    public String  getDescription()             { return description; }
    public void    setDescription(String v)     { this.description = v; }

    public String  getRoomcondition()           { return roomcondition; }
    public void    setRoomcondition(String v)   { this.roomcondition = v; }

    public String  getStatus()                  { return status; }
    public void    setStatus(String v)          { this.status = v; }

    public List<String> getFacilitiesIds()               { return facilitiesIds; }
    public void         setFacilitiesIds(List<String> v) { this.facilitiesIds = v; }

    public String  getImages()                  { return images; }
    public void    setImages(String v)          { this.images = v; }
}