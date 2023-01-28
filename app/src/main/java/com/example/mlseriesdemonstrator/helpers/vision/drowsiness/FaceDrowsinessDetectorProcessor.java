package com.example.mlseriesdemonstrator.helpers.vision.drowsiness;


import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;

import com.example.mlseriesdemonstrator.helpers.vision.FaceGraphic;
import com.example.mlseriesdemonstrator.helpers.vision.GraphicOverlay;
import com.example.mlseriesdemonstrator.helpers.vision.VisionBaseProcessor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/** Face Drowsiness Detector Demo. */
public class FaceDrowsinessDetectorProcessor extends VisionBaseProcessor<List<Face>> {

  private static final String MANUAL_TESTING_LOG = "FaceDetectorProcessor";

  private final FaceDetector detector;
  private final GraphicOverlay graphicOverlay;
  private final HashMap<Integer, FaceDrowsiness> drowsinessHashMap = new HashMap<>();

  public FaceDrowsinessDetectorProcessor(GraphicOverlay graphicOverlay) {
    this.graphicOverlay = graphicOverlay;
    FaceDetectorOptions faceDetectorOptions = new FaceDetectorOptions.Builder()
          .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
          .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
          .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
          .enableTracking()
          .build();
    Log.v(MANUAL_TESTING_LOG, "Face detector options: " + faceDetectorOptions);
    detector = FaceDetection.getClient(faceDetectorOptions);
  }

  @OptIn(markerClass = ExperimentalGetImage.class)
  public Task<List<Face>> detectInImage(ImageProxy imageProxy, Bitmap bitmap, int rotationDegrees) {
    InputImage inputImage = InputImage.fromMediaImage(imageProxy.getImage(), rotationDegrees);
    int rotation = rotationDegrees;

    // In order to correctly display the face bounds, the orientation of the analyzed
    // image and that of the viewfinder have to match. Which is why the dimensions of
    // the analyzed image are reversed if its rotation information is 90 or 270.
    boolean reverseDimens = rotation == 90 || rotation == 270;
    int width;
    int height;
    if (reverseDimens) {
      width = imageProxy.getHeight();
      height =  imageProxy.getWidth();
    } else {
      width = imageProxy.getWidth();
      height = imageProxy.getHeight();
    }
    return detector.process(inputImage)
            .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
              @Override
              public void onSuccess(List<Face> faces) {
                graphicOverlay.clear();
                for (Face face : faces) {
                  if (!drowsinessHashMap.containsKey(face.getTrackingId())) {
                    FaceDrowsiness faceDrowsiness = new FaceDrowsiness();
                    drowsinessHashMap.put(face.getTrackingId(), faceDrowsiness);
                  }
                  boolean isDrowsy = drowsinessHashMap.get(face.getTrackingId()).isDrowsy(face);
                  FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay, face, isDrowsy, width, height);
                  graphicOverlay.add(faceGraphic);
                }
              }
            })
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                // intentionally left empty
              }
            });
  }

  public void stop() {
    detector.close();
  }

  private static void logExtrasForTesting(Face face) {
    if (face != null) {
      Log.v(MANUAL_TESTING_LOG, "face bounding box: " + face.getBoundingBox().flattenToString());
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle X: " + face.getHeadEulerAngleX());
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle Y: " + face.getHeadEulerAngleY());
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle Z: " + face.getHeadEulerAngleZ());

      // All landmarks
      int[] landMarkTypes =
          new int[] {
            FaceLandmark.MOUTH_BOTTOM,
            FaceLandmark.MOUTH_RIGHT,
            FaceLandmark.MOUTH_LEFT,
            FaceLandmark.RIGHT_EYE,
            FaceLandmark.LEFT_EYE,
            FaceLandmark.RIGHT_EAR,
            FaceLandmark.LEFT_EAR,
            FaceLandmark.RIGHT_CHEEK,
            FaceLandmark.LEFT_CHEEK,
            FaceLandmark.NOSE_BASE
          };
      String[] landMarkTypesStrings =
          new String[] {
            "MOUTH_BOTTOM",
            "MOUTH_RIGHT",
            "MOUTH_LEFT",
            "RIGHT_EYE",
            "LEFT_EYE",
            "RIGHT_EAR",
            "LEFT_EAR",
            "RIGHT_CHEEK",
            "LEFT_CHEEK",
            "NOSE_BASE"
          };
      for (int i = 0; i < landMarkTypes.length; i++) {
        FaceLandmark landmark = face.getLandmark(landMarkTypes[i]);
        if (landmark == null) {
          Log.v(
              MANUAL_TESTING_LOG,
              "No landmark of type: " + landMarkTypesStrings[i] + " has been detected");
        } else {
          PointF landmarkPosition = landmark.getPosition();
          String landmarkPositionStr =
              String.format(Locale.US, "x: %f , y: %f", landmarkPosition.x, landmarkPosition.y);
          Log.v(
              MANUAL_TESTING_LOG,
              "Position for face landmark: "
                  + landMarkTypesStrings[i]
                  + " is :"
                  + landmarkPositionStr);
        }
      }
      Log.v(
          MANUAL_TESTING_LOG,
          "face left eye open probability: " + face.getLeftEyeOpenProbability());
      Log.v(
          MANUAL_TESTING_LOG,
          "face right eye open probability: " + face.getRightEyeOpenProbability());
      Log.v(MANUAL_TESTING_LOG, "face smiling probability: " + face.getSmilingProbability());
      Log.v(MANUAL_TESTING_LOG, "face tracking id: " + face.getTrackingId());
    }
  }
}
