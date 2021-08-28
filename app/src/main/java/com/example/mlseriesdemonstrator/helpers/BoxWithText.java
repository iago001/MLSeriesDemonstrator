package com.example.mlseriesdemonstrator.helpers;

import android.graphics.Rect;
import android.graphics.RectF;

public class BoxWithText {
    public String text;
    public Rect rect;

    public BoxWithText(String text, Rect rect) {
        this.text = text;
        this.rect = rect;
    }

    public BoxWithText(String displayName, RectF boundingBox) {
        this.text = displayName;
        this.rect = new Rect();
        boundingBox.round(rect);
    }
}
