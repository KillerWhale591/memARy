package com.killerwhale.memary;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * App Entrance
 */
public class MainApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
