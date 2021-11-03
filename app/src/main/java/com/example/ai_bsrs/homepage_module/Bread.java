package com.example.ai_bsrs.homepage_module;
import java.util.Map;

public class Bread {

    public String name, quantity, price, totalPrice, checkOutTime, checkOutDate;
    private Map<String, String> names, quantities, prices;
    private String [] nameArr, qtyArr, priceArr;

    public Bread(String name, String quantity, String price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String[] getNameArr() {
        return nameArr;
    }

    public void setNameArr(String[] nameArr) {
        this.nameArr = nameArr;
    }

    public String[] getQtyArr() {
        return qtyArr;
    }

    public void setQtyArr(String[] qtyArr) {
        this.qtyArr = qtyArr;
    }

    public String[] getPriceArr() {
        return priceArr;
    }

    public void setPriceArr(String[] priceArr) {
        this.priceArr = priceArr;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
