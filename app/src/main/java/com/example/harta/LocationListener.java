package com.example.harta;

import android.location.Location;
import android.os.Bundle;

public class LocationListener implements android.location.LocationListener {
    public static Location location;

    @Override
    public void onLocationChanged(Location location) {
        LocationListener.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
