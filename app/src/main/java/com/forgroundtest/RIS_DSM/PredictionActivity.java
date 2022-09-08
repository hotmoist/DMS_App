package com.forgroundtest.RIS_DSM;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;

import android.os.Bundle;
import android.view.TextureView;
import android.view.ViewStub;

public class PredictionActivity extends AbstractCameraXActivity<PredictionActivity.AnalysisResult> {

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


    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_prediction;
    }

    @Override
    protected TextureView getCameraPreviewTextureView() {
        return ((ViewStub) findViewById(R.id.prediction_view_stub))
                .inflate()
                .findViewById(R.id.prediction_texture_view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);
    }

    @WorkerThread
    @Nullable
    @Override
    protected AnalysisResult analyzeImage(ImageProxy image, int rotationDegrees) {
        return null;
    }

    @Override
    protected void applyToUiAnalyzeImageResult(AnalysisResult result) {

    }

}