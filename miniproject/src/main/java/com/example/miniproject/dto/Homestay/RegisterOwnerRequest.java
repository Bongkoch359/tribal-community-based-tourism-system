package com.example.miniproject.dto.Homestay;

import java.util.List;

public class RegisterOwnerRequest {

    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String password;
    private List<HomestayDto> homestays;

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

    public List<HomestayDto> getHomestays() { return homestays; }
    public void setHomestays(List<HomestayDto> homestays) { this.homestays = homestays; }

    public static class HomestayDto {
    private String homestayname;
    private String description;
    private String address;
    private List<String> images; // ✅ Base64

    public String getHomestayname() { return homestayname; }
    public void setHomestayname(String homestayname) { this.homestayname = homestayname; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}
}
