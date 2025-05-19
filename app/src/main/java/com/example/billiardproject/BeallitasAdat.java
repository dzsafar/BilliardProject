package com.example.billiardproject;

public class BeallitasAdat {
    private String id;
    private String asztal;
    private String idopont;

    public BeallitasAdat() {
    }

    public BeallitasAdat(String asztal, String idopont) {
        this.asztal = asztal;
        this.idopont = idopont;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAsztal() {
        return asztal;
    }

    public void setAsztal(String asztal) {
        this.asztal = asztal;
    }

    public String getIdopont() {
        return idopont;
    }

    public void setIdopont(String idopont) {
        this.idopont = idopont;
    }
}