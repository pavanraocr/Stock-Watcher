package com.example.pavan.stock_monitor;

/**
 * Created by pavan on 3/6/17.
 */

public class Stocks {
    private String companySym;
    private String companyName;
    private double stockPrice;
    private double stockChangePrice;
    private double stockChangePercentage;
    private String percentageChangeIndecator;

    public Stocks() {
    }

    public Stocks(String companySym, double stockPrice, String percentageChangeIndecator, double stockChangePrice, double stockChangePercentage) {
        this.companySym = companySym;
        this.companyName = companySym;
        this.stockPrice = stockPrice;
        this.percentageChangeIndecator = percentageChangeIndecator;
        this.stockChangePrice = stockChangePrice;
        this.stockChangePercentage = stockChangePercentage;
    }

    /*-----Getters and Setters----------*/

    public String getCompanySym() {
        return companySym;
    }

    public void setCompanySym(String companySym) {
        this.companySym = companySym;
    }

    public String getStockPriceString() {
        return String.valueOf(stockPrice);
    }

    public void setStockPrice(double stockPrice) {
        this.stockPrice = stockPrice;
    }

    public String getStockChangeString() {
        return String.valueOf(stockChangePrice) + " (" + String.valueOf(stockChangePercentage) + "%)";
    }

    public double getStockChangePrice() {
        return stockChangePrice;
    }

    public void setStockChangePrice(double stockChangePrice) {
        this.stockChangePrice = stockChangePrice;
    }

    public void setStockChangePercentage(double stockChangePercentage) {
        this.stockChangePercentage = stockChangePercentage;
    }

    public String getPercentageChangeIndecator() {
        return percentageChangeIndecator;
    }

    public void setPercentageChangeIndecator(String percentageChangeIndecator) {
        this.percentageChangeIndecator = percentageChangeIndecator;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String serializeToNameListItemFormat(){
        return companySym + "-" + companyName;
    }

}
