package com.forgroundtest.RIS_DSM;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class InitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        findViewById(R.id.init_start_monitoring_button).setOnClickListener(v -> startActivity(new Intent(InitActivity.this, CameraXActivity.class) ));
    }
}