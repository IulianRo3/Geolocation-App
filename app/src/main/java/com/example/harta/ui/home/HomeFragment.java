package com.example.harta.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.harta.Cafenea;
import com.example.harta.Cafenele;
import com.example.harta.Detalii;
import com.example.harta.MainActivity;
import com.example.harta.MapStateManager;
import com.example.harta.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class HomeFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    private BottomSheetBehavior mBottomSheetBehavior;
    View bottomSheet;

    static ArrayList<Marker> chunk = new ArrayList<>();
    public
    double latitudine;
    static CameraPosition pozitie;
    double longitudine;
    String fileName = "yourFileName";

    boolean json;
    boolean fisier;
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "Update";
    @SuppressLint("StaticFieldLeak")
    static ViewFlipper viewFlipper;
    ArrayList<Cafenea> cache = new ArrayList<>();
    static ArrayList<Marker> mrk = new ArrayList<>();
    static LocationCallback locationCallback;
    static ArrayList<Cafenea> rezultat;
    static Boolean requestingLocationUpdates = true;
    static MapStateManager mgr;
    CameraPosition position;
    // short delay = 150;
    //Variabile globale
    private
    static GoogleMap googleMap;
    Marker srch = null;
    ListView lv;
    TextView negasit;
    MapView mMapView;
    FusedLocationProviderClient fusedLocationProviderClient;
    ListView infoP;
    Button next;

    ValueEventListener CameraEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                //Log.e("Count ", "" + dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Cafenea cafenea = postSnapshot.getValue(Cafenea.class);
                    assert cafenea != null;

                    try {
                        if (cafenea.getAddress().contains(getCityName(pozitie.target.latitude, pozitie.target.longitude))) {
                            chunk.add(googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(cafenea.getLatitude(), cafenea.getLongitude()))
                                    .title(cafenea.getName())
                                    .snippet(cafenea.getAddress())
                                    .icon(BitmapDescriptorFactory.defaultMarker
                                            (BitmapDescriptorFactory.HUE_AZURE))));

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
        Geocoder geocoder = new Geocoder(HomeFragment.this.requireContext(), Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
        myCity = addresses.get(0).getLocality();
        return myCity;
    }

    public void verificareJson() throws IOException {
        FileInputStream fis = HomeFragment.this.requireContext().openFileInput(fileName);
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(isr);
        File file = HomeFragment.this.requireContext().getFileStreamPath(fileName);
        if (file.exists()) {
            fisier = true;
        }
        if (bufferedReader.readLine() != null) {
            json = true;
        }
        Log.e("Fisier Exista", "" + fisier);
        Log.e("Fisier e scris", "" + json);
    }


    public void read_file(@NonNull Context context, String filename, ArrayList<Cafenea> cache) {
        try {
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            Gson g = new Gson();
            Cafenele cafenele = g.fromJson(String.valueOf(sb), Cafenele.class);

            if (!cache.isEmpty()) {
                cache.clear();
            }
            cache.addAll(cafenele.getCafenele());
            Log.e("Marime cache", "" + cache.size());
            //Log.e("Citire", "" + sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            mrk.clear();
            if (dataSnapshot.exists()) {
                //Log.e("Count ", "" + dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Cafenea cafenea = postSnapshot.getValue(Cafenea.class);
                    assert cafenea != null;
                    if (MainActivity.currentLocation1 != null && cafenea.getLatitude() <= MainActivity.currentLocation1.getLatitude() + 0.007 && cafenea.getLatitude() >= MainActivity.currentLocation1.getLatitude() - 0.007) {
                        if (cafenea.getLongitude() <= MainActivity.currentLocation1.getLongitude() + 0.007 && cafenea.getLongitude() >= MainActivity.currentLocation1.getLongitude() - 0.007) {
                            mrk.add(googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(cafenea.getLatitude(), cafenea.getLongitude()))
                                    .title(cafenea.getName())
                                    .snippet(cafenea.getAddress())
                                    .icon(BitmapDescriptorFactory.defaultMarker
                                            (BitmapDescriptorFactory.HUE_AZURE))));
                        }
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };


    DatabaseReference databaseCafea = FirebaseDatabase.getInstance().getReference("Cafenea");


    private void removeAllMarkers(ArrayList<Marker> AllMarkers) {
        for (Marker mLocationMarker : AllMarkers) {
            mLocationMarker.remove();
        }
        AllMarkers.clear();
    }

    SearchView searchView;

    //De aici incepe aplicata.Locul unde se creeaza fragmentul cu toate utilitatile sale(Main)

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.getMapAsync(this);
        Log.e("PIZZA", String.valueOf(MainActivity.currentLocation1));

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(HomeFragment.this.requireContext());
        try {
            verificareJson();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Back back = new Back();
        new Thread(back).start();
        Log.e("Json", "" + json);
        ListenerForSingeAndMGR();
        lv = view.findViewById(R.id.result_list);
        Button center = view.findViewById(R.id.Center);
        center.setOnClickListener(this);
        searchView = view.findViewById(R.id.sv_location);
        updateValuesFromBundle(savedInstanceState);
        searchView.setQueryHint("Cauta ceva:");
        viewFlipper = view.findViewById(R.id.view_flipper);
        Button previous = view.findViewById(R.id.previous);
        previous.setOnClickListener(this);
        next = view.findViewById(R.id.next);
        next.setOnClickListener(this);
        negasit = view.findViewById(R.id.GasitNimic);
        infoP = view.findViewById(R.id.infoP);
        bottomSheet = view.findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //Apelare functii de cautare
        SetOnQuery();

        return view;
    }



    public void ListenerForSingeAndMGR() {
        if (ActivityCompat.checkSelfPermission(HomeFragment.this.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (!json) {
                databaseCafea.addListenerForSingleValueEvent(valueEventListener);
            } else {
                read_file(HomeFragment.this.requireContext(), fileName, cache);
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    for (Cafenea cafenea : cache) {
                        if (MainActivity.currentLocation1 != null && cafenea.getLatitude() <= MainActivity.currentLocation1.getLatitude() + 0.007 && cafenea.getLatitude() >= MainActivity.currentLocation1.getLatitude() - 0.007) {
                            if (cafenea.getLongitude() <= MainActivity.currentLocation1.getLongitude() + 0.007 && cafenea.getLongitude() >= MainActivity.currentLocation1.getLongitude() - 0.007) {
                                mrk.add(googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(cafenea.getLatitude(), cafenea.getLongitude()))
                                        .title(cafenea.getName())
                                        .snippet(cafenea.getAddress())
                                        .icon(BitmapDescriptorFactory.defaultMarker
                                                (BitmapDescriptorFactory.HUE_AZURE))));
                            }
                        }

                        //Log.e("Citire",""+cafenea.getLatitude()+" "+ cafenea.getLongitude());
                    }
                }, 150);
            }

        } else {
            Toast.makeText(HomeFragment.this.requireContext(), "Please enable location access", Toast.LENGTH_SHORT).show();
        }

        mgr = new MapStateManager(HomeFragment.this.requireContext());
        if (mgr.getSavedCameraPosition() != null) {
            position = mgr.getSavedCameraPosition();
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
            }
        };
    }

    public void SetOnQuery() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                rezultat = new ArrayList<>();
                if (MainActivity.currentLocation1 != null) {
                    new FirebaseUserSearch().execute();
                } else {
                    Toast.makeText(HomeFragment.this.requireContext(), "Cannot detect user location", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new Async(HomeFragment.this).execute();
        mMapView.onResume();

        if (requestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    //Locul butoanelor
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previous: {
                viewFlipper.setInAnimation(HomeFragment.this.requireContext(), R.anim.right);
                viewFlipper.setOutAnimation(HomeFragment.this.requireContext(), R.anim.slide_out_left);
                viewFlipper.showPrevious();
                break;
            }
            case R.id.next: {
                viewFlipper.setInAnimation(HomeFragment.this.requireContext(), android.R.anim.slide_in_left);
                viewFlipper.setOutAnimation(HomeFragment.this.requireContext(), android.R.anim.slide_out_right);
                viewFlipper.showNext();
                break;
            }
            case R.id.Center: {
                if (MainActivity.currentLocation1 != null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().
                            target(new LatLng(MainActivity.currentLocation1.getLatitude(), MainActivity.currentLocation1.getLongitude())).
                            tilt(0).
                            zoom(16).
                            bearing(0).
                            build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else {
                    Toast.makeText(HomeFragment.this.getContext(), "Can't find location", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //Face update uri in timp real
    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        /* 60 secs */
        long UPDATE_INTERVAL = 60000;
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        /* 5 secs */
        long FASTEST_INTERVAL = 5000;
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onPause() {
        super.onPause();


        mMapView.onPause();
        MapStateManager mgr = new MapStateManager(HomeFragment.this.requireContext());
        if (googleMap != null) {
            mgr.saveMapState(googleMap);
            stopLocationUpdates();
        }
        removeAllMarkers(chunk);
    }

    //In caz ca se schimba limba/se roteste sa nu se distruga appul.E apelat in main(CreateView)

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            requestingLocationUpdates = savedInstanceState.getBoolean(
                    REQUESTING_LOCATION_UPDATES_KEY);
        }
        UpdateUi();

    }

    public void UpdateUi() {
        if (isVisible()) {
            if (null != MainActivity.currentLocation1) {
                MainActivity.currentLocation1.getLongitude();
                MainActivity.currentLocation1.getLatitude();
            }

        }
    }

    //------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onMapReady(GoogleMap mMap) {
        googleMap = mMap;
        MapRdy mrd = new MapRdy();
        googleMap.setOnMapLongClickListener(latLng -> {
            for (Marker marker : mrk) {
                if (latLng.latitude <= marker.getPosition().latitude + 0.0014 && latLng.latitude >= marker.getPosition().latitude - 0.0001) {
                    if (latLng.longitude <= marker.getPosition().longitude + 0.0003 && latLng.longitude >= marker.getPosition().longitude - 0.0003) {
                        Toast.makeText(HomeFragment.this.requireContext(), "" + marker.getTitle(), Toast.LENGTH_SHORT).show(); //do some stuff
                        break;
                    }

                }
            }

        });
        googleMap.setOnInfoWindowClickListener(marker -> {
                    if (srch != null) {
                        if (marker.getPosition().equals(srch.getPosition())) {
                            try {
                                InfoPanel(marker);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                    } else if (chunk.isEmpty()) {

                        try {
                            InfoPanel(marker);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(HomeFragment.this.requireContext(), "" + marker.getTitle(), Toast.LENGTH_SHORT).show();

                    } else {

                        try {
                            InfoPanel(marker);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
        );
        googleMap.setOnMarkerClickListener(marker -> {

            if (srch != null) {
                if (marker.getPosition().equals(srch.getPosition())) {
                    Toast.makeText(HomeFragment.this.requireContext(), "s-a apasat", Toast.LENGTH_SHORT).show();

                }

            } else if (chunk.isEmpty()) {
                for (Marker marker1 : mrk) {
                    if (marker.getPosition().equals(marker1.getPosition())) {
                        Toast.makeText(HomeFragment.this.requireContext(), "s-a apasat", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }


            } else {
                for (Marker marker1 : chunk) {
                    if (marker.getPosition().equals(marker1.getPosition())) {
                        Toast.makeText(HomeFragment.this.requireContext(), "s-a apasat", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }


            }
            return false;
        });


        new Thread(mrd).start();

        Runnable r = new Runnable() {
            Handler rr = new Handler();

            @Override
            public void run() {
                rr.post(() -> googleMap.setOnCameraMoveListener(() -> {
                    //RAMAS : TREBUIE ORDONATE AMBELE LISTE INAINTE DE COMPARARE
                    if (cache.isEmpty()) {
                        try {
                            verificareJson();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        read_file(HomeFragment.this.requireContext(), fileName, cache);
                    }
                    pozitie = googleMap.getCameraPosition();
                    latitudine = pozitie.target.latitude;
                    longitudine = pozitie.target.longitude;

                    if (pozitie.zoom > 16.0 && chunk.isEmpty()) {
                        Log.e("MARIME CHUNK", "" + chunk.size());
                        if (!json) {
                            databaseCafea.addListenerForSingleValueEvent(CameraEventListener);
                        } else {
                            for (Cafenea cafenea : cache) {
                                chunk.add(mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(cafenea.getLatitude(), cafenea.getLongitude()))
                                        .title(cafenea.getName())
                                        .snippet(cafenea.getAddress())
                                        .icon(BitmapDescriptorFactory.defaultMarker
                                                (BitmapDescriptorFactory.HUE_AZURE))));
                                Log.e("chunk", "" + chunk.get(0).getPosition().longitude);
                            }

                        }


                    }

                    if (pozitie.zoom < 15.0) {
                        removeAllMarkers(chunk);


                    }

                }));
            }
        };
        Thread map = new Thread(r);
        new Thread(map).start();
        final Handler handler = new Handler();

        handler.postDelayed(() -> {

            pozitie = googleMap.getCameraPosition();
            latitudine = pozitie.target.latitude;
            longitudine = pozitie.target.longitude;
            Log.e("POZITIE", "" + pozitie.zoom);
            if (pozitie.zoom > 16.0 && chunk.isEmpty()) {
                Log.e("MARIME CHUNK", "" + chunk.size());
                if (!json) {
                    databaseCafea.addListenerForSingleValueEvent(CameraEventListener);
                } else {
                    for (Cafenea cafenea : cache) {
                        chunk.add(mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(cafenea.getLatitude(), cafenea.getLongitude()))
                                .title(cafenea.getName())
                                .snippet(cafenea.getAddress())
                                .icon(BitmapDescriptorFactory.defaultMarker
                                        (BitmapDescriptorFactory.HUE_AZURE))));
                        Log.e("chunk", "" + chunk.get(0).getPosition().longitude);
                    }

                }


            }
        }, 175);
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates);

        super.onSaveInstanceState(savedInstanceState);
    }
//70 ms

    static class Async extends AsyncTask<Void, Void, Void> {
        private final WeakReference<HomeFragment> wk;
        FusedLocationProviderClient fusedLocationProviderClient;
        Location currentLocation;
        CameraPosition position;
        int delay = 50;

        public Async(HomeFragment context) {
            this.wk = new WeakReference<>(context);
        }

        public int setDelay(int delay) {
            if (currentLocation == null) {
                delay += 20;
                final Handler handler = new Handler();
                int finalDelay = delay;
                handler.postDelayed(() -> setDelay(finalDelay), 0);
            } else {
                delay = 0;
            }
            return delay;
        }

        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Void... voids) {
            HomeFragment activity = wk.get();
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity.requireContext());

            @SuppressLint("MissingPermission") final Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    currentLocation = task.getResult();
                }
            }).addOnFailureListener(e -> {
                Log.d("MapDemoActivity", "Error trying to get last GPS location");
                e.printStackTrace();
            });

            mgr = new MapStateManager(activity.requireContext());
            if (mgr.getSavedCameraPosition() != null) {
                position = mgr.getSavedCameraPosition();

            }

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                }
            };
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            HomeFragment activity = wk.get();
            Handler ol = new Handler();
            ol.postDelayed(() -> {
                if (currentLocation != null && mgr.getSavedCameraPosition() != null && mgr.getSavedCameraPosition().target.latitude <= currentLocation.getLatitude() + 0.25 && mgr.getSavedCameraPosition().target.latitude >= currentLocation.getLatitude() - 0.25 && mgr.getSavedCameraPosition().target.longitude <= currentLocation.getLongitude() + 0.25 && mgr.getSavedCameraPosition().target.longitude >= currentLocation.getLongitude() - 0.25 && position != null) {
                    CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
                    googleMap.moveCamera(update);
                    googleMap.setMapType(mgr.getSavedMapType());
                } else if (currentLocation != null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()), 15));
                } else {
                    Toast.makeText(activity.requireContext(), "S-a miscat prea incet", Toast.LENGTH_SHORT).show();
                }
            }, setDelay(delay));
        }

    }


    class MapRdy implements Runnable {
        Handler mr = new Handler();

        @Override
        public void run() {
            mr.post(() -> {
                // For showing a move to my location button
                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                if (ActivityCompat.checkSelfPermission(HomeFragment.this.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                    new Async(HomeFragment.this).execute();
                } else {
                    Toast.makeText(HomeFragment.this.requireContext(), "Please enable location access", Toast.LENGTH_SHORT).show();
                }
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            });
        }
    }



    @SuppressLint("StaticFieldLeak")
    class FirebaseUserSearch extends AsyncTask<Void, Void, Void> {
        Handler search = new Handler();

        private String getCityName(double latitude, double longitude) throws IOException {
            String myCity;
            Geocoder geocoder = new Geocoder(HomeFragment.this.requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                myCity = addresses.get(0).getLocality();
                return myCity;
            } else {
                return null;
            }

        }

        private void firebaseUserSearch(final String searchText, final Location currentLocation, DatabaseReference databaseCafea) throws IOException {
            if (cache.isEmpty()) {
                verificareJson();
                read_file(HomeFragment.this.requireContext(), fileName, cache);
            }

            final ArrayList<Cafenea> cautare = new ArrayList<>();
            if (!json) {
                Log.e("Baza de date", "Se cauta");
                Query query = databaseCafea.orderByChild("name");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                try {
                                    new Cafenea();
                                    Cafenea caf;
                                    caf = issue.getValue(Cafenea.class);
                                    assert caf != null;
                                    if (getCityName(latitudine, longitudine) == null) {
                                        if (latitudine == 0 && caf.getAddress().contains(Objects.requireNonNull(getCityName(currentLocation.getLatitude(), currentLocation.getLongitude())))) {
                                            if (cautare.size() <= 30) {
                                                cautare.add(issue.getValue(Cafenea.class));
                                                if (cautare.size() == 30) {
                                                    for (Cafenea cafenea : cautare) {
                                                        if (cafenea.getName().toLowerCase().contains(searchText)) {
                                                            rezultat.add(cafenea);
                                                        }
                                                    }
                                                    cautare.clear();
                                                }
                                            }
                                        }

                                    } else if (caf.getAddress().contains(Objects.requireNonNull(getCityName(latitudine, longitudine)))) {
                                        if (cautare.size() <= 30) {
                                            cautare.add(issue.getValue(Cafenea.class));
                                            if (cautare.size() == 30) {
                                                for (Cafenea cafenea : cautare) {
                                                    if (cafenea.getName().toLowerCase().contains(searchText)) {
                                                        rezultat.add(cafenea);
                                                    }
                                                }
                                                cautare.clear();
                                            }
                                        }
                                    } else {
                                        Toast.makeText(HomeFragment.this.requireContext(), "Get closer I can't see", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            cautare.clear();
                        }
                        if (rezultat.isEmpty()) {
                            negasit.setVisibility(View.VISIBLE);

                        } else {
                            negasit.setVisibility(View.GONE);
                        }
                        UsersAdapter   adapter = new UsersAdapter(HomeFragment.this.requireContext(), rezultat);
                        lv.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            } else {
                read_file(HomeFragment.this.requireContext(), fileName, cache);
                Log.e("cache", "" + cache.size());
                for (Cafenea caf : cache) {
                    if (getCityName(latitudine, longitudine) == null) {
                        if (latitudine == 0 && caf.getAddress().contains(Objects.requireNonNull(getCityName(currentLocation.getLatitude(), currentLocation.getLongitude())))) {
                            if (caf.getName().toLowerCase().contains(searchText)) {
                                rezultat.add(caf);
                            }
                        }
                    } else if (caf.getAddress().contains(Objects.requireNonNull(getCityName(latitudine, longitudine)))) {
                        if (caf.getName().toLowerCase().contains(searchText)) {
                            rezultat.add(caf);
                        }
                    }

                }
                UsersAdapter adapter = new UsersAdapter(HomeFragment.this.requireContext(), rezultat);
                lv.setAdapter(adapter);

                if (getCityName(latitudine, longitudine) != null && !cache.get(0).getAddress().contains(Objects.requireNonNull(getCityName(latitudine, longitudine)))) {

                    Query query = databaseCafea.orderByChild("name");
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                    try {
                                        new Cafenea();
                                        Cafenea caf;
                                        caf = issue.getValue(Cafenea.class);
                                        assert caf != null;
                                        if (caf.getAddress().contains(Objects.requireNonNull(getCityName(latitudine, longitudine)))) {
                                            if (cautare.size() <= 30) {
                                                cautare.add(issue.getValue(Cafenea.class));
                                                if (cautare.size() == 30) {
                                                    for (Cafenea cafenea : cautare) {
                                                        if (cafenea.getName().toLowerCase().contains(searchText)) {
                                                            rezultat.add(cafenea);
                                                        }
                                                    }
                                                    if (rezultat.isEmpty()) {
                                                        negasit.setVisibility(View.VISIBLE);
                                                    } else {
                                                        negasit.setVisibility(View.GONE);
                                                    }
                                                    UsersAdapter adapter = new UsersAdapter(HomeFragment.this.requireContext(), rezultat);
                                                    lv.setAdapter(adapter);
                                                    cautare.clear();
                                                }
                                            }
                                        } else {
                                            Toast.makeText(HomeFragment.this.requireContext(), "Get closer I can't see", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                cautare.clear();
                            }

                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }else if(rezultat.isEmpty()){
                    negasit.setVisibility(View.VISIBLE);
                }else {
                    negasit.setVisibility(View.GONE);
                }


            }
            Log.e("Rezultat", "" + rezultat.isEmpty());

            cautare.clear();


        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                getCityName(MainActivity.currentLocation1.getLatitude(), MainActivity.currentLocation1.getLongitude());
            } catch (IOException e) {
                e.printStackTrace();
            }
            search.post(() -> {
                String location = searchView.getQuery().toString().toLowerCase();
                try {
                    firebaseUserSearch(location, MainActivity.currentLocation1, databaseCafea);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return null;
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    //OnBackPressedCallBack - setare functie buton de back
    class Back implements Runnable {

        Handler sh = new Handler();

        @Override
        public void run() {
            sh.post(() -> {
                OnBackPressedCallback callback = new OnBackPressedCallback(
                        true // default to enabled
                ) {
                    @Override
                    public void handleOnBackPressed() {
                        switch (mBottomSheetBehavior.getState()) {
                            case BottomSheetBehavior.STATE_EXPANDED:
                                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                break;
                            case BottomSheetBehavior.STATE_COLLAPSED:
                                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                                if (srch != null) {
                                    srch.remove();
                                    srch = null;
                                }
                                break;
                            case BottomSheetBehavior.STATE_HIDDEN:
                                if (srch != null) {
                                    srch.remove();
                                    srch = null;
                                } else {
                                    viewFlipper.showPrevious();
                                }
                                break;
                            case BottomSheetBehavior.STATE_DRAGGING:
                            case BottomSheetBehavior.STATE_HALF_EXPANDED:
                            case BottomSheetBehavior.STATE_SETTLING:
                                break;
                        }
                    }
                };
                requireActivity().getOnBackPressedDispatcher().addCallback(
                        getViewLifecycleOwner(), // LifecycleOwner
                        callback);
            });
        }
    }


//--------------------------------------------------------------------------------------------------------------------

    //Format afisare rezultate cautare
    public class UsersAdapter extends ArrayAdapter<Cafenea> {
        public UsersAdapter(@NonNull Context context, ArrayList<Cafenea> users) {
            super(context, 0, users);
        }
        @NonNull
        @Override
        //Creare butoane si elemente de afisare
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            final Cafenea user = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_layout, parent, false);
            }
            // Lookup view for data population
            final View finalConvertView = convertView;
            final TextView user_name = finalConvertView.findViewById(R.id.nume_text);
            final TextView adress = finalConvertView.findViewById(R.id.adresa_text);
            Button Locatie = finalConvertView.findViewById(R.id.buttonLocatie);
            Button Detalii = finalConvertView.findViewById(R.id.buttonDetalii);
            assert user != null;
            user_name.setText(user.getName());
            adress.setText(user.getAddress());

            Locatie.setOnClickListener(view -> {
                if (srch != null) {
                    srch.remove();
                }
                viewFlipper.setOutAnimation(HomeFragment.this.requireContext(), R.anim.slide_out_left);
                viewFlipper.showPrevious();
                for (Cafenea cafenea : rezultat) {
                    if (user_name.getText().equals(cafenea.getName()) && adress.getText().equals(cafenea.getAddress())) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(cafenea.getLatitude(),
                                        cafenea.getLongitude()), 20));
                        srch = googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(cafenea.getLatitude(), cafenea.getLongitude()))
                                .title(cafenea.getName())
                                .snippet(cafenea.getAddress())
                                .icon(BitmapDescriptorFactory.defaultMarker
                                        (BitmapDescriptorFactory.HUE_AZURE)));
                    }
                }
            });
            Detalii.setOnClickListener(view->{
                if (srch != null) {
                    srch.remove();
                }
                next.setVisibility(View.GONE);
                for (Cafenea cafenea : rezultat) {
                    if (user_name.getText().equals(cafenea.getName()) && adress.getText().equals(cafenea.getAddress())) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(cafenea.getLatitude(),
                                        cafenea.getLongitude()), 20));
                        srch = googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(cafenea.getLatitude(), cafenea.getLongitude()))
                                .title(cafenea.getName())
                                .snippet(cafenea.getAddress())
                                .icon(BitmapDescriptorFactory.defaultMarker
                                        (BitmapDescriptorFactory.HUE_AZURE)));
                    }
                }
                try {
                    InfoPanel(srch);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                viewFlipper.setOutAnimation(HomeFragment.this.requireContext(), R.anim.slide_out_left);
                viewFlipper.showPrevious();
            });
            // Return the completed view to render on screen
            return convertView;
        }
    }
