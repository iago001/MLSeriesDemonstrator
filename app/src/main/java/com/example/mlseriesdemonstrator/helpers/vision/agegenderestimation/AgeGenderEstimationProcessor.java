package com.example.mlseriesdemonstrator.helpers.vision.agegenderestimation;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;

import com.example.mlseriesdemonstrator.helpers.vision.FaceGraphic;
import com.example.mlseriesdemonstrator.helpers.vision.GraphicOverlay;
import com.example.mlseriesdemonstrator.helpers.vision.VisionBaseProcessor;
import com.example.mlseriesdemonstrator.object.VisitorAnalysisActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

public class AgeGenderEstimationProcessor extends VisionBaseProcessor<List<Face>> {

    public interface AgeGenderCallback {
        void onFaceDetected(Face face, int age, int gender);
    }

    private static final String TAG = "AgeGenderEstimationProcessor";

    // Input image size for our age model
    private static final int AGE_INPUT_IMAGE_SIZE = 200;

    // Input image size for our gender model
    private static final int GENDER_INPUT_IMAGE_SIZE = 128;

    private final FaceDetector detector;
    private final Interpreter ageModelInterpreter;
    private final ImageProcessor ageImageProcessor;
    private final Interpreter genderModelInterpreter;
    private final ImageProcessor genderImageProcessor;
    private final GraphicOverlay graphicOverlay;
    private final AgeGenderCallback callback;

    public VisitorAnalysisActivity activity;

    HashMap<Integer, Integer> faceIdAgeMap = new HashMap<>();
    HashMap<Integer, Integer> faceIdGenderMap = new HashMap<>();

    public AgeGenderEstimationProcessor(Interpreter ageModelInterpreter,
                                        Interpreter genderModelInterpreter,
                                        GraphicOverlay graphicOverlay,
                                        AgeGenderCallback callback) {
        this.callback = callback;
        this.graphicOverlay = graphicOverlay;
        // initialize processors
        this.ageModelInterpreter = ageModelInterpreter;
        ageImageProcessor = new ImageProcessor.Builder()
                        .add(new ResizeOp(AGE_INPUT_IMAGE_SIZE, AGE_INPUT_IMAGE_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                        .add(new NormalizeOp(0f, 255f))
                        .build();

        this.genderModelInterpreter = genderModelInterpreter;
        genderImageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(GENDER_INPUT_IMAGE_SIZE, GENDER_INPUT_IMAGE_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0f, 255f))
                .build();

        FaceDetectorOptions faceDetectorOptions = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                // to ensure we don't count and analyse same person again
                .enableTracking()
                .build();
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
                        FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay, face, false, width, height);
                        Log.d(TAG, "face found, id: " + face.getTrackingId());
//                            if (activity != null) {
//                                activity.setTestImage(cropToBBox(bitmap, face.getBoundingBox(), rotation));
//                            }
                        // now we have a face, so we can use that to analyse age and gender
                        Bitmap faceBitmap = cropToBBox(bitmap, face.getBoundingBox(), rotation);

                        if (faceBitmap == null) {
                            Log.d("GraphicOverlay", "Face bitmap null");
                            return;
                        }

                        // We skip further analysis if we have already analysed the face (it may reduce accuracy)
                        if (faceIdAgeMap.containsKey(face.getTrackingId())) {
                            faceGraphic.age = faceIdAgeMap.get(face.getTrackingId());
                            faceGraphic.gender = faceIdGenderMap.get(face.getTrackingId());
                            graphicOverlay.add(faceGraphic);
                            return;
                        }

                        TensorImage tensorImage = TensorImage.fromBitmap(faceBitmap);
                        ByteBuffer ageImageByteBuffer = ageImageProcessor.process(tensorImage).getBuffer();
                        float[][] ageOutputArray = new float[1][1];
                        ageModelInterpreter.run(ageImageByteBuffer, ageOutputArray);

                        // The model returns a normalized value for the age i.e in range ( 0 , 1 ].
                        // To get the age, we multiply the model's output with p.
                        float age = ageOutputArray[0][0] * 116;
                        Log.d(TAG, "face id: " + face.getTrackingId() + ", age: " + age);

                        ByteBuffer genderImageByteBuffer = genderImageProcessor.process(tensorImage).getBuffer();
                        float[][] genderOutputArray = new float[1][2];
                        genderModelInterpreter.run(genderImageByteBuffer, genderOutputArray);
                        int gender;
                        if (genderOutputArray[0][0] > genderOutputArray[0][1]) {
                            // "Male"
                            gender = 0;
                        } else {
                            // "Female"
                            gender = 1;
                        }
                        Log.d(TAG, "face id: " + face.getTrackingId() + ", gender: " + (gender == 0 ? "Male" : "Female"));

                        faceBitmap.recycle();
                        faceIdAgeMap.put(face.getTrackingId(), (int) age);
                        faceIdGenderMap.put(face.getTrackingId(), gender);
                        faceGraphic.age = (int) age;
                        faceGraphic.gender = gender;
                        graphicOverlay.add(faceGraphic);

                        if (callback != null) {
                            callback.onFaceDetected(face, (int) age, gender);
                        }
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

    private Bitmap cropToBBox(Bitmap image, Rect boundingBox, int rotation) {
        int shift = 0;
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
        }
        if (boundingBox.top >= 0 && boundingBox.bottom <= image.getWidth()
                && boundingBox.top + boundingBox.height() <= image.getHeight()
                && boundingBox.left >= 0
                && boundingBox.left + boundingBox.width() <= image.getWidth()) {
            return Bitmap.createBitmap(
                    image,
                    boundingBox.left,
                    boundingBox.top + shift,
                    boundingBox.width(),
                    boundingBox.height()
            );
        } else return null;
    }
}
