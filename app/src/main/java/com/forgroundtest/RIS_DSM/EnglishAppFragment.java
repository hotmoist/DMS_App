package com.forgroundtest.RIS_DSM;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.forgroundtest.RIS_DSM.Value.ANS;
import static com.forgroundtest.RIS_DSM.Value.FILE_NAME;
import static com.forgroundtest.RIS_DSM.Value.RESULT_END;
import static com.forgroundtest.RIS_DSM.Value.RESULT_START;
import static com.forgroundtest.RIS_DSM.Value.delayToSpeak;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.forgroundtest.RIS_DSM.Model.Case;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EnglishAppFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EnglishAppFragment extends Fragment {

    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private long mNow;
    private Date mDate;
    /**
     * 파일 생성 변수
     */
    FileWriter file= null;
    CSVWriter writer;
    private String match = "[^\uAC00-\uD7A30-9a-zA-Z]";

    String[] eng = {"Are you all set?", "I'm sure you'll do better next time", "I wish you all the best", "Please speak slower", "I'll have to think about it", "He will be home at six", "I got first prize"};
    String[] kor = {"준비 다 됐어?", "다음에 더 잘할거라고 확신해.", "모든 일이 잘되시길 빌어요.", "천천히 말해주세요.", "생각해봐야겠네요.", "그는 6시에 집에 갈거야.", "나는 첫 상금을 탔다."};
    public static int engIdx = 0;
    public static int nBackIdx = 0;
    public static boolean isEng = false;
    public static Button exampi;
    private long delay1 = 0;
    private long delay2 = 0;
    private long speechLen1 = 0;
    private long speechLen2 = 0;
    private long len1 = 0;
    private long len2 = 0;
    private int englishIndex = 0;
    private int cnt = 0;
    long start_record = 0l;
    private boolean isSetting = false;
    int count=0;
    private char[] nBackArr;
    private boolean isSpeaking = false;


    private com.google.android.material.button.MaterialButton appStartBtn;
    private TextToSpeech textToSpeech = null;
    private SpeechRecognizer speechRecognizer = null;

    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private MediaRecorder mediaRecorder;

    private NBack nBack = new NBack();
    private TextView speaking;

    private TextView speechSct;
    private TextView firstKor;
    private TextView progress;
    private TextView follow;
    private TextView postSpeech;
    private ImageButton blindBtn;
    private ImageButton leftBtn;
    private ImageButton rightBtn;
    private ImageButton replayBtn;
    private ImageButton correct;
    private ImageButton backBtn;

    private LinearLayout something;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EnglishAppFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EnglishAppFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EnglishAppFragment newInstance(String param1, String param2) {
        EnglishAppFragment fragment = new EnglishAppFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_english_app, container, false);

        appStartBtn = getActivity().findViewById(R.id.appStartingBtn);
        appStartBtn.setVisibility(View.INVISIBLE);

//        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        speechSct = rootView.findViewById(R.id.speechScript);
        firstKor = rootView.findViewById(R.id.firstKor);
        progress = rootView.findViewById(R.id.progress);
        follow = rootView.findViewById(R.id.follow);
        postSpeech = rootView.findViewById(R.id.postSpeech);
        speaking = rootView.findViewById(R.id.speaking);
        speechSct.setText(eng[engIdx]);
        firstKor.setText(kor[engIdx]);
        progress.setText(((engIdx+nBackIdx)+1) + "/13");
        follow.setText("");
        postSpeech.setText("");
        something = rootView.findViewById(R.id.something);
        blindBtn = rootView.findViewById(R.id.blind);
        replayBtn = rootView.findViewById(R.id.replaybtn);
        correct = rootView.findViewById(R.id.correct);
        backBtn = rootView.findViewById(R.id.backbutton1);
        blindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blindBtn.setSelected(!blindBtn.isSelected());

                if (blindBtn.isSelected()) {
                    speechSct.setVisibility(View.INVISIBLE);
                } else {
                    speechSct.setVisibility(View.VISIBLE);
                }
            }
        });

        exampi = rootView.findViewById(R.id.exampi);
        exampi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * controller button 클릭 후 실행되는 블럭
                 */



                    /**
                     * beep sound 및 반응 속도 체크 코드 추가
                     *
                     */

