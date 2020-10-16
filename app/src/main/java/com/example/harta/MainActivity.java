package com.example.harta;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.stream.JsonWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 101;
    private AppBarConfiguration mAppBarConfiguration;
    public static ArrayList<Cafenea> cache = new ArrayList<>();
    public static Location currentLocation1;
    public boolean json;
    public boolean fisier;
    public fetchLastLocation fll = new fetchLastLocation();
    public FusedLocationProviderClient fusedLocationProviderClient;
    DatabaseReference databaseCafea = FirebaseDatabase.getInstance().getReference("Cafenea");
    ValueEventListener cacheEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Cafenea cafenea = postSnapshot.getValue(Cafenea.class);
                    assert cafenea != null;

                    try {
                        if (cafenea.getAddress().contains(getCityName(currentLocation1.getLatitude(), currentLocation1.getLongitude()))) {
                            cache.add(cafenea);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };


    private String getCityName(double latitude, double longitude) throws IOException {
        String myCity;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
        myCity = addresses.get(0).getLocality();
        return myCity;
    }


    public void Scriere(String fileName) {
        databaseCafea.addListenerForSingleValueEvent(cacheEventListener);
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            FileOutputStream fos = null;
            try {
                fos = this.openFileOutput(fileName, Context.MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            assert fos != null;
            OutputStreamWriter out = new OutputStreamWriter(fos);
            JsonWriter writer = new JsonWriter(out);
            //set indentation for pretty print
            writer.setIndent("\t");
            //start writing
            try {
                writer.beginObject(); //{
                writer.name("Cafenele").beginArray();
                for (Cafenea cafenea : cache) {

                    writer.beginObject(); //{

                    writer.name("Address").value(cafenea.getAddress()); // "id": 123
                    writer.name("Latitude").value(cafenea.getLatitude()); // "name": "David"
                    writer.name("Longitude").value(cafenea.getLongitude()); // "permanent": false
                    writer.name("id").value(cafenea.getId());
                    writer.name("name").value(cafenea.getName());// "address": {
                    writer.endObject(); // }
                }
                writer.endArray(); // ]
                writer.endObject(); // }
                writer.flush();

                //close writer
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }, 450);
    }

    public void verificareJson() throws IOException {
        FileInputStream fis = this.openFileInput("yourFileName");
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(isr);
        File file = this.getFileStreamPath("yourFileName");
        if (file.exists()) {
            fisier = true;
        }
        if (bufferedReader.readLine() != null) {
            json = true;
        }
        Log.e("Fisier Exista", "" + fisier);
        Log.e("Fisier e scris", "" + json);
    }


    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread(fll).start();
        try {
            verificareJson();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            for(int i =0;i < 600;i++){
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else{
                    i = 600;
                }
            }
        }

        new Thread(fll).start();
        if (!fisier && !json) {
            final Handler handler = new Handler();
            handler.postDelayed(() -> Scriere("yourFileName"), 50);
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.


        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


    }

    class fetchLastLocation implements Runnable {
        @Override
        public void run() {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

            @SuppressLint("MissingPermission") final Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    currentLocation1 = task.getResult();
                }
            }).addOnFailureListener(e -> {
                Log.d("MapDemoActivity", "Error trying to get last GPS location");
                e.printStackTrace();
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


}