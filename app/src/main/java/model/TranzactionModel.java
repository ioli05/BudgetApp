package model;


import java.util.Date;

public class TranzactionModel {
    String name;
    Double sum;
    String category;
    Date date;

    public TranzactionModel(String name, Double sum, String category, Date date) {
        this.name = name;
        this.sum = sum;
        this.category = category;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
