package cinema.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Bill {
    private int        billId;
    private int        customerId;
    private int        staffId;
    private LocalDateTime issueDate;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String     paymentType;

    public Bill() {
        this.issueDate      = LocalDateTime.now();
        this.discountAmount = BigDecimal.ZERO;
    }

    public Bill(int billId, int customerId, BigDecimal discountAmount, BigDecimal totalAmount) {
        this();
        this.billId         = billId;
        this.customerId     = customerId;
        this.discountAmount = discountAmount;
        this.totalAmount    = totalAmount;
    }

    public int        getBillId()               { return billId; }
    public void       setBillId(int v)          { this.billId = v; }

    public int        getCustomerId()           { return customerId; }
    public void       setCustomerId(int v)      { this.customerId = v; }

    public int        getStaffId()              { return staffId; }
    public void       setStaffId(int v)         { this.staffId = v; }

    public LocalDateTime getIssueDate()         { return issueDate; }
    public void       setIssueDate(LocalDateTime v){ this.issueDate = v; }

    public BigDecimal getDiscountAmount()       { return discountAmount; }
    public void       setDiscountAmount(BigDecimal v){ this.discountAmount = v; }

    public BigDecimal getTotalAmount()          { return totalAmount; }
    public void       setTotalAmount(BigDecimal v)  { this.totalAmount = v; }

    public String     getPaymentType()          { return paymentType; }
    public void       setPaymentType(String v)  { this.paymentType = v; }
}
