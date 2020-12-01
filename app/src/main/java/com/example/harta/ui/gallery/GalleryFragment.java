package com.example.harta.ui.gallery;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.harta.Cafenea;
import com.example.harta.Cafenele;
import com.example.harta.Favs.MyAdapter;
import com.example.harta.MainActivity;
import com.example.harta.R;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class GalleryFragment extends Fragment {
    ViewPager2 viewPager;
    static public ArrayList<Cafenea> FavCache;
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
            Log.e("Citire", "" + sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    TabLayout tabLayout;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        FavCache = new ArrayList<>();
        viewPager = view.findViewById(R.id.pager);
        tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Home"));
        tabLayout.addTab(tabLayout.newTab().setText("Sport"));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final MyAdapter adapter = new MyAdapter(GalleryFragment.this.requireContext(),getActivity(), tabLayout.getTabCount());

        viewPager.setAdapter(adapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        read_file(GalleryFragment.this.requireContext(), MainActivity.fileName1,FavCache);
        return view;
    }


}