package com.example.tony.domestique;

public class Shedules {

    private String date;
    private String totalCost;

    public Shedules(String date, String totalCost) {
        this.date = date;
        this.totalCost = totalCost;
    }

    public Shedules(){

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }
}
