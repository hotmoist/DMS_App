package com.forgroundtest.RIS_DSM;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.*;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.LiteModuleLoader;
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
//        private final String[] topNClassNames;
//        private final float[] topNScores;
//        private final long analysisDuration;
//        private final long moduleForwardDuration;
//
//        public AnalysisResult(String[] topNClassNames, float[] topNScores,
//                              long moduleForwardDuration, long analysisDuration) {
//            this.topNClassNames = topNClassNames;
//            this.topNScores = topNScores;
//            this.moduleForwardDuration = moduleForwardDuration;
//            this.analysisDuration = analysisDuration;
//        }
            private final ArrayList<Result> mResults;
            public AnalysisResult(ArrayList<Result> results) {
                mResults = results;
            }
    }

    private TextView mResultText;

    private boolean mAnalyzeImageErrorState;
    private Module mModule;
    private ResultView mResultView;
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
    private ImageButton backToPrediction;
    private ConstraintLayout englishAppView;
    private BeforeAppFragment beforeAppFragment;
    private EnglishAppFragment englishAppFragment;
    private FragmentManager fm;
    private FragmentTransaction fTran;


    private Button soundTestBtn;

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_prediction;
    }

    @Override
    protected TextureView getCameraPreviewTextureView() {
        mResultView = findViewById(R.id.prediction_result_view);
        mResultView.setRotation(270.0f);

        int w = mResultView.getWidth();
        int h = mResultView.getHeight();

        Log.v("tag", "width :" + w + "height : " + h);

        mResultView.setTranslationX(-157);
        mResultView.setTranslationY(-157);
        mResultView.requestLayout();

//        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
//        return ((ViewStub) viewGroup.findViewById(R.id.prediction_texture_view_stub))
//                .inflate()
//                .findViewById(R.id.prediction_texture_view);

        return findViewById(R.id.prediction_texture_view);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        request(1000, new OnPermissionsResult() {
            @Override
            public void OnSuccess() {
                Toast.makeText(PredictionActivity.this, "Connection Succeed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnFail(List<String> noPermissions) {
                Toast.makeText(PredictionActivity.this, "Connection Failed", Toast.LENGTH_SHORT).show();
            }
        });


        onCreateProcess();
        serialBegin(115200);

        // 인지자원 페이지 90도 전환을 위함
        layout = (LinearLayout) findViewById(R.id.cognitive_result_layout);
        int w = layout.getWidth();
        int h = layout.getHeight();


        layout.setRotation(270.0f);
        layout.setTranslationX(-157);
//        layout.setTranslationY((h - w) / 2);

        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) layout.getLayoutParams();
        lp.height = 650;
        lp.width = 584;
        layout.requestLayout();

        mResultText = findViewById(R.id.prediction_result_textview);
        mSpeed = findViewById(R.id.speed_textview);
        mGyroValue = findViewById(R.id.gyro_textview);

//        fileNameEdit = findViewById(R.id.CSV_name);
//        csvBtn = findViewById(R.id.start_csv);
//        csvBtn.setOnClickListener(view -> {
//            startCsvButton();
//        });

        // 아두이노 보드(bluno)를 scan 하기 위한 버튼
//        scanBtn = findViewById(R.id.scanBtn);
//        scanBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                buttonScanOnClickProcess();
//            }
//        });

        /**----- serial communication test via sound test -----**/

        //TODO : fragment 또는
//        soundTestBtn = findViewById(R.id.soundTestBtn);
//        soundTestBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                serialSend("1");
//            }
//        });


        /**--------------------------------------------------- **/


        // 빈 frag와 영어학습 어플리케이션과의 화면 전환
//        englishAppView = findViewById(R.id.englishAppView);
//        englishAppView.setRotation(270.0f);
//        appStartingBtn = findViewById(R.id.appStartingBtn);
//        backToPrediction = findViewById(R.id.backToPrediction);
        fm = getSupportFragmentManager();
        fTran = fm.beginTransaction();
        beforeAppFragment = new BeforeAppFragment();
        englishAppFragment = new EnglishAppFragment();
//        fTran.add(R.id.englishApp, beforeAppFragment);
        fTran.commit();

//        appStartingBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                fTran = fm.beginTransaction();
////                fTran.replace(R.id.englishApp, englishAppFragment);
//                fTran.commit();
//            }
//        });

//        backToPrediction.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                fTran = fm.beginTransaction();
////                fTran.replace(R.id.englishApp, beforeAppFragment);
//                fTran.commit();
//            }
//        });
    }

    /**
     * Serial 통신에서 receive 한 데이터
     * 소리에 대한 반응속도의 string값 받음
     * TODO: csv db에 넣는것 구현 필요
     * @param rString
     */
    @Override
    public void onSerialReceived(String rString) {
        super.onSerialReceived(rString);

        if(rString.startsWith("no")){
            Toast.makeText(PredictionActivity.this, rString, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(PredictionActivity.this, rString +"ms", Toast.LENGTH_SHORT).show();
        }

        writer.writeNext(new String[]{
                getCurrentDateTime().toString(),
                "response time:",
                rString+"",});
    }

    protected String getModuleAssetName() {
        if (!TextUtils.isEmpty(mModuleAssetName)) {
            return mModuleAssetName;
        }
        final String moduleAssetNameFromIntent = getIntent().getStringExtra(INTENT_MODULE_ASSET_NAME);
        mModuleAssetName = !TextUtils.isEmpty(moduleAssetNameFromIntent)
                ? moduleAssetNameFromIntent
                : "yolov5s.torchscript.ptl";

        return mModuleAssetName;
    }

    private Bitmap imgToBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }



