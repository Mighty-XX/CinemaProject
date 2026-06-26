package cinema.model;

import java.time.LocalDate;

public class Customer {
    private int customerId;
    private String fullName;
    private LocalDate dob;

    public Customer() {}

    public Customer(int customerId, String fullName, LocalDate dob) {
        this.customerId = customerId;
        this.fullName   = fullName;
        this.dob        = dob;
    }

    public int getCustomerId()       { return customerId; }
    public void setCustomerId(int v) { this.customerId = v; }

    public String getFullName()         { return fullName; }
    public void setFullName(String v)   { this.fullName = v; }

    public LocalDate getDob()           { return dob; }
    public void setDob(LocalDate v)     { this.dob = v; }

    @Override public String toString()  { return fullName; }
}
