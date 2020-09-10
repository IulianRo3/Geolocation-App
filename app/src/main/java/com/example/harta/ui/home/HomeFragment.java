package com.example.harta.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.harta.Cafenea;
import com.example.harta.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

import java.util.ArrayList;

public class HomeFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    ViewFlipper viewFlipper;
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "Update";
    public ArrayList<Marker> mrk;
    MapView mMapView;
    private GoogleMap googleMap;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    private LocationCallback locationCallback;
    Boolean requestingLocationUpdates = true;
    DatabaseReference databaseCafea;
    private RecyclerView mResultList;


    private void firebaseUserSearch(String searchText) {

        Log.e("adapter", "A parcurs pana aici");


        Query query = databaseCafea.orderByChild("name");
        //.startAt(searchText).endAt(searchText + "\uf8ff");
        FirebaseRecyclerOptions<Cafenea> options =
                new FirebaseRecyclerOptions.Builder<Cafenea>()
                        .setQuery(query, Cafenea.class)
                        .setLifecycleOwner(this)
                        .build();
        FirebaseRecyclerAdapter<Cafenea, UserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Cafenea, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull Cafenea model) {
                holder.setDetails(model.getName(), model.getAddress());
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, parent, false);

                return new UserViewHolder(view);
            }
        };
        Log.e("adapter", "A parcurs pana aici");
        mResultList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
        Log.e("adapter", "A parcurs TOT");
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        databaseCafea = FirebaseDatabase.getInstance().getReference("Cafenea");
        mrk = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_home, container, false);


        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(HomeFragment.this.requireContext());

        mResultList = (RecyclerView) view.findViewById(R.id.result_list);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(HomeFragment.this.getContext()));

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
        /*
        String [] columNames = { SearchManager.SUGGEST_COLUMN_TEXT_1 };
        int [] viewIds = { android.R.id.text1 };
        CursorAdapter adapter = new SimpleCursorAdapter(HomeFragment.this.requireContext(),
                android.R.layout.simple_list_item_1, null, columNames, viewIds);
        searchView.setSuggestionsAdapter(adapter);

         */

        viewFlipper = view.findViewById(R.id.view_flipper);
        Button previous = view.findViewById(R.id.previous);
        previous.setOnClickListener(this);
        Button next = view.findViewById(R.id.next);
        next.setOnClickListener(this);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                String location = searchView.getQuery().toString();
                firebaseUserSearch(location);
               /* for (int i = 0; i < mrk.size(); i++) {
                    if (location.equals(mrk.get(i).getTitle())) {

                        //viewFlipper.showPrevious();
                        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mrk.get(i).getPosition(), 20));
                       // searchView.setQuery(null, false);
                    }

                }*/
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return view;
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

                    mrk.add(googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(cafenea.getLatitude(), cafenea.getLongitude()))
                            .title(cafenea.getName())
                            .snippet(cafenea.getAddress())
                            .icon(BitmapDescriptorFactory.defaultMarker
                                    (BitmapDescriptorFactory.HUE_AZURE))));


                }
            } else {
                Log.e("ACCESARE ", "NU A MERS");
            }
        }


        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
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

    //private GoogleApiClient mGoogleApiClient;

    /*
       @Override
       public void onClick(View v) {
           viewFlipper.setInAnimation(HomeFragment.this.requireContext(), R.anim.right);
           viewFlipper.setOutAnimation(HomeFragment.this.requireContext(), R.anim.slide_out_left);
           viewFlipper.showPrevious();
           Toast.makeText(HomeFragment.this.requireContext(),"AAAAAAAAAAAAAA",Toast.LENGTH_SHORT).show();
       }
       */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previous: {
                viewFlipper.setInAnimation(HomeFragment.this.requireContext(), R.anim.right);
                viewFlipper.setOutAnimation(HomeFragment.this.requireContext(), R.anim.slide_out_left);
                viewFlipper.showPrevious();
                Toast.makeText(HomeFragment.this.requireContext(), "AAAAAAAAAAAAAA", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.next: {
                viewFlipper.setInAnimation(HomeFragment.this.requireContext(), android.R.anim.slide_in_left);
                viewFlipper.setOutAnimation(HomeFragment.this.requireContext(), android.R.anim.slide_out_right);
                searchView.setIconified(false);
                viewFlipper.showNext();
                break;
            }
        }
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

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDetails(String userName, String address) {
            TextView user_name = (TextView) mView.findViewById(R.id.nume_text);
            TextView adress = (TextView) mView.findViewById(R.id.adresa_text);


            user_name.setText(userName);
            adress.setText(address);


        }
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


    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            requestingLocationUpdates = savedInstanceState.getBoolean(
                    REQUESTING_LOCATION_UPDATES_KEY);
        }
        UpdateUi();
        Toast.makeText(HomeFragment.this.requireContext(), "S-A SALVAT", Toast.LENGTH_SHORT).show();
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