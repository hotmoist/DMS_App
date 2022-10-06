package com.forgroundtest.RIS_DSM;

import java.io.File;
import java.util.*;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;
import androidx.core.view.WindowCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;
import org.w3c.dom.Text;

import java.nio.FloatBuffer;

/**
 * 운전자 인지 자원 측정을 위한 Activity
 * layout : activity_prediction.xml
 */

public class PredictionActivity extends AbstractCameraXActivity<PredictionActivity.AnalysisResult> {

    public static final String INTENT_MODULE_ASSET_NAME = "INTENT_MODULE_ASSET_NAME";
    public static final String INTENT_INFO_VIEW_TYPE = "INTENT_INFO_VIEW_TYPE";

    private static final int INPUT_TENSOR_WIDTH = 224;
    private static final int INPUT_TENSOR_HEIGHT = 224;
    private static final int TOP_K = 3;
    private static final int MOVING_AVG_PERIOD = 10;

    static class AnalysisResult {

        private final String[] topNClassNames;
        private final float[] topNScores;
        private final long analysisDuration;
        private final long moduleForwardDuration;

        public AnalysisResult(String[] topNClassNames, float[] topNScores,
                              long moduleForwardDuration, long analysisDuration) {
            this.topNClassNames = topNClassNames;
            this.topNScores = topNScores;
            this.moduleForwardDuration = moduleForwardDuration;
            this.analysisDuration = analysisDuration;
        }
    }

    private TextView mResultText;

    private boolean mAnalyzeImageErrorState;
    private Module mModule;
    private String mModuleAssetName;
    private FloatBuffer mInputTensorBuffer;
    private Tensor mInputTensor;
    private long mMovingAvgSum = 0;
    private Queue<Long> mMovingAvgQueue = new LinkedList<>();

    private LinearLayout layout;

    private TextView mGPSName;
    private TextView mSpeed;
    private TextView mGyroName;
    private TextView mGyroValue;

    private com.google.android.material.button.MaterialButton appStartingBtn;

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_prediction;
    }

    @Override
    protected TextureView getCameraPreviewTextureView() {
        return findViewById(R.id.prediction_texture_view);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if(Build.VERSION.SDK_INT >= 30){
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        }

        layout = (LinearLayout) findViewById(R.id.cognitive_result_layout);
        int w = layout.getWidth();
        int h = layout.getHeight();

        Toast.makeText(this, "weight: " + layout.getWidth() +"| height: " + layout.getHeight(), Toast.LENGTH_SHORT).show();

        layout.setRotation(270.0f);
        layout.setTranslationX((w - h) / 2 + 100);
        layout.setTranslationY((h - w) /2 );

        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) layout.getLayoutParams();
        lp.height = w*2;
        lp.width = 600;
        layout.requestLayout();

        mResultText = findViewById(R.id.prediction_result_textview);
        mSpeed = findViewById(R.id.speed_textview);
        mGyroValue = findViewById(R.id.gyro_textview);

        fileNameEdit = findViewById(R.id.CSV_name);
        csvBtn = findViewById(R.id.start_csv);
        csvBtn.setOnClickListener(view->{startCsvButton();});

        // 영어학습 앱으로 전환
        appStartingBtn = findViewById(R.id.appStartingBtn);
        appStartingBtn.setRotation(270.0f);
        appStartingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // englishLearningFragment로 전환
            }
        });
    }

    protected String getModuleAssetName() {
        if (!TextUtils.isEmpty(mModuleAssetName)) {
            return mModuleAssetName;
        }
        final String moduleAssetNameFromIntent = getIntent().getStringExtra(INTENT_MODULE_ASSET_NAME);
        mModuleAssetName = !TextUtils.isEmpty(moduleAssetNameFromIntent)
                ? moduleAssetNameFromIntent
                : "resnet18.pt";

        return mModuleAssetName;
    }


    @WorkerThread
    @Nullable
    @Override
    protected AnalysisResult analyzeImage(ImageProxy image, int rotationDegrees) {
        if (mAnalyzeImageErrorState){
            return null;
        }
        try{
            if(mModule == null){
                final String moduleFileAbsoluteFilePath = new File(
                        Utils.assetFilePath(this, getModuleAssetName())).getAbsolutePath();
                //module 불러오기
                mModule = Module.load(moduleFileAbsoluteFilePath);

                mInputTensorBuffer =
                        Tensor.allocateFloatBuffer(3 * INPUT_TENSOR_WIDTH * INPUT_TENSOR_HEIGHT);
                mInputTensor = Tensor.fromBlob(mInputTensorBuffer, new long[]{1, 3, INPUT_TENSOR_HEIGHT, INPUT_TENSOR_WIDTH});
            }

            final long startTime = SystemClock.elapsedRealtime();
            TensorImageUtils.imageYUV420CenterCropToFloatBuffer(
                    image.getImage(), rotationDegrees,
                    INPUT_TENSOR_WIDTH, INPUT_TENSOR_HEIGHT,
                    TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                    TensorImageUtils.TORCHVISION_NORM_STD_RGB,
                    mInputTensorBuffer, 0);

            final long moduleForwardStartTime = SystemClock.elapsedRealtime();
            final Tensor outputTensor = mModule.forward(IValue.from(mInputTensor)).toTensor();
            final long moduleForwardDuration = SystemClock.elapsedRealtime() - moduleForwardStartTime;

            final float[] scores = outputTensor.getDataAsFloatArray();
            final int[] ixs = Utils.topK(scores, TOP_K);

            final String[] topKClassNames = new String[TOP_K];
            final float[] topKScores = new float[TOP_K];
            for (int i = 0; i < TOP_K; i++) {
                final int ix = ixs[i];
                topKClassNames[i] = Constants.IMAGENET_CLASSES[ix];
                topKScores[i] = scores[ix];
            }
            final long analysisDuration = SystemClock.elapsedRealtime() - startTime;
            return new AnalysisResult(topKClassNames, topKScores, moduleForwardDuration, analysisDuration);
        }catch (Exception e){
            Log.e("DSM_EDU", "Error during image analysis", e);
            mAnalyzeImageErrorState = true;
            runOnUiThread(() -> {
                if(!isFinishing()){
                    showErrorDialog(v -> PredictionActivity.this.finish());
                }
            });
        }
        return null;
    }

    /**
     * 결과값 UI에 반영하기 위한 메소드
     * UI Thread 에 의해 적용
     * UI 요소는 cognitive_result_page.xml 참고
     * @param result 추론 결과
     */
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    protected void applyToUiAnalyzeImageResult(AnalysisResult result) {
        mResultText.setText(result.topNClassNames[0]);
        mSpeed.setText(String.format("%.2f km/h",Value.SPEED));
        mGyroValue.setText(
                String.format("X : %.2f ",Value.GYRO_X)+
                String.format("Y : %.2f ",Value.GYRO_Y)+
                String.format("Z : %.2f ",Value.GYRO_Z));
        Value.RESULT = result.topNClassNames[0];
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mModule != null){
            mModule.destroy();
        }
    }
}