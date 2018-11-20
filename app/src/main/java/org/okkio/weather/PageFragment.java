package org.okkio.weather;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

public class PageFragment extends Fragment {
    private static final String ARG_DATA = "data";
    private Model mData;

    public PageFragment() {
        // Required empty public constructor
    }

    public static PageFragment newInstance(Model data) {
        PageFragment fragment = new PageFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_DATA, data);
        fragment.setArguments(args);
        return fragment;
    }

    public Model getData() {
        return mData;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mData = getArguments().getParcelable(ARG_DATA);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);
        ImageView weatherIcon = view.findViewById(R.id.weather_icon);
        TextView cityNameView = view.findViewById(R.id.city_name);
        TextView temperatureView = view.findViewById(R.id.temperature);
        TextView descriptionView = view.findViewById(R.id.weather_description);
        TextView cloudView = view.findViewById(R.id.cloud_value);
        TextView windView = view.findViewById(R.id.wind_value);
        TextView humidityView = view.findViewById(R.id.humidity_value);
        if (mData != null) {
            Context context = inflater.getContext();
            cityNameView.setText(mData.getCityName());
            temperatureView.setText(mData.getTemperatureCelsius());
            descriptionView.setText(mData.getWeatherDescription());
            cloudView.setText(context.getString(R.string.percent, (int) Math.round(mData.getClouds())));
            windView.setText(context.getString(R.string.ms, (int) Math.round(mData.getWindSpeed())));
            humidityView.setText(context.getString(R.string.percent, (int) Math.round(mData.getHumidity())));
            int iconRes = getIconRes(mData.getIconName());
            if (iconRes != 0) {
                weatherIcon.setImageResource(iconRes);
            }
        }
        return view;
    }

    private int getIconRes(String name) {
        return getContext() != null ? getResources().getIdentifier(name, "drawable", getContext().getPackageName()) : 0;
    }
}