//                    if (nBackIdx == ) {
//                        Toast.makeText(getContext(), "nBackTest가 더 이상 없습니다.", Toast.LENGTH_SHORT);
//                        return;
//                    }
//                    nBackArr = NBack.nBackKor[nBackIdx++].toCharArray();

//                    try {
//                        String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS) +"/"+FILE_NAME;
//                        file = new FileWriter(path,true);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        Toast.makeText(getContext(),"파일이 생성되지않았습니다.",Toast.LENGTH_LONG).show();
//                    }
//                    writer = new CSVWriter(file);
/**
 *                      getCurrentDateTime().toString()+","
 *                      +Value.SPEED+","
 *                      +Value.ACC+","
 *                      +Value.GYRO_X+","
 *                      +Value.GYRO_Y+","
 *                      +Value.GYRO_Z+","
 *                      +Value.LIGHT
 */
                    /**
                     * 저장 컬럼 제목 넣기
                     */
                    // 난중에 수정할 것. (incorrect한 index list화)
                    BaseModuleActivity.writer.writeNext(new String[]{
                            BaseModuleActivity.getCurrentDateTime(),
                            "nback test start",});

//                    Timer timer = new Timer();
//                    TimerTask timerTask = new TimerTask() {
//                        @Override
//                        public void run() {
////                            if(count == 7) {
////                                count = 0;
////
////                                isSetting = false;
////                                getActivity().runOnUiThread(new Runnable() {
////                                    @Override
////                                    public void run() {
////                                        Log.e("ORDER", "듣기종료 : "+BaseModuleActivity.getCurrentDateTime()+"  -- "+count);
////                                         speechRecognizer.stopListening();
////                                        //speechRecognizer.cancel();
////                                        /**
////                                         * csv 오답 저장
////                                         */
////                                        if (mediaRecorder != null) {
////                                            double db = getDb();
////                                            Log.e("amplitude", String.valueOf(db));
////                                            mediaRecorder.stop();
////                                            mediaRecorder.release();
////                                            mediaRecorder = null;
////                                        }
////
////
////                                    }
////                                });
////                                timer.cancel();
////                                return;
//
//
////                            if (mediaRecorder != null) {
////                                mediaRecorder.stop();
////                                mediaRecorder.release();
////                                mediaRecorder = null;
////                            }
//
////                            getActivity().runOnUiThread(new Runnable() {
////                                @Override
////                                public void run() {
////                                    if (speechRecognizer != null) {
////                                        speechRecognizer.stopListening();
////                                        speechRecognizer.cancel();
////                                        speechRecognizer = null;
////                                    }
////                                }
////                            });
//
////                            speak(String.valueOf(nBackArr[count]));
//
//                            count++;
//                        }
//                    };
//                    start_record = System.currentTimeMillis();
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.e("ORDER", "듣기시작 : "+ BaseModuleActivity.getCurrentDateTime()+"  -- "+count);
//                            sw=true;
//                            startListen();
//                        }
//                    });

                turnPage();
            }
        });


        // tts 초기화
        textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    int result = textToSpeech.setLanguage(Locale.CANADA);
                    if(result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA){
                        Log.e("TTS", "This Language is not supported");
                    }
                    ttsUtterInitialize();
                } else{
                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });

        // check permission to record
        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            requestRecordAudioPermission();
        }

        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);

        return rootView;
    }

    private void requestRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String requiredPermission = Manifest.permission.RECORD_AUDIO;

            // If the user previously denied this permission then show a message explaining why
            // this permission is needed
            if (getActivity().checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 101);
            }
        }
    }

    // Initialize The stt service.
    // 나중에 영어학습 어플 구축할 때 필요함
    boolean sw = true;
    private void sttInitialize() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.e("ORDER", "listen ready : "+BaseModuleActivity.getCurrentDateTime()+"  -- "+count);
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
                Log.e("ORDER", "listen buffer : "+BaseModuleActivity.getCurrentDateTime());
            }

            @Override
            public void onEndOfSpeech() {
                Log.e("ORDER", "listen speach end : "+BaseModuleActivity.getCurrentDateTime()+"  -- "+count);
            }

            @Override
            public void onError(int i) {
                Log.e("ORDER", "listen error : "+BaseModuleActivity.getCurrentDateTime());
                startListen();
            }

            @Override
            public void onResults(Bundle bundle) {

                ArrayList<String> str = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//
                if (str.get(0) != null) {
                    postSpeech.setText(str.get(0));
                    answerCheck();
                }
                speechRecognizer.stopListening();
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }

    // Compare answer to Input
    // 영어 문장 정확도 체크
    private void answerCheck() {
        String voice = speechSct.getText().toString().toLowerCase();
        String userInput = postSpeech.getText().toString().toLowerCase();
        voice = voice.replaceAll(match, "");
        userInput = userInput.replaceAll(match, "");
        String[] first = voice.split("");
        String[] post = userInput.split("");
        int cnt = 0;
        boolean isCorrect = false;

        if (first.length != post.length) {
            cnt = (int) Math.abs(first.length - post.length);
            correct.setBackgroundResource(R.drawable.ic_baseline_priority_high_24);
        } else {
            for (int i = 0; i < first.length; i++) {
                if (!first[i].equals(post[i])) cnt++;
            }
            if (cnt == 0) {
                isCorrect = true;
                correct.setBackgroundResource(R.drawable.ic_baseline_check_24);
            }
            else {
                correct.setBackgroundResource(R.drawable.ic_baseline_priority_high_24);
            }
        }

        follow.setText("");

        Log.e("틀린 단어의 개수:", String.valueOf(cnt));

        // write the firebase realtimeDB about 4 diffrent case
        writeNewCase(engIdx, cnt, isCorrect, (int) (delay2-delay1), (int) (speechLen2-speechLen1));

//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                turnPage();
//            }
//        }, 1500);
    }



    // 영어 문장 정확도 체크 후 base에 write
    private void writeNewCase(int idx, int wordCnt, boolean isCorrect, int delayToSpeak, int delayDuringSpeak) {
        isEng = false;
        Case ca = new Case(getTime(), wordCnt, isCorrect, delayToSpeak, delayDuringSpeak);

        mDatabase.child("study").child("test").child("englishTest").child(String.valueOf(idx)).setValue(ca).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.e("main", "저장성공");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("main", "저장실패");
                    }
                });

        try {
            String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS) +"/nback_test_string"+FILE_NAME;
            file = new FileWriter(path,true);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(),"파일이 생성되지않았습니다.",Toast.LENGTH_LONG).show();
        }
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
        /**
         * 저장 컬럼 제목 넣기
         */
        // 난중에 수정할 것. (incorrect한 index list화)
        writer.writeNext(new String[]{
                "currentTime",
                "starting Point of incorrect",
                "correct",
                "start time",
                "speaking time",});
        /**
         * 저장 컬럼별 데이터
         */
        writer.writeNext(new String[]{
                BaseModuleActivity.getCurrentDateTime().toString(),
                wordCnt+"",
                isCorrect+"",
                delayToSpeak+"",
                delayDuringSpeak+"",});
        try {
            writer.close();
        } catch (IOException e) {
            Toast.makeText(getContext(),"파일이 종료되지않았습니다.",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // Initialize the tts service.
    private void ttsUtterInitialize() {
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                speechLen1 = System.currentTimeMillis();
                Log.e("ORDER", "Speak run : "+ BaseModuleActivity.getCurrentDateTime()+"  -- "+count);
            }

            @Override
            public void onDone(String s) {
                speechLen2 = System.currentTimeMillis();
//                Log.e("ORDER", "Speak done : "+ BaseModuleActivity.getCurrentDateTime()+"  -- "+count);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        follow.setText("이제 따라해보세요.");
                        speaking.setBackgroundResource(R.drawable.circleshape);
                        speaking.setTextColor(Color.WHITE);
                    }
                });

                // stt must be run in main thread.
                something.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isEng) {
                            delay1 = System.currentTimeMillis();
                            startListen();
                        }
                    }
                });

            }

            @Override
            public void onError(String s) {
                Log.e("ORDER", "TTS error");
            }
        });
    }

