package  com.example.miniproject.entity;
import jakarta.persistence.*;
import com.example.miniproject.entity.enums.HomestayownerStatus;
import java.util.List;

@Entity
@Table(name="Homestayowner")
public class Homestayowner {

    @Id
	@Column(name = "ownerid",length = 10)
    private Integer ownerid;
    @Column(name="password", length = 1050)
	private String password;

	@Column(name="firstname", length = 100)
	private String firstname;

	@Column(name="lastname", length = 100)
	private String lastname;

	@Column(name="email", length = 150)
	private String email;

	@Column(name="phone", length = 10)
	private String phone;

	@Column(name = "verificationstatus")
	private  Boolean verificationstatus;

	@Enumerated(EnumType.STRING)
	private HomestayownerStatus accountstatus;

    @OneToMany(mappedBy="owner")
    private List<Homestay> homestays;
	public Homestayowner() {
		// TODO Auto-generated constructor stub
	}
	public Integer getOwnerid() {
		return ownerid;
	}
	public void setOwnerid(Integer ownerid) {
		this.ownerid = ownerid;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public Boolean getVerificationstatus() {
		return verificationstatus;
	}
	public void setVerificationstatus(Boolean verificationstatus) {
		this.verificationstatus = verificationstatus;
	}
	public List<Homestay> getHomestays() {
		return homestays;
	}
	public void setHomestays(List<Homestay> homestays) {
		this.homestays = homestays;
	}
	public HomestayownerStatus getAccountstatus() {
		return accountstatus;
	}
	public void setAccountstatus(HomestayownerStatus accountstatus) {
		this.accountstatus = accountstatus;
	}
	
	

}
