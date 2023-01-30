package com.forgroundtest.RIS_DSM.Model;


import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.forgroundtest.RIS_DSM.Value.FILE_NAME;

import android.icu.text.SimpleDateFormat;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import android.content.Context;

import androidx.annotation.NonNull;

import com.forgroundtest.RIS_DSM.BaseModuleActivity;
import com.forgroundtest.RIS_DSM.Value;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;

public class BaseRunner {
    private final String match = "[^\uAC00-\uD7A30-9a-zA-Z]";
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private long mNow;
    private Date mDate;
    private FileWriter file= null;
    private CSVWriter writer;

    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    public BaseRunner() {

    }

    public boolean onCheckAnswer(Context context, String realAnswer, String userAnswer, int index, double delay) {
        String realAn = realAnswer.toLowerCase();
        String userAn = userAnswer.toLowerCase();
        realAn = realAn.replaceAll(match, "");
        userAn = userAn.replaceAll(match, "");
        String[] realAnArr = realAn.split("");
        String[] userAnArr = userAn.split("");
        int cnt = 0;
        boolean isCorrect = false;

        if (realAnArr.length != userAnArr.length) {
            cnt = (int) Math.abs(realAnArr.length - userAnArr.length);
        } else {
            for (int i = 0; i < realAnArr.length; i++) {
                if (!realAnArr[i].equals(userAnArr[i])) cnt++;
            }
            if (cnt == 0) {
                isCorrect = true;
            }
        }

        double incorrectRate = (double) cnt / realAnArr.length;
        Value.ENG_REACT_WEIGHT = 0.5 * delay * (1 + incorrectRate);

        Log.e("틀린 단어의 개수:", String.valueOf(cnt));

        // write the firebase realtimeDB about 4 diffrent case
        onWriteNewCase(context, index, cnt, isCorrect, 0, 0);

//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                turnPage();
//            }
//        }, 1500);

        return isCorrect;
    }

    public void onWriteNewCase(Context context, int index, int count, boolean isCorrect, int delay1, int delay2) {
        Case ca = new Case(getTime(), count, isCorrect, delay1, delay2);

        // firebase에 저장.
        mDatabase.child("study").child("test").child("englishTest").child(String.valueOf(index-1)).setValue(ca).addOnSuccessListener(new OnSuccessListener<Void>() {
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
            Toast.makeText(context, "파일이 생성되지 않았습니다.", Toast.LENGTH_LONG).show();
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
         * todo 영어 문장 stt 끝난 후 csv 처리
         * wordCnt는 틀린 단어 개수, delayToSpeak와 delayDuringSpeak는 지워도됨.
         */
        /**
         * 저장 컬럼 제목 넣기
         */
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
                count+"",
                isCorrect+"",
                delay1+"",
                delay2+"",});
        try {
            writer.close();
        } catch (IOException e) {
            Toast.makeText(context,"파일이 종료되지않았습니다.",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }
}