//    private void mediaRecordStart() throws IOException {
//        try {
//            Log.e("mediaRecord", "listenStart");
//            if (mediaRecorder == null) {
//                mediaRecorder = new MediaRecorder();
//                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.OGG);
//                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.OPUS);
//                mediaRecorder.setOutputFile("/dev/null");
////                mediaRecorder.setMaxDuration(1500);
////                mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
////                    @Override
////                    public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
////                        Log.e("media", "stoplisten");
////                        if (i == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
////                            Log.e("liEnd", String.valueOf(System.currentTimeMillis()-len1));
////
////                            double amplitudeDb = 20 * Math.log10((double) Math.abs(amplitude));
////                            Log.e("db", String.valueOf(amplitude));
////                            mediaRecorder.stop();
////                            mediaRecorder.release();
////                            mediaRecorder = null;
////                        }
////                    }
////                });
//                try {
//                    mediaRecorder.prepare();
//                }catch (java.io.IOException ioe) {
//                    android.util.Log.e("[Monkey]", "IOException: " +
//                            android.util.Log.getStackTraceString(ioe));
//
//                }catch (java.lang.SecurityException e) {
//                    android.util.Log.e("[Monkey]", "SecurityException: " +
//                            android.util.Log.getStackTraceString(e));
//                }
//                try{
//                    mediaRecorder.start();
//                    len1 = System.currentTimeMillis();
//                }catch (java.lang.SecurityException e) {
//                    android.util.Log.e("[Monkey]", "SecurityException: " +
//                            android.util.Log.getStackTraceString(e));
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    // 영어 문장과 반응 속도 체크 페이지 넘기기.
    @SuppressLint("SetTextI18n")
    public void turnPage() {
        if ((engIdx) == eng.length) {
            // 끝처리
            Toast.makeText(getContext(), "다음 영어 문장이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setText(((engIdx)+1) + "/" + String.valueOf(eng.length));
        postSpeech.setText("");
        correct.setBackgroundResource(R.drawable.round_box_g);
        speaking.setBackgroundResource(R.drawable.circleempty);
        speaking.setTextColor(Color.BLACK);
        follow.setText("");
        if (isEng) {
            if (engIdx == eng.length) {
                Toast.makeText(getContext(), "다음 영어 문장이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            speechSct.setText(eng[engIdx]);
            firstKor.setText(kor[engIdx++]);
            speak(speechSct.getText().toString());
        }
    }

// 나중에 stt가 필요함. (지우기 말 것)
    private void startListen() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        sttInitialize();

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra("android.speech.extra.DICTATION_MODE", true);
        speechRecognizer.startListening(intent);
    }

    // tts speak함수.
    private String speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    int result = textToSpeech.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                        Log.e("TTS", "This Language is not supported");
                    } else {
                        ttsUtterInitialize();
                        if (!isEng) {
                            textToSpeech.setSpeechRate(1.50f);
                            textToSpeech.setLanguage(Locale.KOREAN);
                        }
                        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
                    }
                } else {
                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });
        return text;
    }

    // mediaRecorder decibel check.
    private double getDb() {
        int amplitude = mediaRecorder.getMaxAmplitude();
        while (amplitude == 0) {
            amplitude = mediaRecorder.getMaxAmplitude();
        }
        return 20 * Math.log10((double) Math.abs(amplitude));
    }


    @Override
    public void onStart() {
        super.onStart();

//        speak(speechSct.getText().toString());
    }

    @Override
    public void onPause() {
        super.onPause();
////        saveIdx();
//        EnglishAppFragment.engIdx--;
    }

    @Override
    public void onResume() {
        super.onResume();
//        loadIdx();
//        turnPage();
        if (engIdx+nBackIdx == 13) {
            engIdx = 0;
            nBackIdx = 0;
        }
    }

    @Override
    public void onDestroy() {

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
        }

        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        super.onDestroy();
    }

    private String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }
}