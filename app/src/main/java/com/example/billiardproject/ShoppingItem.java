package com.example.billiardproject;

public class ShoppingItem {
    private String asztal;
    private String idopont;

    public ShoppingItem() {
    }
    public ShoppingItem(String asztal, String idopont) {
        this.asztal = asztal;
        this.idopont = idopont;
    }

    public String getAsztal() {return asztal;}

    public String getIdopont() {return idopont;}
}
