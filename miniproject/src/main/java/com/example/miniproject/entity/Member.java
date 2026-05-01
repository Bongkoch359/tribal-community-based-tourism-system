package  com.example.miniproject.entity;
import jakarta.persistence.*;

import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;


@Entity
@Table(name="Member")
public class Member {
	@Id
@Column(name = "memberid", length = 20)
private String memberid; // ลบ @GeneratedValue ออก เพราะเราจะเซตเองใน Service

   @Column(name="password", length = 100)
	private String password;

@Column(name="first_name", length = 100)
private String firstname;

@Column(name="last_name", length = 100)
private String lastname;

@Column(name="email", length = 150)
private String email;

@Column(name="phone", length = 10)
private String phone;

// 2. เพิ่ม Annotation ตรงนี้ครับ
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name="birthdate")
    private Date birthdate;

@Column(name="address", length = 255)
private String address;

    @OneToMany(mappedBy = "member")
    private List<Booking> bookings;

	public Member() {
		// TODO Auto-generated constructor stub
	}

	public String getMemberid() {
		return memberid;
	}

	public void setMemberid(String memberid) {
		this.memberid = memberid;
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

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<Booking> getBookings() {
		return bookings;
	}

	public void setBookings(List<Booking> bookings) {
		this.bookings = bookings;
	}

	
	
}
