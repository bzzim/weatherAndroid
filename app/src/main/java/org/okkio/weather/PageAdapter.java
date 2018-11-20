package org.okkio.weather;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;

import java.util.List;

public class PageAdapter extends FragmentPagerAdapter {
    private List<Model> mCityList;
    private long baseId = 0;

    PageAdapter(FragmentManager fm, List<Model> cityList) {
        super(fm);
        mCityList = cityList;
    }

    @Override
    public Fragment getItem(int position) {
        return PageFragment.newInstance(mCityList.get(position));
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        // this is called when notifyDataSetChanged() is called
        // refresh all fragments when data set changed
        return POSITION_NONE;
    }

    @Override
    public long getItemId(int position) {
        // give an ID different from position when position has been changed
        return baseId + position;
    }

    @Override
    public int getCount() {
        return mCityList.size();
    }

    /**
     * Notify that the position of a fragment has been changed.
     * Create a new ID for each position to force recreation of the fragment
     *
     * @param n number of items which have been changed
     */
    void notifyChangeInPosition(int n) {
        // shift the ID returned by getItemId outside the range of all previous fragments
        baseId += getCount() + n;
        notifyDataSetChanged();
    }
}
