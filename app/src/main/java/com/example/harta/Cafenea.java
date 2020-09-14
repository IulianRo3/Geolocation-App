package com.example.harta;

public class Cafenea {
    String Address;
    Double Latitude;
    Double Longitude;
    String id;
    String name;

    public Cafenea() {
    }

    public Cafenea(String Address, Double Latitude, Double Longitude, String Id, String Name) {
        this.Address = Address;
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.id = Id;
        this.name = Name;
    }

    public String getAddress() {
        return this.Address;
    }

    public void setAddress(String Address) {
        this.Address = Address;
    }

    public Double getLatitude() {
        return this.Latitude;
    }

    public void setLatitude(Double Latitude) {
        this.Latitude = Latitude;
    }

    public Double getLongitude() {
        return this.Longitude;
    }

    public void setLongitude(Double Longitude) {
        this.Longitude = Longitude;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String Id) {
        this.id = Id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String Name) {
        this.name = Name;
    }
}
