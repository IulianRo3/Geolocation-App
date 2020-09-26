package com.example.harta;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Cafenele implements Serializable {

    private final static long serialVersionUID = -7864391134904354978L;
    @SerializedName("Cafenele")
    @Expose
    private List<Cafenea> cafenele = null;

    public List<Cafenea> getCafenele() {
        return cafenele;
    }

    public void setCafenele(List<Cafenea> cafenele) {
        this.cafenele = cafenele;
    }

    public Cafenele withCafenele(List<Cafenea> cafenele) {
        this.cafenele = cafenele;
        return this;
    }

}