public void InfoPanel(Marker marker) throws IOException {
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    LinkedList<Cafenea> info = new LinkedList<>();
    if (cache.isEmpty()) {
        verificareJson();
        read_file(HomeFragment.this.requireContext(), fileName, cache);
    }
    for (Cafenea cafenea1 : cache) {
        if (marker.getPosition().latitude == cafenea1.getLatitude() && marker.getPosition().longitude == cafenea1.getLongitude()) {
            info.add(cafenea1);
            Log.e("PIZZAaasd", "RRRRRRRR" + marker.getTitle());
            break;
        }
    }
    UsersAdapter2  adapter = new UsersAdapter2(HomeFragment.this.requireContext(), info);
    infoP.setAdapter(adapter);

}

    //Format afisare rezultate cautare
    public class UsersAdapter2 extends ArrayAdapter<Cafenea> {
        public Detalii detaliu;
        String ID;
        ValueEventListener DetaliiEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Log.e("Count ", "" + dataSnapshot.getChildrenCount());
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Detalii detalii = postSnapshot.getValue(Detalii.class);

                        assert detalii != null;

                        if (detalii.getId().equals(ID)) {
                            detaliu = new Detalii(detalii.getId(), detalii.getPoza(), detalii.getInfo1(), detalii.getTag());

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        DatabaseReference databaseDetalii = FirebaseDatabase.getInstance().getReference("Detalii");

        public void AfisarePoze(Cafenea cafenea, View view) {
            ID = cafenea.getId();
            databaseDetalii.addListenerForSingleValueEvent(DetaliiEventListener);
            ImageView imageView = view.findViewById(R.id.imageView3);
            TextView info_mic = view.findViewById(R.id.textView10);
            TextView tag1 = view.findViewById(R.id.textView7);
            TextView tag2 = view.findViewById(R.id.textView8);
            TextView tag3 = view.findViewById(R.id.textView9);
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                Glide.with(HomeFragment.this.requireContext())
                        .load(detaliu.getPoza())
                        .into(imageView);
                info_mic.setText(detaliu.getInfo1());
                tag1.setText(detaliu.getTag().get(0));
                tag2.setText(detaliu.getTag().get(1));
                tag3.setText(detaliu.getTag().get(2));
            }, 200);


        }

        public UsersAdapter2(@NonNull Context context, LinkedList<Cafenea> users) {
            super(context, 0, users);
        }

        @NonNull
        @Override
        //Creare butoane si elemente de afisare
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            final Cafenea user = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.infopanel, parent, false);
            }
            // Lookup view for data population
            final View finalConvertView = convertView;
            final TextView info = finalConvertView.findViewById(R.id.textView2);
            assert user != null;
            info.setText(user.getName());
            AfisarePoze(user, convertView);



            // Return the completed view to render on screen
            return convertView;
        }
    }
}

