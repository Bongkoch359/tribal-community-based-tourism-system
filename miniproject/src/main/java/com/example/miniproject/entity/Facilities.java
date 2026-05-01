package  com.example.miniproject.entity;
import jakarta.persistence.*;

import java.util.List;



@Entity
@Table(name="Facilities")
public class Facilities {

    @Id
	@Column(name = "facilitiesid",length = 10)
    private String facilitiesid;
	@Column(length = 100)
    private String facilitiesname;

    @ManyToMany(mappedBy="facilities")
    private List<Roomtype> roomTypes;

    public Facilities(){
		
	}
	public Facilities(String facilitiesid, String facilitiesName, List<Roomtype> roomTypes) {
		super();
		this.facilitiesid = facilitiesid;
		this.facilitiesname = facilitiesName;
		this.roomTypes = roomTypes;
	}

	
	public String getFacilitiesid() {
		return facilitiesid;
	}


	public void setFacilitiesid(String facilitiesid) {
		this.facilitiesid = facilitiesid;
	}


	public String getFacilitiesname() {
		return facilitiesname;
	}


	public void setFacilitiesname(String facilitiesname) {
		this.facilitiesname = facilitiesname;
	}


	public List<Roomtype> getRoomTypes() {
		return roomTypes;
	}


	public void setRoomTypes(List<Roomtype> roomTypes) {
		this.roomTypes = roomTypes;
	}
    
}

