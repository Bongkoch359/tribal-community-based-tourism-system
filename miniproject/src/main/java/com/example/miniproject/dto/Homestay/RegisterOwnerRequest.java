package com.example.miniproject.dto.Homestay;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class RegisterOwnerRequest {

    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String password;
    private List<HomestayItem> homestays;
    // private List<HomestayDto> homestay; 
    // Getters & Setters
    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<HomestayItem> getHomestays() { return homestays; }
    public void setHomestays(List<HomestayItem> homestays) { this.homestays = homestays; }
    

    // ── Inner class สำหรับข้อมูลแต่ละ homestay ──
    public static class HomestayItem {
        private String homestayname;
        private String description;
        private String address;
        private List<MultipartFile> images;

        public String getHomestayname() { return homestayname; }
        public void setHomestayname(String homestayname) { this.homestayname = homestayname; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public List<MultipartFile> getImages() { return images; }
        public void setImages(List<MultipartFile> images) { this.images = images; }
    }


    
}