//ZONA COMENTARII:
/* @Override
            public void run() {
                Iterator<Cafenea> iter
                        = chunk.iterator();
                Iterator<Marker> etcetera
                        = cache.iterator();
                rr.post(() -> googleMap.setOnCameraMoveListener(() -> {
                        RAMAS : TREBUIE ORDONATE AMBELE LISTE INAINTE DE COMPARARE
                        pozitie = googleMap.getCameraPosition();
                        databaseCafea.addListenerForSingleValueEvent(CameraEventListener);
                        if (pozitie.zoom > 18.0 && pozitie.zoom < 20) {
        if (!cache.isEmpty()) {
        if (cache.size() < chunk.size()) {
        for (int i = 0; i < chunk.size(); i++) {
        if (i < cache.size()) {
        if (!cache.get(i).getSnippet().equals(chunk.get(i).getAddress())) {
        cache.get(i).setPosition(new LatLng(chunk.get(i).getLatitude(), chunk.get(i).getLongitude()));
        cache.get(i).setTitle(chunk.get(i).getName());
        cache.get(i).setSnippet(chunk.get(i).getAddress());
        }
        }
        if (i > cache.size()) {
        cache.add(googleMap.addMarker(new MarkerOptions()
        .position(new LatLng(chunk.get(i).getLatitude(), chunk.get(i).getLongitude()))
        .title(chunk.get(i).getName())
        .snippet(chunk.get(i).getAddress())
        .icon(BitmapDescriptorFactory.defaultMarker
        (BitmapDescriptorFactory.HUE_AZURE))));
        }

        }
        } else if (cache.size() > chunk.size() && chunk.size() != 0) {
       /* for (int y = 0; y < cache.size(); y++) {
        if (y < chunk.size()) { //CHUNK.GET(Y) DA EROAREA INDEX:0, SIZE:0
        if (!cache.get(y).getSnippet().equals(chunk.get(y).getAddress())) {
        cache.get(y).setPosition(new LatLng(chunk.get(y).getLatitude(), chunk.get(y).getLongitude()));
        cache.get(y).setTitle(chunk.get(y).getName());
        cache.get(y).setSnippet(chunk.get(y).getAddress());
        }

        } else {
        cache.get(y).remove();
        }
        }
        }
        } else {
        for (int i = 0; i < chunk.size(); i++) {
        cache.add(googleMap.addMarker(new MarkerOptions()
        .position(new LatLng(chunk.get(i).getLatitude(), chunk.get(i).getLongitude()))
        .title(chunk.get(i).getName())
        .snippet(chunk.get(i).getAddress())
        .icon(BitmapDescriptorFactory.defaultMarker
        (BitmapDescriptorFactory.HUE_AZURE))));
        }

        }
        if (!chunk.isEmpty()) {
        chunk.clear();
        }
        } else if (pozitie.zoom < 16.0) {
        removeAllMarkers(cache);
        cache.clear();
        }
        }));
        }
        };
        Thread map = new Thread(r);
        new Thread(map).start();
        }
        */
/* googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(currentLocation.getLatitude(),
                        currentLocation.getLongitude()), 15));*/
/*
public CameraPosition savePos(){
    final CameraPosition[] test = new CameraPosition[1];
    googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
        @Override
        public void onCameraMove() {


           test[0] = googleMap.getCameraPosition();

        }
    });
    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(test[0]));

    return test[0];
}
public void SetPos(CameraPosition LastViewed){
        LastViewed = savePos();

}
 */





