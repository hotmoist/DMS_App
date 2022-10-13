package com.forgroundtest.RIS_DSM;

import static com.forgroundtest.RIS_DSM.Value.FILE_NAME;

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
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EnglishAppFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EnglishAppFragment extends Fragment {

    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private long mNow;
    private Date mDate;

    private String match = "[^\uAC00-\uD7A30-9a-zA-Z]";

    String[] eng = {"Are you all set?", "I'm sure you'll do better next time", "I wish you all the best"};
    String[] kor = {"준비 다 됐어?", "다음에 더 잘할거라고 확신해.", "모든 일이 잘되시길 빌어요."};
    public static int engIdx = 0;
    public static int nBackIdx = 0;
    public static boolean isEng = false;
    public static boolean isnBack = false;
    public static Button exampi;
    private int index = 0;
    private long delay1 = 0;
    private long delay2 = 0;
    private long speechLen1 = 0;
    private long speechLen2 = 0;

    private com.google.android.material.button.MaterialButton appStartBtn;
    private TextToSpeech textToSpeech = null;
    private SpeechRecognizer speechRecognizer = null;

    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

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

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
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
//        leftBtn = rootView.findViewById(R.id.leftbutton);
//        rightBtn = rootView.findViewById(R.id.rightbutton);
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
                turnPage();
            }
        });


//        rightBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                turnPage();
//            }
//        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // exit fragment
            }
        });

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

        sttInitialize();

        replayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak(speechSct.getText().toString());
            }
        });

        requestRecordAudioPermission();

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


                if (str.get(0) != null) {
                    if (!isnBack) {
                        postSpeech.setText(str.get(0));
                        answerCheck();
                    } else {
                        nBackAnswerCheck(str.get(0));
                    }
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

    // Compare answer to nBack Input
    private void nBackAnswerCheck(String voice) {
        // n백 정확도 측정 알고리즘 작성
        // 결과 firebase에 저장.
        voice = voice.replaceAll(match, "");
        voice = voice.replace(" ", "").toLowerCase();
        String[] nBtest = NBack.nBack[index].split(" ");
        Log.e("stt", voice);
        String[] string = voice.split("");

        int cnt = 0;
        boolean isCorrect = true;

        if (string.length != nBtest.length) {
            int sht = Math.min(string.length, nBtest.length);
            for (int wrong = 0; wrong < sht; wrong++) {
                if (!string[wrong].equals(nBtest[wrong])) {
                    cnt = wrong+1;
                    isCorrect = false;
                    break;
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
                    break;
                }
            }
        }

        writeNewBackTest(cnt, isCorrect, (int) (delay2-delay1), (int) (speechLen2-speechLen1));
        Log.e("nBack", "good");
//        turnPage();
    }

    // Compare answer to Input
    private void answerCheck() {
        String voice = speechSct.getText().toString();
        voice = voice.replaceAll(match, "");
        voice = voice.replace(" ", "");
        String[] first = voice.toLowerCase().split(" ");
        String[] post = postSpeech.getText().toString().toLowerCase().split(" ");
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

    private void writeNewBackTest(int end, boolean isCorrect, int delayToSpeak, int delayDuringSpeak) {
        // firebase에 저장.
        isnBack = false;
        Case testCa = new Case(getTime(), end, isCorrect, delayToSpeak, delayDuringSpeak);

        mDatabase.child("study").child("test").child("NBackTest").child(String.valueOf(nBackIdx)).setValue(testCa).addOnSuccessListener(new OnSuccessListener<Void>() {
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

        FileWriter file=null;
        CSVWriter writer;
        try {
            file = new FileWriter("nback_test_number"+FILE_NAME,true);
        } catch (IOException e) {
            e.printStackTrace();
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
                end+"",
                isCorrect+"",
                delayToSpeak+"",
                delayDuringSpeak+"",});
    }


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
        FileWriter file=null;
        CSVWriter writer;
        try {
            file = new FileWriter("nback_test_string"+FILE_NAME,true);
        } catch (IOException e) {
            e.printStackTrace();
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
        writer.writeNext(new String[]{
                "currentTime",
                "count of incorrect words",
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
    }

    // Initialize the tts service.
    private void ttsUtterInitialize() {
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                Log.e("tts", "run");
            }

            @Override
            public void onDone(String s) {
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
                        delay1 = System.currentTimeMillis();
                        startListen();
                    }
                });
            }

            @Override
            public void onError(String s) {
                Log.e("tts", "error");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void turnPage() {
        if ((engIdx+nBackIdx) == 13) {
            // 끝처리
            return;
        }

        progress.setText(((engIdx+nBackIdx)+1) + "/13");
        postSpeech.setText("");
        correct.setBackgroundResource(R.drawable.round_box_g);
        speaking.setBackgroundResource(R.drawable.circleempty);
        speaking.setTextColor(Color.BLACK);
        if (isEng) {
            if (engIdx == eng.length) {
                Toast.makeText(getContext(), "다음 영어 문장이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            speechSct.setText(eng[engIdx]);
            firstKor.setText(kor[engIdx++]);
            speak(speechSct.getText().toString());
        } else {
            if (nBackIdx == NBack.nBack.length) {
                Toast.makeText(getContext(), "다음 nBack data가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            speechSct.setText(NBack.nBack[nBackIdx]);
            firstKor.setText("");
            speak(NBack.nBack[nBackIdx++]);
        }
    }

    private void startListen() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra("android.speech.extra.DICTATION_MODE", true);

        speechRecognizer.startListening(intent);
    }

    private void speak(String text) {
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
                            textToSpeech.setSpeechRate(0.70f);
                        }
                        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
                    }
                } else {
                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });
    }

//    private void saveIdx() {
//        SharedPreferences pref = getActivity().getSharedPreferences("index", Activity.MODE_PRIVATE);
//        SharedPreferences.Editor editor = pref.edit();
//        if (idx == eng.length-1) {
//            editor.putInt("idx", 0);
//            editor.commit();
//        } else {
//            editor.putInt("idx", idx);
//            editor.commit();
//        }
//    }

//    private void loadIdx() {
//        SharedPreferences pref = getActivity().getSharedPreferences("index", Activity.MODE_PRIVATE);
//        int current = pref.getInt("idx", 0);
//        Log.e("main", String.valueOf(current));
//        currentScreen(current);
//    }
//
//    private void currentScreen(int current) {
//        idx = current-1;
//        turnPage();
//    }

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
    }

    @Override
    public void onDestroy() {

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        speechRecognizer.stopListening();
        speechRecognizer.destroy();

        super.onDestroy();
    }

    private String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }
}