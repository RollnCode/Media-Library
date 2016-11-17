package com.rollncode.media_library.model;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.support.annotation.NonNull;

import java.lang.reflect.Array;

/**
 * @author Maxim Ambroskin kkxmshu@gmail.com
 * @since 02.11.16
 */

public class Photo
        implements Parcelable {

    public static final String[] PROJECTION = {
            Media._ID,
            Media.DATA,
            Media.SIZE,
            Media.DATE_TAKEN,
            Media.WIDTH,
            Media.HEIGHT
    };

    private int mId;
    private String mPath;
    private int mSize;
    private long mDateTaken;
    private int mWidth;
    private int mHeight;

    private static int[] sColumns;

    public Photo() {
    }

    public Photo(@NonNull Cursor cursor) {
        if (sColumns == null) {
            sColumns = new int[PROJECTION.length];
            for (int i = 0; i < sColumns.length; i++) {
                sColumns[i] = cursor.getColumnIndex(PROJECTION[i]);
            }
        }

        mId = cursor.getInt(sColumns[0]);
        mPath = cursor.getString(sColumns[1]);
        mSize = cursor.getInt(sColumns[2]);
        mDateTaken = cursor.getLong(sColumns[3]);
        mWidth = cursor.getInt(sColumns[4]);
        mHeight = cursor.getInt(sColumns[5]);
    }

    public Bitmap getThumbnailBitmap(@NonNull ContentResolver cr) {
        return Thumbnails.getThumbnail(cr, mId, Thumbnails.MICRO_KIND, null);
    }

    public int getId() {
        return mId;
    }

    public String getPath() {
        return mPath;
    }

    public int getSize() {
        return mSize;
    }

    public long getDateTaken() {
        return mDateTaken;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public static Photo[] toEntities(@NonNull Cursor cursor) {
        if (cursor.moveToFirst()) {
            final Photo[] entities = (Photo[]) Array.newInstance(Photo.class, cursor.getCount());

            int count = 0;
            do {
                entities[count++] = new Photo(cursor);

            } while (cursor.moveToNext());

            return entities;
        }
        return null;
    }

    private Photo(Parcel in) {
        mId = in.readInt();
        mPath = in.readString();
        mSize = in.readInt();
        mDateTaken = in.readLong();
        mWidth = in.readInt();
        mHeight = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mPath);
        dest.writeInt(mSize);
        dest.writeLong(mDateTaken);
        dest.writeInt(mWidth);
        dest.writeInt(mHeight);
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        public Photo createFromParcel(Parcel source) {
            return new Photo(source);
        }

        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };
}
