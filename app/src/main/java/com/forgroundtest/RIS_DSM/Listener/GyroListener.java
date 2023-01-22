package com.forgroundtest.RIS_DSM.Listener;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.TextView;

import com.forgroundtest.RIS_DSM.Filter.KalmanFilter;
import com.forgroundtest.RIS_DSM.Value;


public class GyroListener implements SensorEventListener {

    private KalmanFilter mKalmanGyroX = new KalmanFilter(0.0f);;
    private KalmanFilter mKalmanGyroY = new KalmanFilter(0.0f);;

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];


        float filteredX = 0.0f;
        float filteredY = 0.0f;
        float filteredZ = 0.0f;
        // 칼만필터를 적용한다
        filteredX = (float) mKalmanGyroY.update(x);
        filteredY = (float) mKalmanGyroX.update(y);
        filteredZ = (float) mKalmanGyroX.update(z);
//        Log.e("LOG", "ACCELOMETER"
//                + "           [X]:" + String.format("%.4f", event.values[0])
//                + "           [Y]:" + String.format("%.4f", event.values[1])
//                + "           [Z]:" + String.format("%.4f", event.values[2]));
        Value.GYRO_X = filteredX;
        Value.GYRO_Y = filteredY;
        Value.GYRO_Z = filteredZ;
        Log.d("Sensor",Value.GYRO_X+""+Value.GYRO_Y+""+Value.GYRO_Z+"");

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
