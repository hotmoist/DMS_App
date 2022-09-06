package com.forgroundtest.RIS_DSM;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.main_driving_start_button).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, InitActivity.class)));
    }
}