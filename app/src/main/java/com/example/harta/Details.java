package com.example.harta;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Details implements Serializable {

    private final static long serialVersionUID = -5999831236905356830L;
    @SerializedName("Detalii")
    @Expose
    private List<Detalii> detalii = null;

    /**
     * No args constructor for use in serialization
     */
    public Details() {
    }

    /**
     * @param detalii
     */
    public Details(List<Detalii> detalii) {
        super();
        this.detalii = detalii;
    }

    public List<Detalii> getDetalii() {
        return detalii;
    }

    public void setDetalii(List<Detalii> detalii) {
        this.detalii = detalii;
    }

    public Details withDetalii(List<Detalii> detalii) {
        this.detalii = detalii;
        return this;
    }

}
