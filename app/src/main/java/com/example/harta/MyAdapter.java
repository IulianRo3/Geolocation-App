package com.example.harta;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MyAdapter extends FragmentStateAdapter {

    int totalTabs;
Context context;
    public MyAdapter(Context context, FragmentActivity fm, int totalTabs) {
        super(fm);
        this.totalTabs = totalTabs;
        this.context = context;
    }

    // this is for fragment tabs

    // this counts total number of tabs


    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0:
                FavTab1 favTab1 = new FavTab1();
                return favTab1;
            case 1:
                FavTab2 favTab2 = new FavTab2();
                return favTab2;

            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return totalTabs;
    }
}