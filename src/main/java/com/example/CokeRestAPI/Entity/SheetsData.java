package com.example.CokeRestAPI.Entity;

import java.math.BigDecimal;
import java.util.Date;

public class SheetsData {

    private String name;
    private BigDecimal phoneNumber;
    private String productName;
    private String units;
    private int quantity;
    private int price;
    private String dateOrdered;

    private String deliveryDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(BigDecimal phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getDateOrdered() {
        return dateOrdered;
    }

    public void setDateOrdered(String dateOrdered) {
        this.dateOrdered = dateOrdered;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }



    public SheetsData(String name, BigDecimal phoneNumber, String productName, String units, int quantity, int price, String dateOrdered, String deliveryDate) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.productName = productName;
        this.units = units;
        this.quantity = quantity;
        this.price = price;
        this.dateOrdered = dateOrdered;
        this.deliveryDate = deliveryDate;
    }

}
