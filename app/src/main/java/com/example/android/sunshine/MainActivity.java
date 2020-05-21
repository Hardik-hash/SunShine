/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;
import com.example.android.sunshine.ForecastAdapter.ForecastAdapterOnClickHandler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements ForecastAdapterOnClickHandler,LoaderCallbacks<String[]>{

    private static final String TAG = MainActivity.class.getSimpleName();

       private RecyclerView mRecyclerView;
       private ForecastAdapter mForecastAdapter;

    private TextView merrormsg;
    private ProgressBar mprogressBar;
    private static final int FORECAST_LOADER_ID=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        mRecyclerView=(RecyclerView)findViewById(R.id.recyclerview_forecast);
        merrormsg = (TextView)findViewById(R.id.errormsg);

    LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.setHasFixedSize(true);
    mForecastAdapter = new ForecastAdapter(this);
    mRecyclerView.setAdapter(mForecastAdapter);
        mprogressBar = (ProgressBar)findViewById(R.id.mprogress);

     int loaderId = FORECAST_LOADER_ID;

        LoaderCallbacks<String[]> callback = MainActivity.this;
        Bundle bundleForLoader = null;
        getSupportLoaderManager().initLoader(loaderId,bundleForLoader,callback);
    }

    @Override
    public Loader<String[]> onCreateLoader(int i, @Nullable Bundle bundle) {
        return new AsyncTaskLoader<String[]>(this) {
            String[] mWeatherData = null;

            @Override
            protected void onStartLoading() {
                if(mWeatherData !=null)
                {
                    deliverResult(mWeatherData);
                }

                else
                {
                    mprogressBar.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public String[] loadInBackground() {
             String locationQuery = SunshinePreferences.getPreferredWeatherLocation(MainActivity.this);
             URL weatherRequestUrl = NetworkUtils.buildUrl(locationQuery);


             try{
                 String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);

                 String[] simpleJsonWeatherData = OpenWeatherJsonUtils.getSimpleWeatherStringsFromJson(MainActivity.this,jsonWeatherResponse);
                 return simpleJsonWeatherData;
             }

             catch (Exception e)
             {
                 e.printStackTrace();
                 return null;
             }
            }

            @Override
            public void deliverResult(String[] data) {
                mWeatherData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String[]> loader, String[] data) {
        mprogressBar.setVisibility(View.INVISIBLE);
         mForecastAdapter.setWeatherData(data);

         if(null == data)
         {
             showErrorMessage();
         }

         else
         {
             showWeatherDataView();
         }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String[]> loader) {

    }

    public void invalidateData()
    {
        mForecastAdapter.setWeatherData(null);
    }

    private void openLocationInMap()
    {
        String addressString = "Sardar Bridge, Surat";
        Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if(intent.resolveActivity(getPackageManager())!=null)
        {
            startActivity(intent);
        }

        else
        {
            Log.d(TAG,"couldn't call" +geoLocation.toString() + ", no receiving apps installed!");
        }
    }

    @Override
    public void onClick(String weatherForDay) {
        Context context =this;
        Toast.makeText(context, weatherForDay, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this,DetailActivity.class);
        intent.putExtra("weather_msg",weatherForDay);
        startActivity(intent);
    }

    private void showWeatherDataView(){
      mRecyclerView.setVisibility(View.VISIBLE);
        merrormsg.setVisibility(View.INVISIBLE);
    }

    private void showErrorMessage()
    {
        merrormsg.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.forecast,menu);
        getMenuInflater().inflate(R.menu.forecast,menu);
        return true;
    }

     @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();

        if(id == R.id.action_refresh)
        {
           invalidateData();
            getSupportLoaderManager().restartLoader(FORECAST_LOADER_ID,null,this);
            return true;
        }

        if(id==R.id.action_map)
        {
             openLocationInMap();
             return true;
        }
        return super.onOptionsItemSelected(item);
    }
}