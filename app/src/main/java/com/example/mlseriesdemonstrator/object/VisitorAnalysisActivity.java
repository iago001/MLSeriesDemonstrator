package com.example.mlseriesdemonstrator.object;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.mlseriesdemonstrator.R;
import com.example.mlseriesdemonstrator.helpers.MLVideoHelperActivity;
import com.example.mlseriesdemonstrator.helpers.vision.VisionBaseProcessor;
import com.example.mlseriesdemonstrator.helpers.vision.agegenderestimation.AgeGenderEstimationProcessor;
import com.google.mlkit.vision.face.Face;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class VisitorAnalysisActivity extends MLVideoHelperActivity implements AgeGenderEstimationProcessor.AgeGenderCallback {

    private Interpreter ageModelInterpreter;
    private Interpreter genderModelInterpreter;

    private int facesCount;
    private int smilingCount;
    private int maleCount;
    private int femaleCount;
    private int kidsCount;
    private int youngCount;
    private int adultCount;
    private int agedCount;

    private Set<Integer> faceTrackingIdSet = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected VisionBaseProcessor setProcessor() {
        try {
            ageModelInterpreter = new Interpreter(FileUtil.loadMappedFile(this, "model_lite_age_q.tflite"), new Interpreter.Options());
            genderModelInterpreter = new Interpreter(FileUtil.loadMappedFile(this, "model_lite_gender_q.tflite"), new Interpreter.Options());
        } catch (IOException e) {
            e.printStackTrace();
        }

        AgeGenderEstimationProcessor ageGenderEstimationProcessor = new AgeGenderEstimationProcessor(
                ageModelInterpreter,
                genderModelInterpreter,
                graphicOverlay,
                this
        );
        ageGenderEstimationProcessor.activity = this;
        return ageGenderEstimationProcessor;
    }

    public void setTestImage(Bitmap cropToBBox) {
        if (cropToBBox == null) {
            return;
        }
        runOnUiThread(() -> ((ImageView) findViewById(R.id.testImageView)).setImageBitmap(cropToBBox));
    }

    @Override
    public void onFaceDetected(Face face, int age, int gender) {
        if (!faceTrackingIdSet.contains(face.getTrackingId())) {
            facesCount++;

            if (face.getSmilingProbability() != null && face.getSmilingProbability() > .79f) {
                smilingCount++;
            }

            if (age < 12) {
                kidsCount++;
            } else if (age < 20) {
                youngCount++;
            } else if (age < 60) {
                adultCount++;
            } else {
                agedCount++;
            }

            if (gender == 0) {
                maleCount++;
            } else {
                femaleCount++;
            }

            StringBuilder builder = new StringBuilder();
            builder.append("Total faces: ").append(facesCount).append(", Smiling: ").append((int) ((smilingCount/(float) facesCount) * 100.0f)).append("%\n")
                    .append("Male: ").append(maleCount).append(", Female: ").append(femaleCount).append("\n")
                    .append("Kids: ").append(kidsCount).append(", Young: ").append(youngCount).append("\n")
                    .append("Adults: ").append(adultCount).append(", Aged: ").append(agedCount);

            setOutputText(builder.toString());
        }
    }
}
