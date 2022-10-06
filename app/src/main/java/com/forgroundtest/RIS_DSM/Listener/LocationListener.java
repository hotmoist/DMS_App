package com.forgroundtest.RIS_DSM.Listener;

import android.content.Context;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.forgroundtest.RIS_DSM.Value;

public class LocationListener implements android.location.LocationListener {

    private Context mContext = null;
    private double mSpeed = 0;
    private double beforeSpeed = 0;
    private TextView GPSName = null;
    private TextView GPSSpeedTextView = null;

    LocationRequest lr;


    public LocationListener(Context c, TextView GPSSpeedTextView, TextView GPSName, double mSpeed){
        this.mContext = c;
        this.GPSSpeedTextView = GPSSpeedTextView;
        this.GPSName = GPSName;
        this.mSpeed = mSpeed;
    }
    /**
     * callback function
     * 위치 바뀔때 마다 정보를 받아옴
     *
     * @param location
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onLocationChanged(Location location) {
        
        GPSName.setText(location.getProvider());
        Value.SPEED = location.getSpeed()*3.6;

        GPSSpeedTextView.setText(String.format("%.2f", Value.SPEED)+""+location.hasSpeed()+"");
        Value.ACC = Value.SPEED - beforeSpeed;
        beforeSpeed = Value.SPEED;
        //        Toast.makeText(mContext, mSpeed+"", Toast.LENGTH_SHORT).show();
        /**
         * speed ui 변경
         */
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}

