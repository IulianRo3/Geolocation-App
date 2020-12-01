package com.example.harta.Favs;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.harta.Cafenea;
import com.example.harta.MainActivity;
import com.example.harta.R;
import com.example.harta.ui.gallery.GalleryFragment;
import com.example.harta.ui.home.HomeFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FavTab1 extends Fragment {
    public FavTab1(){
    }
    ListView lv;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fav_tab1, container, false);
        lv = view.findViewById(R.id.Faves_list);
        UsersAdapter adapter = new UsersAdapter(FavTab1.this.requireContext(), GalleryFragment.FavCache);

        lv.setAdapter(adapter);
        // Inflate the layout for this fragment

        return view;
    }
    public class UsersAdapter extends ArrayAdapter<Cafenea> {
        public UsersAdapter(@NonNull Context context, ArrayList<Cafenea> users) {
            super(context, 0, users);
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        @NonNull
        @Override
        //Creare butoane si elemente de afisare
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            final Cafenea user = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.fav_list_layout, parent, false);
            }
            // Lookup view for data population
            final View finalConvertView = convertView;
            final TextView user_name = finalConvertView.findViewById(R.id.textView3);
            final TextView adress = finalConvertView.findViewById(R.id.textView4);


            assert user != null;
            user_name.setText(user.getName());
            adress.setText(user.getAddress());



            // Return the completed view to render on screen
            return convertView;
        }
    }

}
