package  com.example.miniproject.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name="Homestayowner")
public class Homestayowner {

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ownerid",length = 10)
    private int ownerid;
    @Column(name="password", length = 50)
	private String password;

	@Column(name="firstname", length = 100)
	private String firstname;

	@Column(name="lastname", length = 100)
	private String lastname;

	@Column(name="email", length = 150)
	private String email;

	@Column(name="phone", length = 10)
	private String phone;

	@Column(name = "bank_name", length = 100)
	private String bankName;

	@Column(name = "account_name", length = 200)
	private String accountName;

	@Column(name = "account_number", length = 30)
	private String accountNumber;

	@Column(name = "bank_branch", length = 100)
	private String bankBranch;

	@Column(length = 50)
	private  Boolean verificationstatus;

	private String accountstatus;
	// @Enumerated(EnumType.STRING)
	// private HomestayownerStatus accountstatus;

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

	public String getBankName() {
    return bankName;
}

	public void setBankName(String bankName) {
    	this.bankName = bankName;
	}

	public String getAccountName() {
    	return accountName;
	}

	public void setAccountName(String accountName) {
    	this.accountName = accountName;
	}

	public String getAccountNumber() {
    	return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
    	this.accountNumber = accountNumber;
	}

	public String getBankBranch() {
    	return bankBranch;
	}

	public void setBankBranch(String bankBranch) {
    	this.bankBranch = bankBranch;
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
	public String getAccountstatus() {
		return accountstatus;
	}
	public void setAccountstatus(String accountstatus) {
		this.accountstatus = accountstatus;
	}
	
	
	

}
