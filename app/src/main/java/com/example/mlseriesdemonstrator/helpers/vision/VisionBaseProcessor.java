package com.example.mlseriesdemonstrator.helpers.vision;

import android.graphics.Bitmap;

import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Task;

public abstract class VisionBaseProcessor<T> {
    public abstract Task<T> detectInImage(ImageProxy imageProxy, Bitmap bitmap, int rotationDegrees);

    public abstract void stop();
}
