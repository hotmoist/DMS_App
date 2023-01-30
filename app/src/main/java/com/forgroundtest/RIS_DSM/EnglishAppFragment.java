package com.forgroundtest.RIS_DSM;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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

import com.forgroundtest.RIS_DSM.Model.BaseRunner;
import com.forgroundtest.RIS_DSM.Model.Contents;

import java.util.ArrayList;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EnglishAppFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EnglishAppFragment extends Fragment {

    public static int engIdx = 1;
    public static boolean isEng = false;
    public static boolean isnBack = false;
    public static Button exampi;
    private long blankTime = 0L;
    private long delay_1 = 0L;
    private long delay_2 = 0L;
    private double delay = 0.0;

    private boolean check=false;
    private Contents contents;
    private BaseRunner baseRunner;

    private com.google.android.material.button.MaterialButton appStartBtn;
    private TextToSpeech textToSpeech = null;
    private SpeechRecognizer speechRecognizer = null;

    private TextView speaking;
    private TextView speechSct;
    private TextView firstKor;
    private TextView progress;
    private TextView follow;
    private TextView postSpeech;
    private ImageButton blindBtn;
    private ImageButton correct;
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

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_english_app, container, false);

        appStartBtn = getActivity().findViewById(R.id.appStartingBtn);
        appStartBtn.setVisibility(View.INVISIBLE);

        onArrangeAllUI(rootView, inflater, container, savedInstanceState);
        onClearUI();
        onClickBlindBtn();

        setContents(new Contents());
        setBaseRunner(new BaseRunner());
        progress.setText(((engIdx())) + "/" + (contents().english().length-1));

        onClickController();

        // tts Initialize
        setTextToSpeech(new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    int result = textToSpeech().setLanguage(Locale.ENGLISH);
                    if(result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA){
                        Log.e("TTS", "This Language is not supported");
                    }
                    setTTSUtterInitialize();
                } else{
                    Log.e("TTS", "Initialization Failed!");
                }
            }
        }));

        onInitializeSTT();

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
    private void onInitializeSTT() {
        setSpeechToText(SpeechRecognizer.createSpeechRecognizer(getContext()));

        speechToText().setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.e("ORDER", "readyForSpeech");
            }

            @Override
            public void onBeginningOfSpeech() {
                if (isEng()) {
                    delay_2 = System.currentTimeMillis();
                    delay = (double) (delay_2-delay_1) / 1000.0;
                }
                Log.e("ORDER", "beginingOfSpeech");
            }

            @Override
            public void onRmsChanged(float v) {
//                Log.e("ORDER", "rmsChange");
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
                Log.e("ORDER", "listen buffer : "+BaseModuleActivity.getCurrentDateTime());
            }

            @Override
            public void onEndOfSpeech() {
                Log.e("ORDER", "endOfSpeech");
             }

            @Override
            public void onError(int i) {
                Log.e("ORDER", "error");
//                speechRecognizer.stopListening();
                if(!check()) {
                    long blankSpeakTime = System.currentTimeMillis() - blankTime();
                    if (blankSpeakTime > 5000) {
                        if (isEng()) {
                            setIsEng(false);
                        }
                        PredictionActivity.backToPrediction.callOnClick();
                    }
                    Log.e("ORDER", (blankSpeakTime) + "");
                    onStartListenSTT();
                }
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> str = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (str.get(0) == null) return;

                if (isEng()) {
                    postSpeech.setText(str.get(0));
                    if (baseRunner().onCheckAnswer(getContext(), speechSct.getText().toString(), str.get(0), engIdx(), delay)) {
                        correct.setBackgroundResource(R.drawable.ic_baseline_check_24);
                    } else {
                        correct.setBackgroundResource(R.drawable.ic_baseline_priority_high_24);
                    }
                    setCheck(true);
                    setIsEng(false);
                    follow.setText("");
                    onContinue();
                    setEngIdx(engIdx()+1);
                } else {
                    int answer = contents().onCheckContinue(str.get(0));
                    if (answer > 0) {
                        setIsEng(true);
                        onTurnPage();
                    } else if (answer < 0) {
                        PredictionActivity.backToPrediction.callOnClick();
                    } else {
                        onSpeakTTS(contents().againTest());
                    }
                }
                Log.e("ORDER", "result");
            }

            @Override
            public void onPartialResults(Bundle bundle) {
                Log.e("ORDER", "partialResult");
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
                Log.e("ORDER", "event");
            }
        });
    }

    private void onContinue() {
        onClearUI();
        follow.setText("계속 학습을 진행하겠습니까?");
        onSpeakTTS(contents().continueTest());
    }

    // Initialize the tts service.
    private void setTTSUtterInitialize() {
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                setCheck(false);
            }

            @Override
            public void onDone(String s) {
//                Log.e("ORDER", "Speak done : "+ BaseModuleActivity.getCurrentDateTime()+"  -- "+count);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isEng()) {
                            return;
                        }
                        onUIReadyToSpeak();
                    }
                });

                // stt must be run in main thread.
                something.post(new Runnable() {
                    @Override
                    public void run() {
                        setBlankTime(System.currentTimeMillis());
                        onStartListenSTT();
                    }
                });

            }

            @Override
            public void onError(String s) {
                Log.e("ORDER", "TTS error");
            }
        });
    }


    // 영어 문장과 반응 속도 체크 페이지 넘기기.
    @SuppressLint("SetTextI18n")
    public void onTurnPage() {
        if (isEndOfLen()) {
            // 끝처리
            Toast.makeText(getContext(), "다음 영어 문장이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isEng()) {
            return;
        }
        onUIReadyToListen();
        onSpeakTTS(speechSct.getText().toString());
    }

    private void onStartListenSTT() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        if (isEng()) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en_US");
        } else {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko_KR");
        }
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra("android.speech.extra.DICTATION_MODE", true);
        speechToText().startListening(intent);
    }

    // tts speak함수.
    private void onSpeakTTS(String text) {
        if (textToSpeech() != null) {
            textToSpeech().stop();
            textToSpeech().shutdown();
        }

        setTextToSpeech(new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    int result;
                    if (isEng()) {
                        result = textToSpeech().setLanguage(Locale.ENGLISH);
                    } else {
                        result = textToSpeech().setLanguage(Locale.KOREA);
                    }
                    if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                        Log.e("TTS", "This Language is not supported");
                    } else {
                        setTTSUtterInitialize();
                        if (isEng()) {
                            delay_1 = System.currentTimeMillis();
                        }
                        textToSpeech().speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
                    }
                } else {
                    Log.e("TTS", "Initialization Failed!");
                }
            }
        }));
    }


    private boolean check() {
        return check;
    }

    private void setCheck(boolean check) {
        this.check = check;
    }

    @SuppressLint("SetTextI18n")
    private void onUIReadyToListen() {
        progress.setText(((engIdx())) + "/" + String.valueOf(contents().english().length-1));
        postSpeech.setText("");
        correct.setBackgroundResource(R.drawable.round_box_g);
        speaking.setBackgroundResource(R.drawable.circleempty);
        speaking.setTextColor(Color.BLACK);
        follow.setText("");
        speechSct.setText(contents().onSetEnglish(engIdx()));
        firstKor.setText(contents().onSetKorean(engIdx()));
    }

    private void onUIReadyToSpeak() {
        follow.setText("이제 따라해보세요.");
        speaking.setBackgroundResource(R.drawable.circleshape);
        speaking.setTextColor(Color.WHITE);
    }

    private void onClearUI() {
        speechSct.setText("");
        firstKor.setText("");
        follow.setText("");
        postSpeech.setText("");
    }

    private void onArrangeAllUI(View rootView, LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        speechSct = rootView.findViewById(R.id.speechScript);
        firstKor = rootView.findViewById(R.id.firstKor);
        progress = rootView.findViewById(R.id.progress);
        follow = rootView.findViewById(R.id.follow);
        postSpeech = rootView.findViewById(R.id.postSpeech);
        speaking = rootView.findViewById(R.id.speaking);
        something = rootView.findViewById(R.id.something);
        blindBtn = rootView.findViewById(R.id.blind);
        correct = rootView.findViewById(R.id.correct);
        exampi = rootView.findViewById(R.id.exampi);
    }

    private boolean isEndOfLen() {
        return engIdx() == contents().english().length;
    }

    private boolean isZeroIndex() {
        return engIdx() == 0;
    }

    private void onClickBlindBtn() {
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
    }

    private void onClickController() {
        exampi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * controller button 클릭 후 실행되는 블럭
                 */
                if (isnBack()) {
                    /**
                     * TODO: beep sound 및 반응 속도 체크 코드 추가
                     *
                     */
                    ((BaseModuleActivity)getActivity()).serialSend("1");
                    /**
                     * 저장 컬럼 제목 넣기
                     */
                    BaseModuleActivity.writer.writeNext(new String[]{
                            BaseModuleActivity.getCurrentDateTime(),
                            "nback test start",});
                    setIsnBack(false);
                } else {
                    if (isZeroIndex()) {
                        setEngIdx(engIdx()+1);
                    }
                    onTurnPage();
                }
            }
        });
    }

    private TextToSpeech textToSpeech() {
        return textToSpeech;
    }

    private void setTextToSpeech(TextToSpeech textToSpeech) {
        this.textToSpeech = textToSpeech;
    }

    private SpeechRecognizer speechToText() {
        return speechRecognizer;
    }

    private void setSpeechToText(SpeechRecognizer speechToText) {
        this.speechRecognizer = speechToText;
    }

    public static int engIdx() {
        return engIdx;
    }

    public static void setEngIdx(int engIdx) {
        EnglishAppFragment.engIdx = engIdx;
    }

    public static boolean isEng() {
        return isEng;
    }

    public static void setIsEng(boolean isEng) {
        EnglishAppFragment.isEng = isEng;
    }

    public static boolean isnBack() {
        return isnBack;
    }

    public static void setIsnBack(boolean isnBack) {
        EnglishAppFragment.isnBack = isnBack;
    }

    private long blankTime() {
        return this.blankTime;
    }

    private void setBlankTime(long blank) {
        this.blankTime = blank;
    }

    private Contents contents() {
        return this.contents;
    }

    private void setContents(Contents contents) {
        this.contents = contents;
    }

    private BaseRunner baseRunner() {
        return this.baseRunner;
    }

    private void setBaseRunner(BaseRunner baseRunner) {
        this.baseRunner = baseRunner;
    }

    @Override
    public void onStart() {
        super.onStart();
//        speak(speechSct.getText().toString());
        onContinue();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
//        loadIdx();
//        turnPage();
        if (isEndOfLen()) {
            setEngIdx(0);
        }
    }

    @Override
    public void onDestroy() {

        if (textToSpeech() != null) {
            textToSpeech().stop();
            textToSpeech().shutdown();
        }

        if (speechToText() != null) {
            speechToText().stopListening();
            speechToText().destroy();
        }
        super.onDestroy();
    }
}