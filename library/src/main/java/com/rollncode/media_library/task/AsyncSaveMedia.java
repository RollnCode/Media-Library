package com.rollncode.media_library.task;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.rollncode.media_library.R;
import com.rollncode.media_library.interfaces.ObjectsReceiver;
import com.rollncode.media_library.utility.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * <p>AsyncTask for saving bitmap in file on external storage
 *
 * @author Chekashov R.(email:roman_woland@mail.ru)
 * @since 03.11.16
 */
public class AsyncSaveMedia extends AsyncTask<Void, Void, String> {

    private static final String JPG = ".jpg";

    private final WeakReference<ObjectsReceiver> mReceiver;
    private final Bitmap mData;
    private final String mDir;

    /**
     * @param receiver callback
     * @param bitmap   bitmap entity for saving in file
     */
    public AsyncSaveMedia(@NonNull ObjectsReceiver receiver, @NonNull Bitmap bitmap) {
        mReceiver = new WeakReference<>(receiver);
        mData = bitmap;
        mDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
    }

    @Override
    protected String doInBackground(Void... voids) {
        FileOutputStream out = null;
        File mediaFile = null;
        final String timeStamp = Utils.SDF_FULL.format(new Date());
        try {
            final File mediaStorageDir = new File(mDir);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }

            mediaFile = new File(mediaStorageDir.getPath() + File.separator + timeStamp + JPG);
            out = new FileOutputStream(mediaFile);
            mData.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return mediaFile != null ? mediaFile.getAbsolutePath() : null;
    }

    /**
     * Return file path
     *
     * @param file path to file which created in doInBackground
     */
    @Override
    protected void onPostExecute(String file) {
        super.onPostExecute(file);

        Utils.receiveObjects(mReceiver, R.id.code_async_save_media, file);
    }
}
