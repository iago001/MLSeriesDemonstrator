package com.example.mlseriesdemonstrator.image;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.example.mlseriesdemonstrator.helpers.MLImageHelperActivity;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

public class ImageClassificationActivity extends MLImageHelperActivity {
    private ImageLabeler imageLabeler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageLabeler = ImageLabeling.getClient(new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f)
                .build());
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
               getOutputTextView().setText("Could not classify!!");
           } else {
               getOutputTextView().setText(sb.toString());
           }
        }).addOnFailureListener(e -> {
            e.printStackTrace();
        });
    }
}
