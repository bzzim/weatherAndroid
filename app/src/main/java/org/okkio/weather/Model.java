package org.okkio.weather;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.graphics.ColorUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class Model implements Parcelable {
    private int mId;
    private String mCityName;
    private String mCountryCode;
    private double mTemperature;
    private int mPressure;
    private int mHumidity;
    private int mVisibility;
    private double mWindSpeed;
    private int mWindDegree;
    private int mClouds;
    private String mWeatherName;
    private String mWeatherDescription;
    private int mColor;
    private int mIcon;
    private String mIconName;
    private int mDt;

    public Model() {

    }

    public Model(JSONObject object) {
        try {
            JSONArray weather = object.getJSONArray("weather");
            JSONObject main = object.getJSONObject("main");
            JSONObject wind = object.getJSONObject("wind");

            mId = object.getInt("id");
            mCityName = object.getString("name");
            mCountryCode = object.getJSONObject("sys").getString("country");
            mTemperature = main.getDouble("temp");
            mPressure = main.getInt("temp");
            mHumidity = main.getInt("humidity");
            if (object.has("visibility")) {
                mVisibility = object.getInt("visibility");
            }
            if (wind.has("speed")) {
                mWindSpeed = wind.getDouble("speed");
            }
            if (wind.has("deg")) {
                mWindDegree = wind.getInt("deg");
            }
            mClouds = object.getJSONObject("clouds").getInt("all");
            mWeatherName = weather.getJSONObject(0).getString("main");
            mWeatherDescription = weather.getJSONObject(0).getString("description");
            mColor = getColorByTemp(mTemperature);
            mIconName = "ic_weather_" + weather.getJSONObject(0).getString("icon");
            //model.setIcon(getIconRes(weather.getJSONObject(0).getString("icon")));
            mDt = object.getInt("dt");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected Model(Parcel in) {
        mId = in.readInt();
        mCityName = in.readString();
        mCountryCode = in.readString();
        mTemperature = in.readDouble();
        mPressure = in.readInt();
        mHumidity = in.readInt();
        mVisibility = in.readInt();
        mWindSpeed = in.readDouble();
        mWindDegree = in.readInt();
        mClouds = in.readInt();
        mWeatherName = in.readString();
        mWeatherDescription = in.readString();
        mColor = in.readInt();
        mIcon = in.readInt();
        mIconName = in.readString();
        mDt = in.readInt();
    }

    public static final Creator<Model> CREATOR = new Creator<Model>() {
        @Override
        public Model createFromParcel(Parcel in) {
            return new Model(in);
        }

        @Override
        public Model[] newArray(int size) {
            return new Model[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mCityName);
        dest.writeString(mCountryCode);
        dest.writeDouble(mTemperature);
        dest.writeInt(mPressure);
        dest.writeInt(mHumidity);
        dest.writeInt(mVisibility);
        dest.writeDouble(mWindSpeed);
        dest.writeInt(mWindDegree);
        dest.writeInt(mClouds);
        dest.writeString(mWeatherName);
        dest.writeString(mWeatherDescription);
        dest.writeInt(mColor);
        dest.writeInt(mIcon);
        dest.writeString(mIconName);
        dest.writeInt(mDt);
    }

    public String getCityName() {
        return mCityName;
    }

    public void setCityName(String cityName) {
        mCityName = cityName;
    }

    public String getTemperatureCelsius() {
        return String.valueOf(Math.round(mTemperature)) + (char) 0x00B0 + "C";
    }

    public double getTemperature() {
        return mTemperature;
    }

    public void setTemperature(double temperature) {
        mColor = getColorByTemp(temperature);
        mTemperature = temperature;
    }

    public int getPressure() {
        return mPressure;
    }

    public void setPressure(int pressure) {
        mPressure = pressure;
    }

    public int getHumidity() {
        return mHumidity;
    }

    public void setHumidity(int humidity) {
        mHumidity = humidity;
    }

    public int getVisibility() {
        return mVisibility;
    }

    public void setVisibility(int visibility) {
        mVisibility = visibility;
    }

    public double getWindSpeed() {
        return mWindSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        mWindSpeed = windSpeed;
    }

    public int getWindDegree() {
        return mWindDegree;
    }

    public void setWindDegree(int windDegree) {
        mWindDegree = windDegree;
    }

    public int getClouds() {
        return mClouds;
    }

    public void setClouds(int clouds) {
        mClouds = clouds;
    }

    public String getWeatherName() {
        return mWeatherName;
    }

    public void setWeatherName(String weatherName) {
        mWeatherName = weatherName;
    }

    public String getWeatherDescription() {
        return mWeatherDescription;
    }

    public void setWeatherDescription(String weatherDescription) {
        weatherDescription = weatherDescription.substring(0, 1).toUpperCase() + weatherDescription.substring(1);
        mWeatherDescription = weatherDescription;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public int getIcon() {
        return mIcon;
    }

    public void setIcon(int icon) {
        mIcon = icon;
    }

    public int getDt() {
        return mDt;
    }

    public void setDt(int dt) {
        mDt = dt;
    }

    public String getCountryCode() {
        return mCountryCode;
    }

    public void setCountryCode(String countryCode) {
        mCountryCode = countryCode;
    }

    public String getIconName() {
        return mIconName;
    }

    public void setIconName(String iconName) {
        mIconName = iconName;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    private int getColorByTemp(double temp) {
        int min = -35;
        int max = 35;
        int[] colors = {
                Color.rgb(159, 85, 181),
                Color.rgb(44, 106, 187),
                Color.rgb(82, 139, 213),
                Color.rgb(103, 163, 222),
                Color.rgb(142, 202, 240),
                Color.rgb(155, 213, 244),
                Color.rgb(172, 225, 253),
                Color.rgb(194, 234, 255),
                Color.rgb(253, 212, 97),
                Color.rgb(244, 168, 94),
                Color.rgb(244, 129, 89),
                Color.rgb(244, 104, 89),
                Color.rgb(244, 76, 73)
        };
        int length = colors.length - 1;
        if (temp <= min) {
            return colors[0];
        }
        if (temp >= max) {
            return colors[length];
        }
        int abs = Math.abs(min) + Math.abs(max);
        temp = temp + abs / 2;
        double percent = 100 / (abs / temp);
        int value = (int) Math.round((length / 100.0) * percent);
        return colors[value];
    }
}
