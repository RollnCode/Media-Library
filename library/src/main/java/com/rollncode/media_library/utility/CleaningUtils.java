package com.rollncode.media_library.utility;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Class cleaning utils
 *
 * @author Chekashov R.(email:roman_woland@mail.ru)
 * @since 14.06.16
 */
public class CleaningUtils {

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static <VIEW extends View> void cleanUp(@NonNull VIEW view) {
        cleanUp(view.getBackground());
        if (Build.VERSION.SDK_INT == 15) {
            view.setBackgroundDrawable(null);
        } else {
            view.setBackground(null);
        }
        view.setOnTouchListener(null);
        view.setOnClickListener(null);
        view.setOnLongClickListener(null);
    }

    public static <VIEW extends TextView> void cleanUp(@NonNull VIEW view) {
        cleanUp((View) view);
        view.setCompoundDrawables(null, null, null, null);
    }

    public static <VIEW extends ViewGroup> void cleanUp(@NonNull VIEW view) {
        cleanUp((View) view);
        final int size = view.getChildCount();
        for (int i = 0; i < size; i++) {
            CleaningUtils.cleanUp(view.getChildAt(i));
        }
        view.removeAllViewsInLayout();
    }

    public static <VIEW extends RecyclerView> void cleanUp(@NonNull VIEW view) {
        cleanUp((ViewGroup) view);
        view.setAdapter(null);
    }

    @SuppressWarnings("WeakerAccess")
    public static <DRAWABLE extends Drawable> void cleanUp(@Nullable DRAWABLE drawable) {
        if (drawable != null) {
            drawable.setCallback(null);
        }
    }

    public static <VIEW extends ImageView> void cleanUp(@NonNull VIEW view) {
        cleanUp((View) view);

        cleanUp(view.getDrawable());
        view.setImageDrawable(null);
    }
}
