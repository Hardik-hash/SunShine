package com.example.android.sunshine;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {
    private String mForecast;
    private TextView mWeatherDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mWeatherDisplay=(TextView)findViewById(R.id.tv_display_weather);

        if(getIntent().hasExtra("weather_msg"))
        {
           mForecast=getIntent().getStringExtra("weather_msg");
           mWeatherDisplay.setText(mForecast);
        }
    }
}
