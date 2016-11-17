package com.rollncode.media_library.utility;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.rollncode.media_library.interfaces.ObjectsReceiver;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author Chekashov R.(email:roman_woland@mail.ru)
 * @since 03.11.16
 */
public class Utils {

    public static final SimpleDateFormat SDF_FULL = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH);

    @NonNull
    public static String getTag(@NonNull Object object) {
        return object.getClass().getSimpleName();
    }

    public static boolean changeVisibility(@NonNull View v, int visibility) {
        final boolean change = v.getVisibility() != visibility;
        if (change) {
            v.setVisibility(visibility);
        }
        return change;
    }

    public static void setOnClickListener(@Nullable View.OnClickListener listener, @NonNull View parent, @NonNull int... childrenIds) {
        for (int id : childrenIds) {
            parent.findViewById(id).setOnClickListener(listener);
        }
    }

    public static void setOnClickListener(@Nullable View.OnClickListener listener, @NonNull View... views) {
        for (View view : views) {
            view.setOnClickListener(listener);
        }
    }

    public static boolean receiveObjects(@Nullable WeakReference<ObjectsReceiver> weakReceiver, int code, @NonNull Object... objects) {
        final ObjectsReceiver receiver = weakReceiver == null ? null : weakReceiver.get();
        final boolean received = receiver != null;
        if (received) {
            receiver.onObjectsReceive(code, objects);
        }
        return received;
    }
}
