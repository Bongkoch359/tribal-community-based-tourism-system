package com.example.miniproject.dto.Member;


import java.util.List;

public class TourDTO {

    private String tourid;
    private String tourname;
    private String detail;
    private String condition;
    private Integer minseats;
    private Integer maxseats;
    private Double adultprice;
    private Double childprice;
    private List<String> images;
    private String status;

    public TourDTO() {}

    public String getTourid() {
        return tourid;
    }

    public void setTourid(String tourid) {
        this.tourid = tourid;
    }

    public String getTourname() {
        return tourname;
    }

    public void setTourname(String tourname) {
        this.tourname = tourname;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Integer getMinseats() {
        return minseats;
    }

    public void setMinseats(Integer minseats) {
        this.minseats = minseats;
    }

    public Integer getMaxseats() {
        return maxseats;
    }

    public void setMaxseats(Integer maxseats) {
        this.maxseats = maxseats;
    }

    public Double getAdultprice() {
        return adultprice;
    }

    public void setAdultprice(Double adultprice) {
        this.adultprice = adultprice;
    }

    public Double getChildprice() {
        return childprice;
    }

    public void setChildprice(Double childprice) {
        this.childprice = childprice;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}