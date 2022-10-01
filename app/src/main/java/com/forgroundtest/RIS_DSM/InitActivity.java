package com.forgroundtest.RIS_DSM;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

/**
 * 운전자의 상태 파악을 위한 Activity
 * layout : activity_init.xml
 *
 * TODO: N back test 구현 필요
 */

public class InitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        findViewById(R.id.init_start_monitoring_button).setOnClickListener(v -> startActivity(new Intent(InitActivity.this, PredictionActivity.class) ));
    }
}