package com.example.cam4pet;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.Image;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cam4pet.util.BorderedText;
import com.example.cam4pet.util.ImageUtils;
import com.example.cam4pet.util.Logger;
import com.example.cam4pet.view.OverlayView;
import com.example.cam4pet.tensorflow.Classifier;
import com.example.cam4pet.tensorflow.MultiBoxTracker;
import com.example.cam4pet.tensorflow.YoloV4Classifier;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


public class DetectFragment extends CameraFragment implements OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();

    private static final int TF_OD_API_INPUT_SIZE = 416;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "yolov4-416-fp32.tflite";

    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco.txt";

    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.3f;
    private static final boolean MAINTAIN_ASPECT = false;
    private Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;
    OverlayView trackingOverlay;
    private Integer sensorOrientation;

    private Classifier detector;
    private String detectResult;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private BorderedText borderedText;

    private static Handler handler;
    private static HandlerThread handlerThread;

    DetectEventListener listener;

    public float ratioWidth;
    public float ratioHeight;

    public interface DetectEventListener {
        void onPetDetected(RectF location);
    }

    public void setDetectEventListener(DetectEventListener listener) {
        this.listener = listener;
    }

    public static DetectFragment newInstance(Context context) {
        DetectFragment detectFragment = new DetectFragment();

        detectFragment.mContext = context;
        if(context instanceof Activity) {
            detectFragment.mActivity = (AppCompatActivity) context;
        }

        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        return detectFragment;
    }

    @Override
    public void onAttach(Context context) {
        LOGGER.d("onAttach " + this);
        super.onAttach(context);

        mContext = context;
        if(context instanceof Activity) {
            mActivity = (AppCompatActivity) context;
        }
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
//        final float textSizePx =
//                TypedValue.applyDimension(
//                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, mActivity.getApplicationContext().getResources().getDisplayMetrics());
//        borderedText = new BorderedText(textSizePx);
//        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(mContext);

        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            detector =
                    YoloV4Classifier.create(
                            mContext.getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_IS_QUANTIZED);
//            detector = TFLiteObjectDetectionAPIModel.create(
//                    getAssets(),
//                    TF_OD_API_MODEL_FILE,
//                    TF_OD_API_LABELS_FILE,
//                    TF_OD_API_INPUT_SIZE,
//                    TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            mContext, "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            mActivity.finish();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) mActivity.findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas);
                        if (isDebug()) {
                            tracker.drawDebug(canvas);
                        }
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }

    @Override
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
//        trackingOverlay.postInvalidate();
        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }

        computingDetection = true;
        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        Runnable run = new Runnable() {
            @Override
            public void run() {
                LOGGER.i("Running detection on image " + currTimestamp);
                final long startTime = SystemClock.uptimeMillis();
                final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                Log.e("CHECK", "run: " + results.size());

//                cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
//                final Canvas canvas = new Canvas(cropCopyBitmap);
//                final Paint paint = new Paint();
//                paint.setColor(Color.RED);
//                paint.setStyle(Paint.Style.STROKE);
//                paint.setStrokeWidth(2.0f);

                float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                switch (MODE) {
                    case TF_OD_API:
                        minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                        break;
                }

                final List<Classifier.Recognition> mappedRecognitions =
                        new LinkedList<Classifier.Recognition>();

                for (final Classifier.Recognition result : results) {
                    final RectF location = result.getLocation();
                    if (location != null && result.getConfidence() >= minimumConfidence) {
//                        canvas.drawRect(location, paint);

                        detectResult = result.getTitle();

                        float paramWidth = 1.5f;
                        float paramHeight = 1.2f;

                        RectF locationModified = new RectF(location.left * ratioWidth * paramWidth, location.top * ratioHeight * paramHeight, location.right * ratioWidth * paramWidth, location.bottom * ratioHeight * paramHeight);

                        if (detectResult.equals("dog") || detectResult.equals("cat")) {
                            LOGGER.i("Detection result " + detectResult);

                            mActivity.runOnUiThread(() -> listener.onPetDetected(locationModified));

                            Toast toast = Toast.makeText(mContext, detectResult + " is Detected", Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        Log.i("DEBUG", ratioWidth + " X " + ratioHeight);
                        Log.i("DEBUG", location.left * ratioWidth * paramWidth + " " + location.top * ratioHeight * paramHeight + " " + location.right * ratioWidth * paramWidth + " " + location.bottom * ratioHeight * paramHeight);

                        result.setLocation(locationModified);
                        mappedRecognitions.add(result);

//                        cropToFrameTransform.mapRect(location);

//                        result.setLocation(location);
//                        mappedRecognitions.add(result);
                    }
                }

                tracker.trackResults(mappedRecognitions, currTimestamp);
                trackingOverlay.postInvalidate();

                computingDetection = false;
            }
        };

        runInBackground(run);
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            LOGGER.i("Runnable run in background");
            handler.post(r);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_cameraconnection;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    protected void setDesiredPreviewFrameSize(Size size) {
        DESIRED_PREVIEW_SIZE = size;
    }

    @Override
    protected void setDesiredRatio(float ratioWidth, float ratioHeight) {
        this.ratioWidth = ratioWidth;
        this.ratioHeight = ratioHeight;
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API;
    }

    @Override
    protected void setUseNNAPI(final boolean isChecked) {
        runInBackground(() -> detector.setUseNNAPI(isChecked));
    }

    @Override
    protected void setNumThreads(final int numThreads) {
        runInBackground(() -> detector.setNumThreads(numThreads));
    }
}