package com.forgroundtest.RIS_DSM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

public class AbstractCameraXActivity<R> extends BaseModuleActivity {
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 200;
    private static final String[] PERMISSIONS = {Manifest.permission.CAMERA};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startBackgroundThread();

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    REQUEST_CODE_CAMERA_PERMISSION
            );
        }else{
            Toast.makeText(
                    this,
                    "Permission Granted!!",
                    Toast.LENGTH_LONG
            ).show();
//            setupCameraX();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_CAMERA_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                Toast.makeText(
                        this,
                        "You can't use image classification example without granting CAMERA permission",
                        Toast.LENGTH_LONG
                ).show();
                finish();
            } else{
                Toast.makeText(
                        this,
                        "Permission Granted",
                        Toast.LENGTH_LONG
                ).show();
                //setupCameraX();
            }
        }
    }

    private void setupCameraX(){

    }
}