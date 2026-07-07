package com.example.miniproject.entity;


import jakarta.persistence.*;
 
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tourtype")
public class TourType {
 
    @Id
    @Column(name = "typeId", length = 10)
    private String typeId;
 
    @Column(name = "Typename", length = 200)
    private String typename;
 
    // 1 ประเภททัวร์ ใช้ได้กับหลายทัวร์
    @OneToMany(mappedBy = "tourtype")
    private List<Tour> tours = new ArrayList<>();
 
    public TourType() {
    }
 
    public TourType(String typeId, String typename) {
        this.typeId = typeId;
        this.typename = typename;
    }
 
    public String getTypeId() {
        return typeId;
    }
 
    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }
 
    public String getTypename() {
        return typename;
    }
 
    public void setTypename(String typename) {
        this.typename = typename;
    }
 
    public List<Tour> getTours() {
        return tours;
    }
 
    public void setTours(List<Tour> tours) {
        this.tours = tours;
    }
}
 