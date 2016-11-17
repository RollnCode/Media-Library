package com.rollncode.media_library.interfaces;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;

/**
 * Base
 * @author Tregub Artem tregub.artem@gmail.com
 * @since 10.08.15
 */
public interface ObjectsReceiver {

    void onObjectsReceive(@IdRes int code, @NonNull Object... objects);
}
