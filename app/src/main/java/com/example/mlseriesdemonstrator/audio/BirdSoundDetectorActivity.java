package com.example.mlseriesdemonstrator.audio;

import android.media.AudioRecord;
import android.util.Log;
import android.view.View;

import com.example.mlseriesdemonstrator.helpers.MLAudioHelperActivity;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BirdSoundDetectorActivity extends MLAudioHelperActivity {

    String modelPath = "my_birds_model.tflite";
    float probabilityThreshold = 0.3f;
    AudioClassifier classifier;
    private TensorAudio tensor;
    private AudioRecord record;
    private TimerTask timerTask;

    public void onStartRecording(View view) {
        super.onStartRecording(view);

        // Loading the model from the assets folder
        try {
            classifier = AudioClassifier.createFromFile(this, modelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Creating an audio recorder
        tensor = classifier.createInputTensorAudio();

        // showing the audio recorder specification
        TensorAudio.TensorAudioFormat format = classifier.getRequiredTensorAudioFormat();
        String specs = "Number of channels: " + format.getChannels() + "\n"
                            + "Sample Rate: " + format.getSampleRate();
        specsTextView.setText(specs);

        // Creating and start recording
        record = classifier.createAudioRecord();
        record.startRecording();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d(BirdSoundDetectorActivity.class.getSimpleName(), "timer task triggered");
                // Classifying audio data
                // val numberOfSamples = tensor.load(record)
                // val output = classifier.classify(tensor)
                int numberOfSamples = tensor.load(record);
                List<Classifications> output = classifier.classify(tensor);

                // Filtering out classifications with low probability
                List<Category> finalOutput = new ArrayList<>();
//                for (Classifications classifications : output) {
                    for (Category category : output.get(0).getCategories()) {
                        if (category.getLabel().equals("Bird") && category.getScore() > probabilityThreshold) {
                            finalOutput.add(category);
                        }
                    }
//                }

                if (finalOutput.isEmpty()) {
                    return;
                }

                finalOutput = new ArrayList<>();
                for (Category category : output.get(1).getCategories()) {
                    if (category.getScore() > probabilityThreshold) {
                        finalOutput.add(category);
                    }
                }

                // Sorting the results
                Collections.sort(finalOutput, (o1, o2) -> (int) (o1.getScore() - o2.getScore()));

                // Creating a multiline string with the filtered results
                StringBuilder outputStr = new StringBuilder();
                for (Category category : finalOutput) {
                    outputStr.append(category.getLabel())
                            .append(": ").append(category.getScore())
                            .append(", ").append(category.getDisplayName()).append("\n");
                }

                // Updating the UI
                List<Category> finalOutput1 = finalOutput;
                runOnUiThread(() -> {
                    if (finalOutput1.isEmpty()) {
                        outputTextView.setText("Could not identify the bird");
                    } else {
                        outputTextView.setText(outputStr.toString());
                    }
                });
            }
        };

        new Timer().scheduleAtFixedRate(timerTask, 1, 500);
    }

    public void onStopRecording(View view) {
        super.onStopRecording(view);

        timerTask.cancel();
        record.stop();
    }
}
