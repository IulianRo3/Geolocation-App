package com.example.harta.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.harta.Cafenea;
import com.example.harta.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    @SuppressLint("StaticFieldLeak")
    static ViewFlipper viewFlipper;
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "Update";
    public ArrayList<Marker> mrk;
    public ArrayList<Cafenea> rezultat;
    MapView mMapView;
    private GoogleMap googleMap;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    private LocationCallback locationCallback;
    Boolean requestingLocationUpdates = true;
    DatabaseReference databaseCafea;


    ListView lv;


    private String getCityName(double latitude, double longitude) throws IOException {
        String myCity;
        Geocoder geocoder = new Geocoder(HomeFragment.this.requireContext(), Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
        myCity = addresses.get(0).getLocality();

        return myCity;
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mrk.clear();
            if (dataSnapshot.exists()) {
                Log.e("Count ", "" + dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Cafenea cafenea = postSnapshot.getValue(Cafenea.class);
                    assert cafenea != null;
                    if (cafenea.getLatitude() <= currentLocation.getLatitude() + 0.007 && cafenea.getLatitude() >= currentLocation.getLatitude() - 0.007) {
                        if (cafenea.getLongitude() <= currentLocation.getLongitude() + 0.007 && cafenea.getLongitude() >= currentLocation.getLongitude() - 0.007) {
                            mrk.add(googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(cafenea.getLatitude(), cafenea.getLongitude()))
                                    .title(cafenea.getName())
                                    .snippet(cafenea.getAddress())
                                    .icon(BitmapDescriptorFactory.defaultMarker
                                            (BitmapDescriptorFactory.HUE_AZURE))));
                        }
                    }


                }
            }  // Log.e("ACCESARE ", "NU A MERS");

        }


        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void firebaseUserSearch(final String searchText, final Location currentLocation, DatabaseReference databaseCafea) {


        final ArrayList<Cafenea> cautare = new ArrayList<>();
        Query query = databaseCafea.orderByChild("name");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {

                        try {
                            new Cafenea();
                            Cafenea caf;
                            caf = issue.getValue(Cafenea.class);
                            assert caf != null;
                            if (getCityName(caf.getLatitude(), caf.getLongitude()).equals(getCityName(currentLocation.getLatitude(), currentLocation.getLongitude()))) {


                                if (cautare.size() <= 30) {
                                    cautare.add(issue.getValue(Cafenea.class));

                                    //Log.e("ceva",""+cautare.size());
                                    if (cautare.size() == 30) {

                                        for (int i = 0; i < cautare.size(); i++) {
                                            if (cautare.get(i).getName().toLowerCase().contains(searchText)) {
                                                rezultat.add(cautare.get(i));
                                                //Log.e("ceva",""+rezultat.size());


                                            }
                                        }
                                        cautare.clear();
                                    }


                                }

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }

                    // Log.e("ceva",""+rezultat.size());
                    //Toast.makeText(HomeFragment.this.requireContext(),rezultat.get(0).getName(),Toast.LENGTH_LONG).show();
                    cautare.clear();

                }
                UsersAdapter adapter = new UsersAdapter(HomeFragment.this.requireContext(), rezultat);
                lv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });











       /* endAt(searchText + "\uf8ff");
        FirebaseListOptions<Cafenea> options =
                new FirebaseListOptions.Builder<Cafenea>()
                        .setLayout(R.layout.list_layout)
                        .setQuery(query, Cafenea.class)
                        .setLifecycleOwner(this)
                        .build();

        myAdapter = new FirebaseListAdapter<Cafenea>(options) {
            @Override
            protected void populateView(@NonNull View v, @NonNull Cafenea model, int position) {
                TextView user_name = (TextView) v.findViewById(R.id.nume_text);
                TextView adress = (TextView) v.findViewById(R.id.adresa_text);


                user_name.setText(model.getName());
                adress.setText(model.getAddress());

            }

        };

        */
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        databaseCafea = FirebaseDatabase.getInstance().getReference("Cafenea");
        mrk = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_home, container, false);


        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(HomeFragment.this.requireContext());
        lv = view.findViewById(R.id.result_list);


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
            }
        };

        mMapView.onResume();
        try {
            MapsInitializer.initialize(requireActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(HomeFragment.this.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeFragment.this.requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                    return;
                }

                databaseCafea.addListenerForSingleValueEvent(valueEventListener);
                fetchLastLocation();

                googleMap.setMyLocationEnabled(true);


            }
        });

        searchView = view.findViewById(R.id.sv_location);
        updateValuesFromBundle(savedInstanceState);

        searchView.setQueryHint(" ¯_(ツ)_/¯ ");

        viewFlipper = view.findViewById(R.id.view_flipper);
        Button previous = view.findViewById(R.id.previous);
        previous.setOnClickListener(this);
        Button next = view.findViewById(R.id.next);
        next.setOnClickListener(this);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                rezultat = new ArrayList<>();

                String location = searchView.getQuery().toString().toLowerCase();
                /*for(int i=0;i<mrk.size();i++){
                    if(location.equals(mrk.get(i).getTitle())){
                        try {
                            getCityName(mrk.get(i).getPosition());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }*/

                firebaseUserSearch(location, currentLocation, databaseCafea);
                rezultat.clear();


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previous: {
                //searchView.clearFocus();
                viewFlipper.setInAnimation(HomeFragment.this.requireContext(), R.anim.right);
                viewFlipper.setOutAnimation(HomeFragment.this.requireContext(), R.anim.slide_out_left);
                viewFlipper.showPrevious();

                //Toast.makeText(HomeFragment.this.requireContext(), "AAAAAAAAAAAAAA", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.next: {
                viewFlipper.setInAnimation(HomeFragment.this.requireContext(), android.R.anim.slide_in_left);
                viewFlipper.setOutAnimation(HomeFragment.this.requireContext(), android.R.anim.slide_out_right);
                //searchView.setIconified(false);
                viewFlipper.showNext();
                //searchView.clearFocus();
                break;
            }
        }
    }
    SearchView searchView;
    //private SettingsClient settingsClient;
    //private LocationSettingsRequest locationSettingsRequest;

    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(HomeFragment.this.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeFragment.this.requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        final Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (task.isSuccessful() && task.getResult() != null) {
                    currentLocation = task.getResult();
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()), 15));


                }
            }
        })


                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });

    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            requestingLocationUpdates = savedInstanceState.getBoolean(
                    REQUESTING_LOCATION_UPDATES_KEY);
        }
        UpdateUi();
        // Toast.makeText(HomeFragment.this.requireContext(), "S-A SALVAT", Toast.LENGTH_SHORT).show();
    }

    protected void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        /* 60 secs */
        long UPDATE_INTERVAL = 60000;
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        /* 5 secs */
        long FASTEST_INTERVAL = 5000;
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(HomeFragment.this.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeFragment.this.requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.getMainLooper());


    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /*    Metoda ceopreste updatarea locatiei dar in plus va da la user(bun de debug un mesaj instiintandu l ca nu i se mai updateaza
    de asemenea in situatia de fata T Task<Void> task este folosit pt a sincroniza textul cu operatia
    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback).addOnCompleteListener
                ((Activity) HomeFragment.this.requireContext(),new OnCompleteListener<Void>(){
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(HomeFragment.this.requireContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();}
                });
    }*/

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        Toast.makeText(HomeFragment.this.requireContext(), "nu!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void UpdateUi() {
        if (isVisible()) {
            currentLocation.getLongitude();
            currentLocation.getLatitude();
        }
    }

    public class UsersAdapter extends ArrayAdapter<Cafenea> {


        public UsersAdapter(@NonNull Context context, ArrayList<Cafenea> users) {
            super(context, 0, users);
        }

        @NonNull
        @Override

        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            // Get the data item for this position

            Cafenea user = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view

            if (convertView == null) {

                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_layout, parent, false);

            }

            // Lookup view for data population

            final TextView user_name = convertView.findViewById(R.id.nume_text);
            final TextView adress = convertView.findViewById(R.id.adresa_text);
            Button Locatie = convertView.findViewById(R.id.buttonLocatie);

            assert user != null;
            user_name.setText(user.getName());
            adress.setText(user.getAddress());

            Locatie.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    //searchView.clearFocus();
                    viewFlipper.setOutAnimation(HomeFragment.this.requireContext(), R.anim.slide_out_left);
                    viewFlipper.showPrevious();
                    for (int i = 0; i < rezultat.size(); i++) {
                        if (user_name.getText().equals(rezultat.get(i).getName()) && adress.getText().equals(rezultat.get(i).getAddress())) {
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(rezultat.get(i).getLatitude(),
                                            rezultat.get(i).getLongitude()), 20));
                            if (mrk.isEmpty()) {
                                googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(rezultat.get(i).getLatitude(), rezultat.get(i).getLongitude()))
                                        .title(rezultat.get(i).getName())
                                        .snippet(rezultat.get(i).getAddress())
                                        .icon(BitmapDescriptorFactory.defaultMarker
                                                (BitmapDescriptorFactory.HUE_AZURE)));
                            } else {
                                for (int x = 0; x < mrk.size(); x++) {
                                    if (!new LatLng(rezultat.get(i).getLatitude(),
                                            rezultat.get(i).getLongitude()).equals(mrk.get(i).getPosition())) {
                                        googleMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(rezultat.get(i).getLatitude(), rezultat.get(i).getLongitude()))
                                                .title(rezultat.get(i).getName())
                                                .snippet(rezultat.get(i).getAddress())
                                                .icon(BitmapDescriptorFactory.defaultMarker
                                                        (BitmapDescriptorFactory.HUE_AZURE)));
                                    }
                                }
                            }
                        }
                    }

                    Log.e("apasat", "A mers");
                }
            });

            // Return the completed view to render on screen

            return convertView;

        }
    }


}

/*public void printtojson(ArrayList<Marker> mrk) {

        try {
            FileOutputStream out = new FileOutputStream(new File(Environment.getDataDirectory().getPath() + "Marcovia.json"), true);
            OutputStreamWriter osw = new OutputStreamWriter(Objects.requireNonNull(out));
            JsonWriter writer = new JsonWriter(osw);
            for (int i = 0; i < mrk.size(); i++) {
                writer.setIndent("\t");
                try {
                    writer.beginObject();
                    writer.name("Cafenele").beginArray();
                    writer.beginObject();
                    writer.name("id").value(String.valueOf(mrk.get(i).getPosition()));
                    writer.name("name").value(mrk.get(i).getTitle());
                    writer.name("Address").value(mrk.get(i).getSnippet());
                    writer.endObject();
                    writer.endArray();
                    writer.endObject();
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    Log.e("problema", "nu s-a putut");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }*/