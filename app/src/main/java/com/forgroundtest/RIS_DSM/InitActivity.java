package com.forgroundtest.RIS_DSM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

/**
 * 운전자의 상태 파악을 위한 Activity
 * layout : activity_init.xml
 *
 * TODO: N back test 구현 필요
 */

public class InitActivity extends AppCompatActivity {

    private ArrayList<SettingData> testList;
    private ImageButton backBtn;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer = null;

    // 나중에 getter를 이용해 배열 받아오자.
    // 지금은 보안상 너무 취약하다.
    private NBack nBackData = new NBack();
    private long delay1 = 0;
    private long delay2 = 0;
    private long speechLen1 = 0;
    private long speechLen2 = 0;
    private int index = 0;

    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        findViewById(R.id.init_start_monitoring_button).setOnClickListener(v -> startActivity(new Intent(InitActivity.this, PredictionActivity.class) ));

        // index init
        EnglishAppFragment.engIdx = 0;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        backBtn = findViewById(R.id.backbutton);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // init 되자마자 flag false로 초기화
        mDatabase.child("study").child("isEnglishTest").setValue(false);
        mDatabase.child("study").child("isNBackTest").setValue(false);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    int result = tts.setLanguage(Locale.CANADA);
                    if(result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA){
                        Log.e("TTS", "This Language is not supported");
                    }
                    ttsInitialize();
                }else{
                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });

        sttInitialize();

        InitializeSettingData();
        ListView listView = findViewById(R.id.testView);
        final Adapter myAdapter = new Adapter(this, testList);

        listView.setAdapter(myAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                index = position;
                speak(NBack.nBack[position]);
                Log.e("nBack", String.valueOf(position));
            }
        });

        requestRecordAudioPermission();

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
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

    private void ttsInitialize() {
        tts.setSpeechRate(0.50f);
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String s) {
                // stt must be run in main thread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        delay1 = System.currentTimeMillis();
                        startListen();
                    }
                });
            }

            @Override
            public void onError(String s) {

            }
        });
    }

    private void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
    }

    // Initialize The stt service.
    private void sttInitialize() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.e("STT", "ready");
            }

            @Override
            public void onBeginningOfSpeech() {
                delay2 = System.currentTimeMillis();
                speechLen1 = System.currentTimeMillis();
                Log.e("발화까지 걸리는 시간:", String.valueOf(delay2-delay1) + "초");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                Log.e("STT", "speach end");
            }

            @Override
            public void onError(int i) {
                startListen();
            }

            @Override
            public void onResults(Bundle bundle) {
                speechLen2 = System.currentTimeMillis();
                Log.e("발화 시간:", String.valueOf((speechLen2-speechLen1) + "초"));

                ArrayList<String> str = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);


                Log.e("stt", str.get(0));
                if (str.get(0) != null) {
                    answerCheck(str.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
//                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//                String text = "";
//                for (String result : matches)
//                    text += result;
//
//                postSpeech.setText(text);
            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }

    private void answerCheck(String voice) {
        voice = voice.replace("?", "").replace(".", "").toLowerCase();
        String[] nBtest = NBack.nBack[index].split(" ");

        while (voice.contains(" ")) {
            voice = voice.replace(" ", "");
        }
        String[] string = voice.split("");

        int cnt = 0;
        boolean isCorrect = true;

        if (string.length != nBtest.length) {
            int sht = Math.min(string.length, nBtest.length);
            for (int wrong = 0; wrong < sht; wrong++) {
                if (!string[wrong].equals(nBtest[wrong])) {
                    cnt = wrong+1;
                    isCorrect = false;
                }
            }
            if (isCorrect) {
                cnt = sht;
            }
        } else {
            for (int wrong = 0; wrong < string.length; wrong++) {
                if (!string[wrong].equals(nBtest[wrong])) {
                    cnt = wrong+1;
                    isCorrect = false;
                }
            }
        }

        writeNewBackTest(cnt, isCorrect, (int) (delay2-delay1), (int) (speechLen2-speechLen1));
    }

    private void writeNewBackTest(int end, boolean isCorrect, int delayToSpeak, int delayDuringSpeak) {
        // local db에 저장
//        Case testCa = new Case(end, isCorrect, delayToSpeak, delayDuringSpeak);
//        user = FirebaseAuth.getInstance().getCurrentUser();
//        assert user != null;
//        String uid = user.getUid();
//
//        mDatabase.child("user").child(uid).child("nBackTest").child(String.valueOf(index)).setValue(testCa).addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        Log.e("main", "저장성공");
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e("main", "저장실패");
//                    }
//                });
        Value.END = String.valueOf(end);
        Value.isCorrect = String.valueOf(isCorrect);
        Value.delayToSpeak = String.valueOf(delayToSpeak);
        Value.delayDuringSpeak = String.valueOf(delayDuringSpeak);
    }

    private void startListen() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra("android.speech.extra.DICTATION_MODE", true);

        speechRecognizer.startListening(intent);
    }

    public void InitializeSettingData()
    {
        testList = new ArrayList<SettingData>();

        testList.add(new SettingData(R.drawable.ic_baseline_speaker_notes, "n-Back 테스트 1"));
        testList.add(new SettingData(R.drawable.ic_baseline_speaker_notes, "n-Back 테스트 2"));
        testList.add(new SettingData(R.drawable.ic_baseline_speaker_notes, "n-Back 테스트 3"));
        testList.add(new SettingData(R.drawable.ic_baseline_speaker_notes, "n-Back 테스트 4"));
        testList.add(new SettingData(R.drawable.ic_baseline_speaker_notes, "n-Back 테스트 5"));
        testList.add(new SettingData(R.drawable.ic_baseline_speaker_notes, "n-Back 테스트 6"));
        testList.add(new SettingData(R.drawable.ic_baseline_speaker_notes, "n-Back 테스트 7"));
        testList.add(new SettingData(R.drawable.ic_baseline_speaker_notes, "n-Back 테스트 8"));
        testList.add(new SettingData(R.drawable.ic_baseline_speaker_notes, "n-Back 테스트 9"));
        testList.add(new SettingData(R.drawable.ic_baseline_speaker_notes, "n-Back 테스트 10"));
    }

    @Override
    protected void onDestroy() {

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        speechRecognizer.stopListening();
        speechRecognizer.destroy();

        super.onDestroy();
    }
}