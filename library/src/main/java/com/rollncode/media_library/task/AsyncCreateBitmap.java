package com.rollncode.media_library.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.rollncode.media_library.R;
import com.rollncode.media_library.interfaces.ObjectsReceiver;
import com.rollncode.media_library.utility.Utils;

import java.lang.ref.WeakReference;

/**
 * Class which extends AsyncTask and decodes array of bytes to bitmap
 *
 * @author Chekashov R.(email:roman_woland@mail.ru)
 * @since 03.11.16
 */
public class AsyncCreateBitmap extends AsyncTask<Void, Void, Bitmap> {

    private final WeakReference<ObjectsReceiver> mReceiver;
    private final byte[] mData;
    private final String mPath;

    public AsyncCreateBitmap(@NonNull ObjectsReceiver receiver, @Nullable byte[] data, @Nullable String path) {
        mReceiver = new WeakReference<>(receiver);
        mData = data;
        mPath = path;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        if (!TextUtils.isEmpty(mPath)) {
            return BitmapFactory.decodeFile(mPath);
        }
        //noinspection ConstantConditions
        return BitmapFactory.decodeByteArray(mData, 0, mData.length);

    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        Utils.receiveObjects(mReceiver, R.id.code_async_create_bitmap, bitmap);
    }
}
