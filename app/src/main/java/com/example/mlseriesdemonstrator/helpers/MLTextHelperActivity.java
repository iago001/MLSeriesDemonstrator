package com.example.mlseriesdemonstrator.helpers;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mlseriesdemonstrator.R;

public abstract class MLTextHelperActivity extends BaseHelperActivity {

    protected EditText inputEditText;
    protected TextView outputTextView;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mltext_helper);

        inputEditText = findViewById(R.id.txtInput);
        outputTextView = findViewById(R.id.txtOutput);
        sendButton = findViewById(R.id.btnSendText);
    }

    public void onSendButtonClicked(View view) {
        runDetection(inputEditText.getText().toString());
        inputEditText.getText().clear();
    }

    protected abstract void runDetection(String text);

    protected TextView getOutputTextView() {
        return outputTextView;
    }
}