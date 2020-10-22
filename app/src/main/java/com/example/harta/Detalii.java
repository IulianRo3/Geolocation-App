package com.example.harta;

import java.util.List;

public class Detalii {

    String id;
    String poza;
    String info1;
    List<String> tag = null;

    public Detalii() {

    }

    public Detalii(String id, String poza, String info1, List<String> tag) {
        this.id = id;
        this.poza = poza;
        this.info1 = info1;
        this.tag = tag;

    }

    public List<String> getTag() {
        return tag;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    public String getInfo1() {
        return info1;
    }

    public void setInfo1(String info1) {
        this.info1 = info1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPoza() {
        return poza;
    }

    public void setPoza(String poza) {
        this.poza = poza;
    }
}
