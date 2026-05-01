package com.example.miniproject.dto.Member;

import java.util.List;

public class HomestayDTO {

    private String homestayid;
    private String homestayname;
    private String address;
    private String description;
    private List<String> images;
    private String status;

    public HomestayDTO() {}

    public String getHomestayid() {
        return homestayid;
    }

    public void setHomestayid(String homestayid) {
        this.homestayid = homestayid;
    }

    public String getHomestayname() {
        return homestayname;
    }

    public void setHomestayname(String homestayname) {
        this.homestayname = homestayname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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