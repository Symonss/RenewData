package com.dennohpeter.renewdata;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class PageAdapter extends FragmentStatePagerAdapter {
    private int numOfTabs;

    PageAdapter(FragmentManager fm, int numOfTabs) {
        super(fm);

        this.numOfTabs = numOfTabs;
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case 1:
                fragment = new OptionsTab();
                break;
            case 2:
                fragment = new LogsTab();
                break;
            default:
                fragment = new HomeTab();
        }
        return fragment;
    }
}
