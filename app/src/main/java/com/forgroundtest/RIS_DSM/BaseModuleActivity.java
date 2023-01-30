package com.forgroundtest.RIS_DSM;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import static com.forgroundtest.RIS_DSM.Value.FILE_NAME;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.forgroundtest.RIS_DSM.Listener.GyroListener;
import com.forgroundtest.RIS_DSM.Listener.STTListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 추론 모델을 위한 background 쓰레드 적용 Activity
 */

public class BaseModuleActivity extends BlunoLibrary {

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
    static CSVWriter writer;

    /**
     * scan 시, 받은 scan 관련 text 저장
     */
    Button scanBtn;
    public String scanStatus = "none"; // ble connection의 상태를 저장하는 String 값
    static StringBuilder recievedSB = new StringBuilder();


    /**
     * firebase 변수
     */
    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    public static int count = 65;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUIHandler = new Handler(getMainLooper());

//        checkPermissions();
        resistSensor();
        resistGyroSensor();
        resistSTT();
        resistLocation();
    }

//    private void checkPermissions() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
//
//                builder.setTitle("블루투스에 대한 액세스가 필요합니다");
//                builder.setMessage("어플리케이션이 블루투스를 감지 할 수 있도록 위치 정보 액세스 권한을 부여하십시오.");
//                builder.setPositiveButton(android.R.string.ok, null);
//
//                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
//                    }
//                });
//                builder.show();
//            }
//
//            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
//
//                builder.setTitle("블루투스에 대한 액세스가 필요합니다");
//                builder.setMessage("어플리케이션이 블루투스를 연결 할 수 있도록 위치 정보 액세스 권한을 부여하십시오.");
//                builder.setPositiveButton(android.R.string.ok, null);
//
//                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 3);
//                    }
//                });
//                builder.show();
//            }
//        }
//    }

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

    protected void startBackgroundThread() {
        // ML 모델 구옫을 위한 별개 쓰래드 실행
        mBackgroundThread = new HandlerThread("ModuleActivity");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
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


    public void resistSTT() {
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

    public void resistLocation() {
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
//        Toast.makeText(this,passiveInterval+"",Toast.LENGTH_SHORT).show();
    }

    public void resistSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    public void resistGyroSensor() {
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        gyroListener = new GyroListener();
    }

    public void startCsvButton() {
        if (START_CSV = !START_CSV) {


            try {
                String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS) + "/" + fileNameEdit.getText().toString();
                FILE_NAME = fileNameEdit.getText().toString();
                startDataToCsv(path);

                timer = new Timer();
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        onStartCogChange();

                        writer.writeNext(new String[]{
                                        getCurrentDateTime().toString(),
                                        +Value.SPEED + "",
                                        +Value.ACC + "",
                                        +Value.GYRO_X + "",
                                        +Value.GYRO_Y + "",
                                        +Value.GYRO_Z + "",
                                        Value.RESULT,
                                        "",
                                        Value.COGNITIVE_LOAD + ""
                                }
                        );
                        /**
                         * firebase 실시간 모니터링 코드 추가
                         * true 판단시에 nback test 실행
                         */
                        mDatabase.child("study").child("isEnglishTest").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Log.e("firebase", "Error getting data", task.getException());
                                } else {
                                    String value = String.valueOf(task.getResult().getValue());
                                    if (value.equals("true")) {
                                        mDatabase.child("study").child("isEnglishTest").setValue(false);
                                        EnglishAppFragment.isEng = true;
                                        EnglishAppFragment.exampi.callOnClick();
                                    }
                                }
                            }
                        });

                        mDatabase.child("study").child("isNBackTest").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Log.e("firebase", "Error getting data", task.getException());
                                } else {
                                    String value = String.valueOf(task.getResult().getValue());
                                    if (value.equals("true")) {
                                        mDatabase.child("study").child("isNBackTest").setValue(false);
                                        EnglishAppFragment.isnBack = true;
                                        EnglishAppFragment.exampi.callOnClick();
                                    }
                                }
                            }
                        });
                    }
                };
                Toast.makeText(this, "저장을 시작합니다.", Toast.LENGTH_SHORT).show();
                timer.schedule(timerTask, 0, 1000);
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
                Toast.makeText(this, "저장을 종료합니다.", Toast.LENGTH_SHORT).show();
                stopDataToCsv();
            } catch (IOException e) {
                Toast.makeText(this.getApplicationContext(), "정지 실패", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void onStartCogChange() {
        onCalculateCogLoad();
    }

    private void onCalculateCogLoad() {
        /**
         * todo 인지부하식 작성 함수
         * 파라미터가 필요하면 작성하3
         * 100 등의 인지부하 연산 결과값을 작성으로 함수 마무리
         */
        double cognitiveLoad = onCalculate();
        // setChangeCogLoad(cognitiveLoad);
        setChangeCogLoad(cognitiveLoad);
    }

    private double onCalculate() {
        // 인지부하 연산.
        double gyro = Math.sqrt(Math.pow((double)Value.GYRO_X,2)+Math.pow((double)Value.GYRO_Y,2)+Math.pow((double)Value.GYRO_Z,2));
        double acc = Value.ACC;
        double speed = Value.SPEED;
        double cognitiveLoad =
                Value.DRIVER_SKILL * (getResultweight(Value.RESULT)*100// 운전자 상태에 따른 가중치
                        +(gyro*100+Math.abs(acc)+speed));
        Value.COGNITIVE_LOAD= cognitiveLoad;
        Log.d("cognitiveload", gyro + " "+acc + " "+speed + " "+gyro + " "+cognitiveLoad);
        return cognitiveLoad;
    }

    private void setChangeCogLoad(double cognitiveLoad) {
        Value.COGNITIVE_LOAD = cognitiveLoad;
    }

    public void startDataToCsv(String path) throws IOException {
        file = new FileWriter(path, true);
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
                "speaking time",
                "response time"});
        writer.writeNext(new String[]{
                getCurrentDateTime().toString(),
                Value.END,
                Value.isCorrect + "",
                Value.delayToSpeak + "",
                Value.delayDuringSpeak + "",
        });
        String[] category = {"TIME", "SPEED", "ACC", "GYRO_X", "GYRO_Y", "GYRO_Z", "RESULT", "RESPONSE", "COGNITIVE"};
        writer.writeNext(category);

    }

    public void stopDataToCsv() throws IOException {
        writer.close();
    }

    public static String getCurrentDateTime() {
        Date today = new Date();
        Locale currentLocale = new Locale("KOREAN", "KOREA");
        String pattern = "yyyy-MM-dd HH:mm:ss"; //hhmmss로 시간,분,초만 뽑기도 가능
        SimpleDateFormat formatter = new SimpleDateFormat(pattern,
                currentLocale);
        return formatter.format(today);
    }

    @Override
    public void onConectionStateChange(connectionStateEnum theConnectionState) {
        switch (theConnectionState) {                                            //Four connection state
            case isConnected:
                scanBtn.setText("Connected");
                break;
            case isConnecting:
                scanBtn.setText("Connecting");
                break;
            case isToScan:
                scanBtn.setText("Scan");
                break;
            case isScanning:
                scanBtn.setText("Scanning");
                break;
            case isDisconnecting:
                scanBtn.setText("isDisconnecting");
                break;
            default:
                break;
        }

    }

    @Override
    public void onSerialReceived(String rString) {
        // 아두이노에서 입력받은 데이터 넣음
        recievedSB.append(rString);
    }

    public double getResultweight(String result){
//        safe
//drowsy driving
//drinking
//operate something
//phone
        double weight=0;
        if(result.equals("safe")) {
            weight = 1;
        }
        else if(result.equals("drowsy driving")) {
            weight = 2.1;
        }
        else if(result.equals("drinking")) {
            weight = 1.2;
        }
        else if(result.equals("operate something")) {
            weight = 1.4;
        }
        else if(result.equals("phone")) {
            weight = 1.35;
        }
        Log.d("predicted",result+" "+weight);
        return weight;

    }
}
