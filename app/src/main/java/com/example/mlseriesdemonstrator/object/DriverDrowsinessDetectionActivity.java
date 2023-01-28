package com.example.mlseriesdemonstrator.object;

import android.os.Bundle;

import com.example.mlseriesdemonstrator.helpers.MLVideoHelperActivity;
import com.example.mlseriesdemonstrator.helpers.vision.drowsiness.FaceDrowsinessDetectorProcessor;
import com.example.mlseriesdemonstrator.helpers.vision.VisionBaseProcessor;

public class DriverDrowsinessDetectionActivity extends MLVideoHelperActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected VisionBaseProcessor setProcessor() {
        return new FaceDrowsinessDetectorProcessor(graphicOverlay);
    }
}
