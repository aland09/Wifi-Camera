package com.example.myapplication.utils;
import android.graphics.Bitmap;

/* loaded from: classes.dex */
public interface CameraListener {
    void cameraChanged(int i);

    void connectStateChanged(boolean z, String str);

    void receiveBatteryData(int i);

    void receiveImageData(Bitmap bitmap, int i);

    void resolutionChanged(String str);
}
