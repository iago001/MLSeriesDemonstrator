/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mlseriesdemonstrator.helpers.vision.posedetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;

import com.example.mlseriesdemonstrator.helpers.vision.GraphicOverlay;
import com.example.mlseriesdemonstrator.helpers.vision.VisionBaseProcessor;
import com.example.mlseriesdemonstrator.helpers.vision.posedetector.classification.PoseClassifierProcessor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.odml.image.BitmapMlImageBuilder;
import com.google.android.odml.image.MlImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/** A processor to run pose detector. */
public class PoseDetectorProcessor extends VisionBaseProcessor<PoseDetectorProcessor.PoseWithClassification> {
  private static final String TAG = "PoseDetectorProcessor";

  private final PoseDetector detector;

  private final boolean showInFrameLikelihood;
  private final boolean visualizeZ;
  private final boolean rescaleZForVisualization;
  private final boolean runClassification;
  private final boolean isStreamMode;
  private final Executor classificationExecutor;
  private final Context context;

  private PoseClassifierProcessor poseClassifierProcessor;
  /** Internal class to hold Pose and classification results. */
  protected static class PoseWithClassification {
    private final Pose pose;
    private final List<String> classificationResult;

    public PoseWithClassification(Pose pose, List<String> classificationResult) {
      this.pose = pose;
      this.classificationResult = classificationResult;
    }

    public Pose getPose() {
      return pose;
    }

    public List<String> getClassificationResult() {
      return classificationResult;
    }
  }

  private GraphicOverlay graphicOverlay;
  private PreviewView previewView;

  public PoseDetectorProcessor(
      PoseDetectorOptionsBase options,
      boolean showInFrameLikelihood,
      boolean visualizeZ,
      boolean rescaleZForVisualization,
      boolean runClassification,
      boolean isStreamMode,
      Context context,
      GraphicOverlay graphicOverlay,
      PreviewView previewView) {
    this.graphicOverlay = graphicOverlay;
    this.previewView = previewView;
    this.context = context;
    this.showInFrameLikelihood = showInFrameLikelihood;
    this.visualizeZ = visualizeZ;
    this.rescaleZForVisualization = rescaleZForVisualization;
    detector = PoseDetection.getClient(options);
    this.runClassification = runClassification;
    this.isStreamMode = isStreamMode;
    classificationExecutor = Executors.newSingleThreadExecutor();
  }

  public void stop() {
    detector.close();
  }
  public Task<PoseWithClassification> detectInImage(ImageProxy imageProxy, Bitmap bitmap, int rotationDegrees) {
    MlImage mlImage = new BitmapMlImageBuilder(bitmap).setRotation(rotationDegrees).build();
    int rotation = imageProxy.getImageInfo().getRotationDegrees();
    // In order to correctly display the face bounds, the orientation of the analyzed
    // image and that of the viewfinder have to match. Which is why the dimensions of
    // the analyzed image are reversed if its rotation information is 90 or 270.
    boolean reverseDimens = rotation == 90 || rotation == 270;
    Log.d(TAG, "rotation: " + rotation);
    int width;
    int height;
    if (reverseDimens) {
      width = imageProxy.getHeight();
      height =  imageProxy.getWidth();
    } else {
      width = imageProxy.getWidth();
      height = imageProxy.getHeight();
    }
    return detector
        .process(mlImage)
        .continueWith(
            classificationExecutor,
            task -> {
              Pose pose = task.getResult();
              List<String> classificationResult = new ArrayList<>();
              if (runClassification) {
                if (poseClassifierProcessor == null) {
                  poseClassifierProcessor = new PoseClassifierProcessor(context, isStreamMode);
                }
                classificationResult = poseClassifierProcessor.getPoseResult(pose);
              }
              return new PoseWithClassification(pose, classificationResult);

            }).addOnSuccessListener(new OnSuccessListener<PoseWithClassification>() {
              @Override
              public void onSuccess(PoseWithClassification poseWithClassification) {
                Log.d(TAG, "on Success for pose detector");
                onSuccessPoseClassified(poseWithClassification, width, height);
              }

            }).addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Pose detection failed!", e);
              }
            });
  }

  private void onSuccessPoseClassified(
          @NonNull PoseWithClassification poseWithClassification, int width, int height) {
    graphicOverlay.clear();
    graphicOverlay.add(
        new PoseGraphic(
            graphicOverlay,
            poseWithClassification.pose,
            showInFrameLikelihood,
            visualizeZ,
            rescaleZForVisualization,
            poseWithClassification.classificationResult,
            width,
            height));
  }
}
