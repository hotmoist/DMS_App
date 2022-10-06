package com.forgroundtest.RIS_DSM;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.forgroundtest.RIS_DSM.Listener.GyroListener;
import com.forgroundtest.RIS_DSM.Listener.STTListener;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 추론 모델을 위한 background 쓰레드 적용 Activity
 */

public class BaseModuleActivity extends AppCompatActivity {

    protected HandlerThread mBackgroundThread;
    protected Handler mBackgroundHandler;
    protected Handler mUIHandler;

    static boolean START_CSV = false;
    /**
     * GPS위치를 위한 변수
     */
    public LocationManager locationManager = null;
    public LocationListener locationListener = null;

    /**
     * Gyro 센서를 위한 변수들
     */
    public SensorManager sensorManager = null;
    public SensorEventListener gyroListener = null;
    public Sensor gyroSensor = null;

    /**
     * CSV를 위한 변수들
     */
    public Intent intent;
    public RecognitionListener STTListener = null;
    public final int PERMISSION = 1;
    public EditText fileNameEdit = null;
    public Button csvBtn;

    Timer timer;
    TimerTask timerTask;
    FileWriter file;
    CSVWriter writer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUIHandler = new Handler(getMainLooper());


        resistSensor();
        resistGyroSensor();
        resistSTT();
        resistLocation();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        startBackgroundThread();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(gyroListener, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        stopBackgroundThread();
        super.onDestroy();
        sensorManager.unregisterListener(gyroListener);

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
    @UiThread
    protected void showErrorDialog(View.OnClickListener clickListener) {
        final View view = InfoViewFactory.newErrorDialogView(this);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog)
                .setCancelable(false)
                .setView(view);
        final AlertDialog alertDialog = builder.show();
        view.setOnClickListener(v -> {
            clickListener.onClick(v);
            alertDialog.dismiss();
        });
    }


    public void resistSTT(){
        /**
         * STT 등록 실행
         */

//        sttText = findViewById(R.id.sttResult);
//        sttBtn = findViewById(R.id.sttStart);

//        intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");   // 텍스트로 변환시킬 언어 설정

//        STTListener = new STTListener(this);
//        sttBtn.setOnClickListener(v -> {
//            SpeechRecognizer mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
//            mRecognizer.setRecognitionListener(STTListener);
//            mRecognizer.startListening(intent);
//        });
    }

    public void resistLocation(){
        /**
         * 위치정보 등록
         * 위치의 리스너 매니저, 권한 등록
         */


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new com.forgroundtest.RIS_DSM.Listener.LocationListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        long passiveInterval = LocationRequest.PASSIVE_INTERVAL;
        Toast.makeText(this,passiveInterval+"",Toast.LENGTH_SHORT).show();
    }
    public void resistSensor(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    public void resistGyroSensor(){
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);



        gyroListener = new GyroListener();
    }

    public void startCsvButton(){
        if(START_CSV=!START_CSV) {


            try {
                String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS) + "/" + fileNameEdit.getText().toString();
                startDataToCsv(path);

                timer = new Timer();
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        writer.writeNext(new String[]{
                                getCurrentDateTime().toString(),
                                +Value.SPEED+"",
                                +Value.ACC+"",
                                +Value.GYRO_X+"",
                                +Value.GYRO_Y+"",
                                +Value.GYRO_Z+"",
                                Value.RESULT}
                        );
                    }
                };
                Toast.makeText(this,"저장을 시작합니다.",Toast.LENGTH_SHORT).show();
                timer.schedule(timerTask,0,1000);
//              writeDataToCsv(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS) + "/sample.csv");
            } catch (IOException e) {
                Toast.makeText(this.getApplicationContext(), "생성 실패", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            try {
                timerTask.cancel();
                timer.cancel();
                timer.purge();
                Toast.makeText(this,"저장을 종료합니다.",Toast.LENGTH_SHORT).show();
                stopDataToCsv();
            } catch (IOException e) {
                Toast.makeText(this.getApplicationContext(), "정지 실패", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
    public void startDataToCsv(String path) throws IOException {
        file = new FileWriter(path,true);
        writer = new CSVWriter(file);
/**
 *                      getCurrentDateTime().toString()+","
 *                      +Value.SPEED+","
 *                      +Value.ACC+","
 *                      +Value.GYRO_X+","
 *                      +Value.GYRO_Y+","
 *                      +Value.GYRO_Z+","
 *                      +Value.LIGHT
 */
        writer.writeNext(new String[]{
                "time",
                "index",
                "correct",
                "start time",
                "speaking time",});
        writer.writeNext(new String[]{
                getCurrentDateTime().toString(),
                Value.END,
                Value.isCorrect+"",
                Value.delayToSpeak+"",
                Value.delayDuringSpeak+"",});
        String[] category = {"TIME", "SPEED", "ACC","GYRO_X","GYRO_Y","GYRO_Z","RESULT"};
        writer.writeNext(category);

    }
    public void stopDataToCsv() throws IOException{
        writer.close();
    }

    public static String getCurrentDateTime() {
        Date today = new Date();
        Locale currentLocale = new Locale("KOREAN", "KOREA");
        String pattern = "yyyyMMddHHmmss"; //hhmmss로 시간,분,초만 뽑기도 가능
        SimpleDateFormat formatter = new SimpleDateFormat(pattern,
                currentLocale);
        return formatter.format(today);
    }
}
