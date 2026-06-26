package cinema.model;

import java.time.LocalDateTime;

public class MovieAccount {
    private int accountId;
    private int customerId;
    private String username;
    private String passwordHash;
    private LocalDateTime registrationDate;
    private String membershipLevel;
    private int rewardPoints;
    private String role;

    public MovieAccount() {
        this.role = "CUSTOMER";
    }

    public MovieAccount(int accountId, int customerId, String username,
                        String passwordHash, String membershipLevel, int rewardPoints) {
        this.accountId       = accountId;
        this.customerId      = customerId;
        this.username        = username;
        this.passwordHash    = passwordHash;
        this.membershipLevel = membershipLevel;
        this.rewardPoints    = rewardPoints;
        this.registrationDate = LocalDateTime.now();
        this.role            = "CUSTOMER";
    }

    public MovieAccount(int accountId, int customerId, String username,
                        String passwordHash, String membershipLevel, int rewardPoints, String role) {
        this.accountId       = accountId;
        this.customerId      = customerId;
        this.username        = username;
        this.passwordHash    = passwordHash;
        this.membershipLevel = membershipLevel;
        this.rewardPoints    = rewardPoints;
        this.registrationDate = LocalDateTime.now();
        this.role            = role;
    }

    public int getAccountId()              { return accountId; }
    public void setAccountId(int v)        { this.accountId = v; }

    public int getCustomerId()             { return customerId; }
    public void setCustomerId(int v)       { this.customerId = v; }

    public String getUsername()            { return username; }
    public void setUsername(String v)      { this.username = v; }

    public String getPasswordHash()        { return passwordHash; }
    public void setPasswordHash(String v)  { this.passwordHash = v; }

    public LocalDateTime getRegistrationDate()          { return registrationDate; }
    public void setRegistrationDate(LocalDateTime v)    { this.registrationDate = v; }

    public String getMembershipLevel()         { return membershipLevel; }
    public void setMembershipLevel(String v)   { this.membershipLevel = v; }

    public int getRewardPoints()           { return rewardPoints; }
    public void setRewardPoints(int v)     { this.rewardPoints = v; }

    public String getRole()                { return role; }
    public void setRole(String v)          { this.role = v; }
    
    public boolean isManager()             { return "MANAGER".equalsIgnoreCase(role); }

    @Override public String toString()     { return username; }
}
