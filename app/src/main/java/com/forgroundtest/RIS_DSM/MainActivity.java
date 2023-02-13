package com.forgroundtest.RIS_DSM;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import com.forgroundtest.RIS_DSM.Model.PermissionSupport;

import java.util.Objects;

/**
 * 최초 화면을 나타내는 Activity
 * layout : activity_main.xml
 */

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 200;
    private PermissionSupport permissionSupport;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        checkPermissions();
        findViewById(R.id.main_driving_start_button).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, InitActivity.class)));
    }

    private void checkPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionSupport = new PermissionSupport(this, this);

            if(!permissionSupport.checkPermission()){
                permissionSupport.requestPermission();
            }
        }
    }

    private void requestBluetoothPermission(){
        if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

            builder.setTitle("블루투스에 대한 액세스가 필요합니다");
            builder.setMessage("어플리케이션이 블루투스를 감지 할 수 있도록 위치 정보 액세스 권한을 부여하십시오.");
            builder.setPositiveButton(android.R.string.ok, null);

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
                }
            });
            builder.show();
        }
        if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

            builder.setTitle("블루투스에 대한 액세스가 필요합니다");
            builder.setMessage("어플리케이션이 블루투스를 연결 할 수 있도록 위치 정보 액세스 권한을 부여하십시오.");
            builder.setPositiveButton(android.R.string.ok, null);

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 3);
                }
            });
            builder.show();
        }

    }

    private void requestCameraPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE_CAMERA_PERMISSION
                    );
        }
    }

    private void requestLocationPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
    }

    private void requestRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String requiredPermission = Manifest.permission.RECORD_AUDIO;

            // If the user previously denied this permission then show a message explaining why
            // this permission is needed
            if (checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 101);
            }
        }
    }
}