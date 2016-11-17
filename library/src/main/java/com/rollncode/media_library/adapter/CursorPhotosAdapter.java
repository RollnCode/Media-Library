package com.rollncode.media_library.adapter;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.rollncode.media_library.fragment.CameraFragment;
import com.rollncode.media_library.interfaces.ObjectsReceiver;
import com.rollncode.media_library.model.Photo;
import com.rollncode.media_library.viewholder.PhotoViewHolder;

import java.lang.ref.WeakReference;

/**
 * Adapter which using for displaying pictures from gallery
 *
 * @author Maxim Ambroskin kkxmshu@gmail.com
 * @since 08.11.16
 */

public class CursorPhotosAdapter extends RecyclerView.Adapter<PhotoViewHolder> {

    private final WeakReference<ObjectsReceiver> mReceiver;

    private ContentResolver mResolver;
    private Cursor mCursor;
    private boolean mDataValid;
    private int mIdIndex;
    private DataSetObserver mObserver;

    public CursorPhotosAdapter(@NonNull ObjectsReceiver receiver) {
        mReceiver = new WeakReference<>(receiver);
        if (receiver instanceof CameraFragment) {
            mResolver = ((CameraFragment) receiver).getContext().getContentResolver();
        }

        mDataValid = mCursor != null;
        mIdIndex = mDataValid ? mCursor.getColumnIndex(BaseColumns._ID) : -1;
        mObserver = new NotifyingDataSetObserver();
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mObserver);
        }
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PhotoViewHolder(parent, mReceiver);
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder holder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        onBindViewHolder(holder, mCursor);
    }

    private void onBindViewHolder(PhotoViewHolder holder, Cursor cursor) {
        final Photo photo = new Photo(cursor);
        holder.bind(photo, mResolver);
    }

    @Override
    public int getItemCount() {
        return (mDataValid && mCursor != null) ? mCursor.getCount() : 0;
    }

    @Override
    public long getItemId(int position) {
        return (mDataValid && mCursor != null && mCursor.moveToPosition(position)) ?
                mCursor.getLong(mIdIndex) : 0;
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    private Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mObserver != null) {
            oldCursor.unregisterDataSetObserver(mObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mObserver != null) {
                mCursor.registerDataSetObserver(mObserver);
            }
            mIdIndex = newCursor.getColumnIndexOrThrow(BaseColumns._ID);
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mIdIndex = -1;
            mDataValid = false;
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
        }
    }
}
