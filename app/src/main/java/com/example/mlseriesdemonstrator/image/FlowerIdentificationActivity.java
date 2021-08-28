package com.example.mlseriesdemonstrator.image;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.example.mlseriesdemonstrator.helpers.MLImageHelperActivity;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;

public class FlowerIdentificationActivity extends MLImageHelperActivity {

    private ImageLabeler imageLabeler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalModel localModel = new LocalModel.Builder().setAssetFilePath("model_flowers.tflite").build();
        CustomImageLabelerOptions options = new CustomImageLabelerOptions.Builder(localModel)
                                                .setConfidenceThreshold(0.7f)
                                                .setMaxResultCount(5)
                                                .build();
        imageLabeler = ImageLabeling.getClient(options);
    }

    @Override
    protected void runDetection(Bitmap bitmap) {
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        imageLabeler.process(inputImage).addOnSuccessListener(imageLabels -> {
            StringBuilder sb = new StringBuilder();
            for (ImageLabel label : imageLabels) {
                sb.append(label.getText()).append(": ").append(label.getConfidence()).append("\n");
            }
            if (imageLabels.isEmpty()) {
                getOutputTextView().setText("Could not identify!!");
            } else {
                getOutputTextView().setText(sb.toString());
            }
        }).addOnFailureListener(e -> {
            e.printStackTrace();
        });
    }
}
