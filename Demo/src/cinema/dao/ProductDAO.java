package cinema.dao;

import cinema.model.Product;
import cinema.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private static final List<Product> DEMO = new ArrayList<>();

    static {
        DEMO.add(new Product(1, "Bắp rang bơ lớn",    "FOOD",  new BigDecimal("55000")));
        DEMO.add(new Product(2, "Bắp rang phô mai",    "FOOD",  new BigDecimal("60000")));
        DEMO.add(new Product(3, "Coca-Cola lớn",       "DRINK", new BigDecimal("45000")));
        DEMO.add(new Product(4, "Pepsi lớn",           "DRINK", new BigDecimal("45000")));
        DEMO.add(new Product(5, "Nước lọc",            "DRINK", new BigDecimal("20000")));
        DEMO.add(new Product(6, "Combo 1 (Bắp + Nước)","COMBO", new BigDecimal("85000")));
        DEMO.add(new Product(7, "Combo 2 (2 Bắp+2 Nước)","COMBO",new BigDecimal("160000")));
        DEMO.add(new Product(8, "Snickers",             "FOOD",  new BigDecimal("30000")));
    }

    public List<Product> getAll() {
        Connection conn = DBConnection.get();
        if (conn == null) return new ArrayList<>(DEMO);
        List<Product> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Product ORDER BY category, productname")) {
            while (rs.next()) {
                list.add(new Product(rs.getInt("productid"), rs.getString("productname"),
                        rs.getString("category"), rs.getBigDecimal("unitprice")));
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] " + e.getMessage());
            return new ArrayList<>(DEMO);
        }
        return list;
    }
}