//    @WorkerThread
//    @Nullable
//    @Override
//    protected AnalysisResult analyzeImage(ImageProxy image, int rotationDegrees) {
//        if (mAnalyzeImageErrorState) {
//            return null;
//        }
//        try {
//            if (mModule == null) {
//                final String moduleFileAbsoluteFilePath = new File(
//                        Utils.assetFilePath(this, getModuleAssetName())).getAbsolutePath();
//                //module 불러오기
//                mModule = Module.load(moduleFileAbsoluteFilePath);
//
//                mInputTensorBuffer =
//                        Tensor.allocateFloatBuffer(3 * INPUT_TENSOR_WIDTH * INPUT_TENSOR_HEIGHT);
//                mInputTensor = Tensor.fromBlob(mInputTensorBuffer, new long[]{1, 3, INPUT_TENSOR_HEIGHT, INPUT_TENSOR_WIDTH});
//            }
//
//            final long startTime = SystemClock.elapsedRealtime();
//            TensorImageUtils.imageYUV420CenterCropToFloatBuffer(
//                    image.getImage(), rotationDegrees,
//                    INPUT_TENSOR_WIDTH, INPUT_TENSOR_HEIGHT,
//                    TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
//                    TensorImageUtils.TORCHVISION_NORM_STD_RGB,
//                    mInputTensorBuffer, 0);
//
//            final long moduleForwardStartTime = SystemClock.elapsedRealtime();
//            final Tensor outputTensor = mModule.forward(IValue.from(mInputTensor)).toTensor();
//            final long moduleForwardDuration = SystemClock.elapsedRealtime() - moduleForwardStartTime;
//
//            final float[] scores = outputTensor.getDataAsFloatArray();
//            final int[] ixs = Utils.topK(scores, TOP_K);
//
//            final String[] topKClassNames = new String[TOP_K];
//            final float[] topKScores = new float[TOP_K];
//            for (int i = 0; i < TOP_K; i++) {
//                final int ix = ixs[i];
//                topKClassNames[i] = Constants.IMAGENET_CLASSES[ix];
//                topKScores[i] = scores[ix];
//            }
//            final long analysisDuration = SystemClock.elapsedRealtime() - startTime;
//            return new AnalysisResult(topKClassNames, topKScores, moduleForwardDuration, analysisDuration);
//        } catch (Exception e) {
//            Log.e("DSM_EDU", "Error during image analysis", e);
//            mAnalyzeImageErrorState = true;
//            runOnUiThread(() -> {
//                if (!isFinishing()) {
//                    showErrorDialog(v -> PredictionActivity.this.finish());
//                }
//            });
//        }
//        return null;
//    }


    @Nullable
    @WorkerThread
    @Override
    protected AnalysisResult analyzeImage(ImageProxy image, int rotationDegrees) {
        try{
            if(mModule == null){
                mModule = LiteModuleLoader.load(PredictionActivity.assetFilePath(getApplicationContext(), "yolov5s.torchscript.ptl"));
                BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("classes.txt")));
                String line;
                List<String> classes = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    classes.add(line);
                }
                PrePostProcessor.mClasses = new String[classes.size()];
                classes.toArray(PrePostProcessor.mClasses);
            }
        } catch (IOException e){
            Log.e("DMS", "Error reading assets", e);
            return null;
        }

        Bitmap bitmap = imgToBitmap(image.getImage());
        Matrix matrix = new Matrix();
        matrix.setRotate(270.0f);
//        matrix.setScale(-1, 1); // 좌우 반전
        matrix.setScale(1, -1);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);

        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
        IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple();
        final Tensor outputTensor = outputTuple[0].toTensor();
        final float[] outputs = outputTensor.getDataAsFloatArray();

        float imgScaleX = (float)bitmap.getWidth() / PrePostProcessor.mInputWidth;
        float imgScaleY = (float)bitmap.getHeight() / PrePostProcessor.mInputHeight;
        float ivScaleX = (float)mResultView.getWidth() / bitmap.getWidth();
        float ivScaleY = (float)mResultView.getHeight() / bitmap.getHeight();

        final ArrayList<Result> results = PrePostProcessor.outputsToNMSPredictions(outputs, imgScaleX, imgScaleY, ivScaleX, ivScaleY, 0, 0);

        return new AnalysisResult(results);
    }

    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    /**
     * 결과값 UI에 반영하기 위한 메소드
     * UI Thread 에 의해 적용
     * UI 요소는 cognitive_result_page.xml 참고
     *
     * @param result 추론 결과
     */
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    protected void applyToUiAnalyzeImageResult(AnalysisResult result) {
//        if(result.mResults != null) return;
        for( Result tResult: result.mResults ) {
            mResultText.setText(PrePostProcessor.mClasses[tResult.classIndex]);
        }
//        Value.RESULT = result.topNClassNames[0];
        mResultView.setResults(result.mResults);
        mResultView.invalidate();
        mSpeed.setText(String.format("%.2f km/h", Value.SPEED));
        mGyroValue.setText(
                String.format("X : %.2f ", Value.GYRO_X) +
                        String.format("Y : %.2f ", Value.GYRO_Y) +
                        String.format("Z : %.2f ", Value.GYRO_Z));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroyProcess();
        if (mModule != null) {
            mModule.destroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onResumeProcess();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        onActivityResultProcess(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();
    }

    @Override
    protected void onStop() {
        super.onStop();
        onStopProcess();
    }


}