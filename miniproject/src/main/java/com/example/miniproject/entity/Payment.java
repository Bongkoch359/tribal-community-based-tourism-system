package  com.example.miniproject.entity;
import jakarta.persistence.*;
import com.example.miniproject.entity.enums.PaymentStatus;

import java.sql.Date;

@Entity
@Table(name="Payment")
public class Payment {
	@Id
	 @Column(length = 10)
	private String paymentid;
    private Date paymentdate;
    private Double amount;
	private String paymentslip;
	
	@Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 50)
    private PaymentStatus paymentStatus;
    
   @OneToOne
	@JoinColumn(name = "bookingid", nullable = false, unique = true)
	private Booking booking;

	public enum paymentstatus {
    UNPAID("ยังไม่ชำระเงิน"),
    PAID("ชำระเงินแล้ว");

    private String label;

    paymentstatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
	
	public Payment() {
		// TODO Auto-generated constructor stub
	}

	public String getPaymentid() {
		return paymentid;
	}

	public void setPaymentid(String paymentid) {
		this.paymentid = paymentid;
	}

	public Date getPaymentdate() {
		return paymentdate;
	}

	public void setPaymentdate(Date paymentdate) {
		this.paymentdate = paymentdate;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getPaymentslip() {
		return paymentslip;
	}

	public void setPaymentslip(String paymentslip) {
		this.paymentslip = paymentslip;
	}


	public Booking getBooking() {
		return booking;
	}

	public void setBooking(Booking booking) {
		this.booking = booking;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
	
	
}
