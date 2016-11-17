package com.rollncode.media_library.task;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.provider.MediaStore.Images.Thumbnails;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.rollncode.media_library.app.AContext;
import com.rollncode.media_library.model.Photo;
import com.rollncode.media_library.viewholder.PhotoViewHolder;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class-singleton for loading thumbnails for images from gallery
 *
 * @author Maxim Ambroskin kkxmshu@gmail.com
 * @since 08.11.16
 */

public class ThumbnailsLoader {

    private static final int THREADS_COUNT = 10;

    // SINGLETON
    private static ThumbnailsLoader sInstance;

    // VALUES
    private ExecutorService mExecutor;
    private SparseArray<Future> mTasks;
    private ContentResolver mContentResolver;

    private ThumbnailsLoader() {
        mExecutor = Executors.newFixedThreadPool(THREADS_COUNT);
        mTasks = new SparseArray<>();
        mContentResolver = AContext.getContentResolver();
    }

    public static synchronized ThumbnailsLoader getInstance() {
        if (sInstance == null) {
            sInstance = new ThumbnailsLoader();
        }
        return sInstance;
    }

    public void load(@NonNull PhotoViewHolder holder, @NonNull Photo photo) {
        final int hashCode = holder.hashCode();
        final Future task = mTasks.get(hashCode, null);
        if (task != null) {
            task.cancel(true);
            mTasks.delete(hashCode);
        }

        mTasks.put(hashCode, mExecutor.submit(new LoadRequest(holder, photo, mContentResolver)));
    }

    private void onComplete(WeakReference<PhotoViewHolder> reference, @NonNull Bitmap bitmap, int id) {
        final PhotoViewHolder holder = reference.get();
        if (holder != null) {
            holder.load(bitmap, id);
            mTasks.delete(holder.hashCode());
        }
    }

    private class LoadRequest implements Runnable {

        // TODO: 09.11.16 FileLocked not solved.

        private final WeakReference<PhotoViewHolder> mHolder;
        private final Photo mPhoto;

        LoadRequest(@NonNull PhotoViewHolder holder, Photo photo, @NonNull ContentResolver contentResolver) {
            mHolder = new WeakReference<>(holder);
            mPhoto = photo;
        }

        @Override
        public void run() {
            final Bitmap bitmap = Thumbnails.getThumbnail(mContentResolver, mPhoto.getId(), Thumbnails.MICRO_KIND, null);
            onComplete(mHolder, bitmap, mPhoto.getId());
        }
    }
}
