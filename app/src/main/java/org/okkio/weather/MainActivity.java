package org.okkio.weather;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LOG_" + MainActivity.class.getSimpleName();
    private static final String SHARED_PREF_NAME = "org.okkio.weather.pref";
    private static final String SHARED_PREF_KEY_CITIES = "cities";
    private static final int REQUEST_CODE_SEARCH = 0;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;

    private ProgressDialog mProgressDialog;

    private List<Model> mCitiesList = new ArrayList<>();
    private ArrayList<ImageView> mIndicators = new ArrayList<>();
    private int mCurrentPagePosition = 0;
    private SharedPreferences mSharedPreferences;
    private PageAdapter mPageAdapter;
    private ViewPager mViewPager;
    private LinearLayout mIndicatorLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black_trans80));
        }
        setContentView(R.layout.activity_main);
        mViewPager = findViewById(R.id.container);
        mIndicatorLayout = findViewById(R.id.indicator_layout);
        ImageButton menuButtonView = findViewById(R.id.menu_button);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        mPageAdapter = new PageAdapter(getSupportFragmentManager(), mCitiesList);
        mViewPager.setAdapter(mPageAdapter);
        mViewPager.setCurrentItem(mCurrentPagePosition);

        final ArgbEvaluator evaluator = new ArgbEvaluator();
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            float sumOffset;
            int nextPagePosition = 1;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int startColor = mCitiesList.get(position).getColor();
                int endColor = startColor;
                nextPagePosition = mCitiesList.size() > position + 1 ? position + 1 : position;
                // hmm, very strange. if scrolling to right - 'position' is the current position.
                // if scrolling to left - 'position' is the previous position
                if (position + positionOffset > sumOffset) {
                    endColor = mCitiesList.get(nextPagePosition).getColor();
                } else {
                    if (position <= mCurrentPagePosition) {
                        startColor = mCitiesList.get(position).getColor();
                        endColor = mCitiesList.get(nextPagePosition).getColor();
                    }
                }
                sumOffset = position + positionOffset; // for check direction
                int colorUpdate = (Integer) evaluator.evaluate(positionOffset, startColor, endColor);
                mViewPager.setBackgroundColor(colorUpdate);
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPagePosition = position;
                updateIndicators();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        final ViewGroup nullParent = null;
        View sheetView = getLayoutInflater().inflate(R.layout._bottom_sheet, nullParent);
        mBottomSheetDialog.setContentView(sheetView);
        NavigationView navigationView = sheetView.findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                mBottomSheetDialog.dismiss();
                switch (menuItem.getItemId()) {
                    case R.id.remove:
                        removeCurrentPage();
                        return true;
                    case R.id.search:
                        showSearch();
                        return true;
                    case R.id.about:
                        showAbout();
                        return true;
                    default:
                        return false;
                }
            }
        });


        menuButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetDialog.show();
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        Set<String> cityIds = mSharedPreferences.getStringSet(SHARED_PREF_KEY_CITIES, null);
        if (cityIds != null && !cityIds.isEmpty()) {
            loadWeatherByIds(TextUtils.join(",", cityIds));
        } else {
            if (!checkPermissions()) {
                requestPermissions();
            } else {
                getLastLocation();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_SEARCH) {
            if (data == null) {
                return;
            }
            Model model = SearchActivity.getSelectedCityId(data);
            for (int i = 0; i < mCitiesList.size(); i++) {
                if (mCitiesList.get(i).getId() == model.getId()) {
                    mCurrentPagePosition = i;
                    return;
                }
            }
            mCitiesList.add(model);
            mPageAdapter.notifyDataSetChanged();
            mCurrentPagePosition = mCitiesList.size() - 1;
            mViewPager.setCurrentItem(mCurrentPagePosition);
            createIndicators();
            saveToPreferences(model.getId(), false);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                return;
            }
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation();
            } else {
                // Permission denied.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Build intent that displays the App settings screen.
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            }
        }
    }

    //region Permissions
    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    mLastLocation = task.getResult();
                    loadWeatherByCoordinates(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                } else {
                    showSearch();
                    //showSnackbar(getString(R.string.no_location_detected));
                }
            }
        });
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (shouldProvideRationale) {
            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            startLocationPermissionRequest();
        }
    }
    //endregion

    //region LoadData
    public void loadWeatherByIds(String ids) {
        mProgressDialog.show();
        Map<String, String> params = new HashMap<>();
        params.put("id", ids);
        String url = buildApiUrl("group", params);
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                mCitiesList.clear();
                try {
                    JSONObject object = new JSONObject(string);
                    JSONArray list = object.getJSONArray("list");
                    for (int i = 0; i < list.length(); i++) {
                        Model model = new Model(list.getJSONObject(i));
                        mCitiesList.add(model);
                    }
                    mPageAdapter.notifyDataSetChanged();
                    createIndicators();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
                mProgressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(MainActivity.this);
        rQueue.add(request);
    }

    public void loadWeatherByCoordinates(double lat, double lon) {
        mProgressDialog.show();
        Map<String, String> params = new HashMap<>();
        params.put("lat", String.valueOf(lat));
        params.put("lon", String.valueOf(lon));
        String url = buildApiUrl("weather", params);
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                try {
                    Model model = new Model(new JSONObject(string));
                    mCitiesList.add(model);
                    mPageAdapter.notifyDataSetChanged();
                    createIndicators();
                    saveToPreferences(model.getId(), false);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
                mProgressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(MainActivity.this);
        rQueue.add(request);
    }
    //endregion

    //region SnackBars
    private void showSnackbar(String text) {
        Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG).show();
    }

    private void showSnackbar(int mainTextStringId, int actionStringId, View.OnClickListener listener) {
        Snackbar
                .make(findViewById(android.R.id.content), getString(mainTextStringId), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }
    //endregion

    //region Page indicators
    private void createIndicators() {
        mIndicators.clear();
        mIndicatorLayout.removeAllViews();
        for (int i = 0; i < mCitiesList.size(); i++) {
            addIndicator();
        }
        updateIndicators();
    }

    private void addIndicator() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(8), dpToPx(8));
        ImageView indicator = new ImageView(MainActivity.this);
        params.setMarginEnd(dpToPx(8));
        indicator.setLayoutParams(params);
        indicator.setImageResource(R.drawable.indicator_unselected);
        mIndicatorLayout.addView(indicator);
        mIndicators.add(indicator);
    }

    private void removeIndicator(int position) {
        mIndicators.remove(mCurrentPagePosition);
        LinearLayout indicatorLayout = findViewById(R.id.indicator_layout);
        indicatorLayout.removeViewAt(position);
        updateIndicators();
    }

    private void updateIndicators() {
        for (int i = 0; i < mIndicators.size(); i++) {
            mIndicators.get(i).setImageResource(i == mCurrentPagePosition ? R.drawable.indicator_selected : R.drawable.indicator_unselected);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    //endregion

    //region Menu Actions
    void removeCurrentPage() {
        saveToPreferences(mCitiesList.get(mCurrentPagePosition).getId(), true);
        mCitiesList.remove(mCurrentPagePosition);
        mPageAdapter.notifyChangeInPosition(mCurrentPagePosition);
        removeIndicator(mCurrentPagePosition);
    }

    void showSearch() {
        Intent i = new Intent(this, SearchActivity.class);
        startActivityForResult(i, REQUEST_CODE_SEARCH);
    }

    void showAbout() {
        AboutDialogFragment dialogFragment = new AboutDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "about");
    }
    //endregion

    /**
     * saveToPreferences
     *
     * @param id       City ID
     * @param isDelete if true - added to set else deleted
     */
    private void saveToPreferences(int id, boolean isDelete) {
        Set<String> set = mSharedPreferences.getStringSet(SHARED_PREF_KEY_CITIES, null);
        // Create a new instance because the Set returned by SharedPreference is immutable.
        Set<String> newSet = (set == null) ? new HashSet<String>() : new HashSet<>(set);
        if (isDelete) {
            newSet.remove(String.valueOf(mCitiesList.get(mCurrentPagePosition).getId()));
        } else {
            newSet.add(String.valueOf(id));
        }
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putStringSet(SHARED_PREF_KEY_CITIES, newSet);
        editor.apply();
    }

    public static String buildApiUrl(String method, Map<String, String> params) {
        Uri.Builder builder = Uri.parse(BASE_URL)
                .buildUpon()
                .appendPath(method)
                .appendQueryParameter("appid", BuildConfig.API_KEY)
                .appendQueryParameter("units", "metric");
        if (params != null) {
            for (Map.Entry entry : params.entrySet()) {
                builder.appendQueryParameter(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return builder.build().toString();
    }
}
