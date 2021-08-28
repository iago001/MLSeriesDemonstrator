package com.example.mlseriesdemonstrator.helpers;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mlseriesdemonstrator.R;

public abstract class MLAudioHelperActivity extends BaseHelperActivity {
    public final static int REQUEST_RECORD_AUDIO = 2033;

    protected TextView outputTextView;
    protected TextView specsTextView;
    protected Button startRecordingButton;
    protected Button stopRecordingButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_helper);

        outputTextView = findViewById(R.id.textViewOutput);
        specsTextView = findViewById(R.id.textViewSpec);
        startRecordingButton = findViewById(R.id.buttonStartRecording);
        stopRecordingButton = findViewById(R.id.buttonStopRecording);

        stopRecordingButton.setEnabled(false);

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }

    public void onStartRecording(View view) {
        startRecordingButton.setEnabled(false);
        stopRecordingButton.setEnabled(true);
    }

    public void onStopRecording(View view) {
        startRecordingButton.setEnabled(true);
        stopRecordingButton.setEnabled(false);
    }

}
