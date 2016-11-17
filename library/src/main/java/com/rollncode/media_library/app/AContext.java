package com.rollncode.media_library.app;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.WindowManager;

/**
 * @author Chekashov R.(email:roman_woland@mail.ru)
 * @since 06.06.16
 */
public class AContext {

    // VALUE`s
    private final ContentResolver mContentResolver;
    private final WindowManager mWindowManager;

    // SINGLETON
    private static AContext sInstance;

    public static void init(@NonNull Context app) {
        sInstance = new AContext(app);
    }

    @SuppressLint("PrivateResource")
    private AContext(@NonNull Context app) {
        mContentResolver = app.getContentResolver();
        mWindowManager = (WindowManager) app.getSystemService(Context.WINDOW_SERVICE);
    }

    public static ContentResolver getContentResolver() {
        return sInstance.mContentResolver;
    }

    @NonNull
    public static WindowManager getWindowManager() {
        return sInstance.mWindowManager;
    }
}
