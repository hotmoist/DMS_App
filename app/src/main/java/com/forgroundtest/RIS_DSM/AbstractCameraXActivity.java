package com.forgroundtest.RIS_DSM;

import androidx.annotation.NonNull;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.audiofx.PresetReverb;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;

/**
 * cameraX 설정 및 추론 모델 설정을 위한 Activity, 별개 레이아웃 없음
 * @param <R> Analysis result
 */
public abstract class AbstractCameraXActivity<R> extends BaseModuleActivity {
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 200;
    private static final String[] PERMISSIONS = {Manifest.permission.CAMERA};

    private long mLastAnalysisResultTime;

    protected abstract int getContentViewLayoutId();

    protected abstract TextureView getCameraPreviewTextureView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewLayoutId());

        startBackgroundThread();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    REQUEST_CODE_CAMERA_PERMISSION);
        } else {
            setupCameraX();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ( Build.VERSION.SDK_INT >= 23 ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO}, PERMISSION);
        }

        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(
                        this,
                        "You can't use image classification without granting CAMERA permission",
                        Toast.LENGTH_LONG
                ).show();
                finish();
            } else {
                setupCameraX();
            }
        }
    }

    private void setupCameraX() {
        final TextureView textureView = getCameraPreviewTextureView(); // preview 역활을 할  textureview
        final PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setLensFacing(CameraX.LensFacing.FRONT)
                .setTargetResolution(new Size(2336, 1080))
                .build();
        final Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(output -> {
            ViewGroup parent = (ViewGroup) textureView.getParent();
            parent.removeView(textureView);
            parent.addView(textureView, 0);
            textureView.setSurfaceTexture(output.getSurfaceTexture());
            updateTransform();
        });

        final ImageAnalysisConfig imageAnalysisConfig =
                new ImageAnalysisConfig.Builder()
                        .setLensFacing(CameraX.LensFacing.FRONT)
                        .setTargetResolution(new Size(224, 224))
                        .setCallbackHandler(mBackgroundHandler)
                        .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                        .build();
        final ImageAnalysis imageAnalysis = new ImageAnalysis(imageAnalysisConfig);

        imageAnalysis.setAnalyzer(
                (image, rotationDegree) -> {
                    if (SystemClock.elapsedRealtime() - mLastAnalysisResultTime < 500) {
                        return;
                    }

                    final R result = analyzeImage(image, rotationDegree);
                    if (result != null) {
                        mLastAnalysisResultTime = SystemClock.elapsedRealtime();
                        runOnUiThread(() -> applyToUiAnalyzeImageResult(result));
                    }
                });

        CameraX.bindToLifecycle((LifecycleOwner) this, preview, imageAnalysis);
    }

    private void updateTransform(){
        // 필요한가??
        Matrix matrix = new Matrix();
        final TextureView textureView = getCameraPreviewTextureView();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float centerX = w / 2f;
        float centerY = h / 2f;

        int rotationDgr = 0;
        int rotation = (int)getCameraPreviewTextureView().getRotation();

        switch (rotation) {
            case Surface.ROTATION_0:
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }
        matrix.postRotate((float)rotationDgr, centerX, centerY);
        textureView.setTransform(matrix);
    }

    @WorkerThread
    @Nullable
    protected abstract R analyzeImage(ImageProxy image, int rotationDegrees);

    @UiThread
    protected abstract void applyToUiAnalyzeImageResult(R result);
}