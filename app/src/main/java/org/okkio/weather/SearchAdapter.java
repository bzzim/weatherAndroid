package org.okkio.weather;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private List<Model> mModelList;
    private Context mContext;
    private SearchActivity.OnCityClickListener mListener;

    SearchAdapter(List<Model> modelList, SearchActivity.OnCityClickListener onClickListener) {
        mModelList = modelList;
        mListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        mContext = viewGroup.getContext();
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout._search_result, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Model item = mModelList.get(i);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCityClick(item);
            }
        });

        viewHolder.mTitleView.setText(mContext.getString(R.string.city_name_country, item.getCityName(), item.getCountryCode()));
        viewHolder.mTemperature.setText(item.getTemperatureCelsius());
        viewHolder.mWeatherDescription.setText(item.getWeatherDescription());
        if (item.getIcon() != 0) {
            viewHolder.mWeatherIcon.setImageResource(item.getIcon());
        } else {
            viewHolder.mWeatherIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mModelList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View mWrapperView;
        TextView mTitleView, mTemperature, mWeatherDescription;
        ImageView mWeatherIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitleView = itemView.findViewById(R.id.title);
            mWeatherIcon = itemView.findViewById(R.id.weather_icon);
            mTemperature = itemView.findViewById(R.id.temperature);
            mWeatherDescription = itemView.findViewById(R.id.weather_description);
            mWrapperView = itemView.findViewById(R.id.wrapper);
        }
    }
}
