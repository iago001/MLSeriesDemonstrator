package com.example.mlseriesdemonstrator;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mlseriesdemonstrator.audio.AudioClassificationActivity;
import com.example.mlseriesdemonstrator.audio.BirdSoundDetectorActivity;
import com.example.mlseriesdemonstrator.image.FlowerIdentificationActivity;
import com.example.mlseriesdemonstrator.image.ImageClassificationActivity;
import com.example.mlseriesdemonstrator.object.DriverDrowsinessDetectionActivity;
import com.example.mlseriesdemonstrator.object.FaceDetectionActivity;
import com.example.mlseriesdemonstrator.object.FaceRecognitionActivity;
import com.example.mlseriesdemonstrator.object.ObjectDetectionActivity;
import com.example.mlseriesdemonstrator.object.ObscureFaceActivity;
import com.example.mlseriesdemonstrator.object.PoseDetectionActivity;
import com.example.mlseriesdemonstrator.object.VisitorAnalysisActivity;
import com.example.mlseriesdemonstrator.text.SpamTextDetectionActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AlgoListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<Algo> arrayList = new ArrayList<>();
        arrayList.add(new Algo(R.drawable.baseline_image_black_48, "Image Classification", ImageClassificationActivity.class));
        arrayList.add(new Algo(R.drawable.baseline_filter_vintage_black_48, "Flower Identification", FlowerIdentificationActivity.class));
        arrayList.add(new Algo(R.drawable.baseline_center_focus_strong_black_48, "Object detection", ObjectDetectionActivity.class));
        arrayList.add(new Algo(R.drawable.baseline_portrait_black_48, "Face detection", FaceDetectionActivity.class));
        arrayList.add(new Algo(R.drawable.baseline_music_note_black_48, "Audio Classification", AudioClassificationActivity.class));
        arrayList.add(new Algo(R.drawable.baseline_flutter_dash_black_48, "Bird Sound Identifier", BirdSoundDetectorActivity.class));
        arrayList.add(new Algo(R.drawable.baseline_comment_black_48, "Spam Text Detector", SpamTextDetectionActivity.class));
        arrayList.add(new Algo(R.drawable.baseline_time_to_leave_black_48, "Driver Drowsiness Detector", DriverDrowsinessDetectionActivity.class));
        arrayList.add(new Algo(R.drawable.baseline_accessibility_black_48, "Pose Detection", PoseDetectionActivity.class));
        arrayList.add(new Algo(R.drawable.baseline_portrait_black_48, "Visitor Analysis", VisitorAnalysisActivity.class));
        arrayList.add(new Algo(R.drawable.baseline_portrait_black_48, "Face recognition", FaceRecognitionActivity.class));
        arrayList.add(new Algo(R.drawable.baseline_portrait_black_48, "Hide/Obscure Face", ObscureFaceActivity.class));

        AlgoAdapter algoAdapter = new AlgoAdapter(arrayList, this);
        RecyclerView recyclerView = findViewById(R.id.main_recycler_view);
        recyclerView.setAdapter(algoAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    @Override
    public void onAlgoSelected(Algo algo) {
        Intent intent = new Intent(this, algo.activityClazz);
        intent.putExtra("name", algo.algoText);
        startActivity(intent);
    }
}

class AlgoAdapter extends RecyclerView.Adapter<AlgoViewHolder> {

    private List<Algo> algoList;
    private AlgoListener algoListener;

    public AlgoAdapter(List<Algo> algoList, AlgoListener listener) {
        this.algoList = algoList;
        this.algoListener = listener;
    }

    @NonNull
    @Override
    public AlgoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icons, parent, false);
        return new AlgoViewHolder(view, algoListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AlgoViewHolder holder, int position) {
        holder.bind(algoList.get(position));
    }

    @Override
    public int getItemCount() {
        return algoList.size();
    }
}

class AlgoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ImageView iconImageView;
    private TextView algoTextView;
    private AlgoListener algoListener;
    private Algo algo;

    public AlgoViewHolder(@NonNull View itemView, AlgoListener algoListener) {
        super(itemView);
        itemView.setOnClickListener(this);
        this.algoListener = algoListener;

        iconImageView = itemView.findViewById(R.id.iconImageView);
        algoTextView = itemView.findViewById(R.id.algoTextView);
    }

    public void bind(Algo algo) {
        this.algo = algo;
        iconImageView.setImageResource(algo.iconResourceId);
        algoTextView.setText(algo.algoText);
    }

    @Override
    public void onClick(View v) {
        if (algoListener != null) {
            algoListener.onAlgoSelected(algo);
        }
    }
}

class Algo<T extends ImageClassificationActivity> {
    public int iconResourceId = R.drawable.ic_launcher_foreground;
    public String algoText = "";
    public Class<T> activityClazz;

    public Algo(int iconResourceId, String algoText, Class<T> activityClazz) {
        this.iconResourceId = iconResourceId;
        this.algoText = algoText;
        this.activityClazz = activityClazz;
    }
}

interface AlgoListener {
    void onAlgoSelected(Algo algo);
}