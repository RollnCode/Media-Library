package com.rollncode.media_library.utility;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rollncode.media_library.BuildConfig;


/**
 * @author Cheakshov R.(email:roman_woland@mail.ru)
 */
public class ALog {

    // CONSTANT`s
    private final static String TAG = "aLog";

    public static void toLog(@Nullable Object object) {
        if (object == null) {
            object = "";
        }
        if (BuildConfig.DEBUG) {
            Log.i(TAG, object.toString());
        }
    }

    public static void toLog(@NonNull Throwable e) {
        final StringBuilder sb = new StringBuilder();

        sb.append(e.getMessage());
        sb.append('\n');
        sb.append(e.getClass().getName());

        for (StackTraceElement s : e.getStackTrace()) {
            sb.append('\n');
            sb.append(s.toString());
        }

        if (BuildConfig.DEBUG) {
            Log.w(TAG, sb.toString());
        }
    }
}
