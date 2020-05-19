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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.net.URL;

public class MainActivity extends AppCompatActivity implements ForecastAdapterOnClickHandler{

       private RecyclerView mRecyclerView;
       private ForecastAdapter mForecastAdapter;

    private TextView merrormsg;
    private ProgressBar mprogressBar;

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

        loadWeatherData();
    }

    public void loadWeatherData()
    {
        showWeatherDataView();
        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        new FetchWeatherTask().execute(location);
    }

    @Override
    public void onClick(String weatherForDay) {
        Context context =this;
        Toast.makeText(context, weatherForDay, Toast.LENGTH_SHORT).show();
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

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {


        // TODO (6) Override the doInBackground method to perform your network requests


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mprogressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(String... params) {

            if(params.length==0)
            {
                return null;
            }

            String location = params[0];
            URL weatherRequestUrl = NetworkUtils.buildUrl(location);

            try{
                String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);

                String[] simpleJsonWeatherData = OpenWeatherJsonUtils.getSimpleWeatherStringsFromJson(MainActivity.this,jsonWeatherResponse);
                return simpleJsonWeatherData;
            }


            catch(Exception e){
                e.printStackTrace();
                return null;
            }

        }


        // TODO (7) Override the onPostExecute method to display the results of the network request

        @Override
        protected void onPostExecute(String[] weatherData) {
            mprogressBar.setVisibility(View.INVISIBLE);

            if(weatherData != null) {
                     showWeatherDataView();

                mForecastAdapter.setWeatherData(weatherData);
            }

            else
            {
                showErrorMessage();
            }
        }
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
           mForecastAdapter.setWeatherData(null);
            loadWeatherData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}