package com.example.ai_bsrs.transaction_module;

import java.util.List;

public class Transaction {

    private String KEY_BREAD_TOTAL_PRICE, KEY_BREAD_DATE_CHECKOUT, KEY_BREAD_TIME_CHECKOUT, transactionId;

    private String breadPriceEach, breadName, breadPrice, breadQty, KEY_IMAGE, KEY_IMAGE_RESULT, KEY_STATUS;

    //private String [] KEY_BREAD_NAME, KEY_BREAD_PRICE, KEY_BREAD_QUANTITY;

    private List<String> KEY_BREAD_NAME, KEY_BREAD_PRICE, KEY_BREAD_QUANTITY;

    private long KEY_TIMESTAMP;



    public Transaction() {
    }

    public Transaction(String transactionId, List<String> breadName, List<String> breadPrice,
                       List<String> breadQty, String totalPrice, String dateCheckOut,
                       String timeCheckOut, String KEY_IMAGE, String KEY_IMAGE_RESULT, String KEY_STATUS, long KEY_TIMESTAMP) {
        this.transactionId = transactionId;
        this.KEY_BREAD_TOTAL_PRICE = totalPrice;
        this.KEY_BREAD_DATE_CHECKOUT = dateCheckOut;
        this.KEY_BREAD_TIME_CHECKOUT = timeCheckOut;
        this.KEY_BREAD_NAME = breadName;
        this.KEY_BREAD_PRICE = breadPrice;
        this.KEY_BREAD_QUANTITY = breadQty;
        this.KEY_IMAGE = KEY_IMAGE;
        this.KEY_IMAGE_RESULT = KEY_IMAGE_RESULT;
        this.KEY_STATUS = KEY_STATUS;
        this.KEY_TIMESTAMP = KEY_TIMESTAMP;
    }

    public Transaction(List<String> KEY_BREAD_NAME, List<String> KEY_BREAD_PRICE, List<String> KEY_BREAD_QUANTITY) {
        this.KEY_BREAD_NAME = KEY_BREAD_NAME;
        this.KEY_BREAD_PRICE = KEY_BREAD_PRICE;
        this.KEY_BREAD_QUANTITY = KEY_BREAD_QUANTITY;
    }

    public Transaction( String breadName, String breadPriceEach, String breadPrice, String breadQty) {
        this.breadPriceEach = breadPriceEach;
        this.breadName = breadName;
        this.breadPrice = breadPrice;
        this.breadQty = breadQty;
    }

    public long getKEY_TIMESTAMP() {
        return KEY_TIMESTAMP;
    }

    public void setKEY_TIMESTAMP(long KEY_TIMESTAMP) {
        this.KEY_TIMESTAMP = KEY_TIMESTAMP;
    }

    public String getKEY_STATUS() {
        return KEY_STATUS;
    }

    public void setKEY_STATUS(String KEY_STATUS) {
        this.KEY_STATUS = KEY_STATUS;
    }

    public String getKEY_IMAGE() {
        return KEY_IMAGE;
    }

    public void setKEY_IMAGE(String KEY_IMAGE) {
        this.KEY_IMAGE = KEY_IMAGE;
    }

    public String getKEY_IMAGE_RESULT() {
        return KEY_IMAGE_RESULT;
    }

    public void setKEY_IMAGE_RESULT(String KEY_IMAGE_RESULT) {
        this.KEY_IMAGE_RESULT = KEY_IMAGE_RESULT;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<String> getKEY_BREAD_NAME() {
        return KEY_BREAD_NAME;
    }

    public void setKEY_BREAD_NAME(List<String> KEY_BREAD_NAME) {
        this.KEY_BREAD_NAME = KEY_BREAD_NAME;
    }

    public String getBreadPriceEach() {
        return breadPriceEach;
    }

    public void setBreadPriceEach(String breadPriceEach) {
        this.breadPriceEach = breadPriceEach;
    }

    public String getBreadName() {
        return breadName;
    }

    public void setBreadName(String breadName) {
        this.breadName = breadName;
    }

    public String getBreadPrice() {
        return breadPrice;
    }

    public void setBreadPrice(String breadPrice) {
        this.breadPrice = breadPrice;
    }

    public String getBreadQty() {
        return breadQty;
    }

    public void setBreadQty(String breadQty) {
        this.breadQty = breadQty;
    }

    public List<String> getKEY_BREAD_PRICE() {
        return KEY_BREAD_PRICE;
    }

    public void setKEY_BREAD_PRICE(List<String> KEY_BREAD_PRICE) {
        this.KEY_BREAD_PRICE = KEY_BREAD_PRICE;
    }

    public List<String> getKEY_BREAD_QUANTITY() {
        return KEY_BREAD_QUANTITY;
    }

    public void setKEY_BREAD_QUANTITY(List<String> KEY_BREAD_QUANTITY) {
        this.KEY_BREAD_QUANTITY = KEY_BREAD_QUANTITY;
    }

    public String getKEY_BREAD_TOTAL_PRICE() {
        return KEY_BREAD_TOTAL_PRICE;
    }

    public void setKEY_BREAD_TOTAL_PRICE(String KEY_BREAD_TOTAL_PRICE) {
        this.KEY_BREAD_TOTAL_PRICE = KEY_BREAD_TOTAL_PRICE;
    }

    public String getKEY_BREAD_DATE_CHECKOUT() {
        return KEY_BREAD_DATE_CHECKOUT;
    }

    public void setKEY_BREAD_DATE_CHECKOUT(String KEY_BREAD_DATE_CHECKOUT) {
        this.KEY_BREAD_DATE_CHECKOUT = KEY_BREAD_DATE_CHECKOUT;
    }

    public String getKEY_BREAD_TIME_CHECKOUT() {
        return KEY_BREAD_TIME_CHECKOUT;
    }

    public void setKEY_BREAD_TIME_CHECKOUT(String KEY_BREAD_TIME_CHECKOUT) {
        this.KEY_BREAD_TIME_CHECKOUT = KEY_BREAD_TIME_CHECKOUT;
    }
}
