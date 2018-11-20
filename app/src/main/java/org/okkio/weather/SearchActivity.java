package org.okkio.weather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {
    public static final String EXTRA_CITY_ID = "org.okkio.weather.city";
    private ProgressDialog mProgressDialog;
    private List<Model> mCitiesList = new ArrayList<>();
    private SearchAdapter mSearchAdapter;

    public static Model getSelectedCityId(Intent result) {
        return result.getParcelableExtra(EXTRA_CITY_ID);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        final SearchView searchView = findViewById(R.id.search);
        searchView.requestFocusFromTouch();
        RecyclerView resultListView = findViewById(R.id.result_list);
        resultListView.setHasFixedSize(true);
        resultListView.setLayoutManager(new LinearLayoutManager(this));
        mSearchAdapter = new SearchAdapter(mCitiesList, new OnCityClickListener() {
            @Override
            public void onCityClick(Model model) {
                setSelectedCityIdResult(model);
            }
        });
        resultListView.setAdapter(mSearchAdapter);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        resultListView.addItemDecoration(itemDecoration);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchCity(s);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private void setSelectedCityIdResult(Model model) {
        Intent data = new Intent();
        data.putExtra(EXTRA_CITY_ID, model);
        setResult(RESULT_OK, data);
        finish();
    }

    public void searchCity(String q) {
        mProgressDialog.show();
        Map<String, String> params = new HashMap<>();
        params.put("q", q);
        String url = MainActivity.buildApiUrl("find", params);
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
                    mSearchAdapter.notifyDataSetChanged();
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

        RequestQueue rQueue = Volley.newRequestQueue(SearchActivity.this);
        rQueue.add(request);
    }

    public interface OnCityClickListener {
        void onCityClick(Model model);
    }
}
