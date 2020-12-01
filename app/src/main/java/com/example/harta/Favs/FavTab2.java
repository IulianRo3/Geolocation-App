package com.example.harta.Favs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.harta.R;

public class FavTab2 extends Fragment {
    public FavTab2(){
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fav_tab1, container, false);
        // Inflate the layout for this fragment
        return view;
    }
}
