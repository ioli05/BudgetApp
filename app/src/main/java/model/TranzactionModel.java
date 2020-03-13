package model;


public class TranzactionModel {
    String name;
    Double sum;
    String category;

    public TranzactionModel(String name, Double sum, String category) {
        this.name = name;
        this.sum = sum;
        this.category = category;
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

}
