package com.forgroundtest.RIS_DSM.Listener;

import android.content.Context;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.forgroundtest.RIS_DSM.Value;

public class LocationListener implements android.location.LocationListener {

    private Context mContext = null;
    private double mSpeed = 0;
    private double beforeSpeed = 0;

    LocationRequest lr;


    public LocationListener(Context c){
        this.mContext = c;
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
        
        Value.SPEED = location.getSpeed()*3.6;

        Value.ACC = Value.SPEED - beforeSpeed;
        beforeSpeed = Value.SPEED;
        //        Toast.makeText(mContext, mSpeed+"", Toast.LENGTH_SHORT).show();
        /**
         * speed ui 변경
         */
        Log.d("Sensor_Speed",Value.SPEED+""+Value.ACC);

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

