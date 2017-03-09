package com.fydp.uwaterloo.launchcam.Adapters;

/**
 * Created by Said Afifi on 15-Jul-16.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.fydp.uwaterloo.launchcam.Fragments.BluetoothFragment;
import com.fydp.uwaterloo.launchcam.Fragments.StreamingDataFragment;
import com.fydp.uwaterloo.launchcam.Fragments.StreamingVideoFragment;

/**
 * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class AppPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

    public AppPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                return new BluetoothFragment();
            case 1:
                return new StreamingDataFragment();
            default:
                return new StreamingVideoFragment();
        }
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Controls";
            case 1:
                return "Data Analysis";
            case 2:
                return "Streaming";
        }
        return null;
    }
}