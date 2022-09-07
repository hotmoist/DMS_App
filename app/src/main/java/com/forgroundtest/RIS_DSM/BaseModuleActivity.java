package com.forgroundtest.RIS_DSM;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseModuleActivity extends AppCompatActivity {

    protected HandlerThread mBackgroundThread;
    protected Handler mBackgroundHandler;
    protected Handler mUIHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUIHandler = new Handler(getMainLooper());
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        startBackgroundThread();
    }

    @Override
    protected void onDestroy() {
        stopBackgroundThread();
        super.onDestroy();
    }

    protected void startBackgroundThread(){
        // ML 모델 구옫을 위한 별개 쓰래드 실행
        mBackgroundThread = new HandlerThread("ModuleActivity");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread(){
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e){
            Log.e("DMS_EDU", "Error on stopping background thread", e);
        }
    }
}
