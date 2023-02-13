package com.forgroundtest.RIS_DSM;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionSupport {
    private Context context;
    private Activity activity;

    private String[] permissions ={
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private List permissionList;

    private final int MULTIPLE_PERMISSIONS = 1023;

    public PermissionSupport(Activity _activity, Context _context){
        this.activity = _activity;
        this.context = _context;
    }

    public boolean checkPermission(){
        permissionList = new ArrayList<>();

        for(String pm: permissions){

            if (ContextCompat.checkSelfPermission(context, pm)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }

        if(!permissionList.isEmpty()){
            return false;
        }
        return true;
    }

    public void requestPermission(){
        ActivityCompat.requestPermissions(activity,
                (String[]) permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
    }

    public boolean permissionResult(int _requestCode, @NonNull String[] _permissions, @NonNull int[] _grantResults){
        if(_requestCode == MULTIPLE_PERMISSIONS && (_grantResults.length >0)){
            for(int result : _grantResults){
                if(result == -1){
                    return  false;
                }
            }
        }
        return  true;
    }
}
