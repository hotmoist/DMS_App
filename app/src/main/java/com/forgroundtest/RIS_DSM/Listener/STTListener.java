package com.forgroundtest.RIS_DSM.Listener;

import android.content.Context;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class STTListener implements RecognitionListener {

    private Context mContext = null;

    public STTListener(Context c) {
        this.mContext = c;
    }
    @Override
    public void onReadyForSpeech(Bundle params) {
        Toast.makeText(mContext,"말을 해주세요.",Toast.LENGTH_SHORT).show();
        /**
         * 소리 + 시간 코드 넣기
         */
    }

    @Override
    public void onBeginningOfSpeech() {
        Toast.makeText(mContext,"녹음 중입니다.",Toast.LENGTH_SHORT).show();
        /**
         * 시간 코드 넣기
         */
    }

    @Override
    public void onRmsChanged(float rmsdB) {}

    @Override
    public void onBufferReceived(byte[] buffer) {}

    @Override
    public void onEndOfSpeech() {}

    @Override
    public void onError(int error) {
        String message;

        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "오디오 에러";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "클라이언트 에러";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "퍼미션 없음";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "네트워크 에러";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "네트웍 타임아웃";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "찾을 수 없음";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RECOGNIZER가 바쁨";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "서버가 이상함";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "말하는 시간초과";
                break;
            default:
                message = "알 수 없는 오류임";
                break;
        }

        Toast.makeText(mContext, "에러가 발생하였습니다. : " + message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResults(Bundle results) {
        // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어준다.
        ArrayList<String> matches =
                results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        for(int i = 0; i < matches.size() ; i++){
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {}

    @Override
    public void onEvent(int eventType, Bundle params) {}
}

