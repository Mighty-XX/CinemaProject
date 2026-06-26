package cinema.model;

import java.math.BigDecimal;

public class Product {
    private int        productId;
    private String     productName;
    private String     category;     // FOOD, DRINK, COMBO
    private BigDecimal unitPrice;

    public Product() {}

    public Product(int productId, String productName, String category, BigDecimal unitPrice) {
        this.productId   = productId;
        this.productName = productName;
        this.category    = category;
        this.unitPrice   = unitPrice;
    }

    public int        getProductId()            { return productId; }
    public void       setProductId(int v)       { this.productId = v; }

    public String     getProductName()          { return productName; }
    public void       setProductName(String v)  { this.productName = v; }

    public String     getCategory()             { return category; }
    public void       setCategory(String v)     { this.category = v; }

    public BigDecimal getUnitPrice()            { return unitPrice; }
    public void       setUnitPrice(BigDecimal v){ this.unitPrice = v; }

    @Override public String toString()          { return productName; }
}
