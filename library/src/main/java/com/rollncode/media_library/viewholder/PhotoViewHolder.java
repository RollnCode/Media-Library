package com.rollncode.media_library.viewholder;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rollncode.media_library.R;
import com.rollncode.media_library.interfaces.ObjectsReceiver;
import com.rollncode.media_library.model.Photo;
import com.rollncode.media_library.task.ThumbnailsLoader;
import com.rollncode.media_library.utility.Utils;

import java.lang.ref.WeakReference;

/**
 * ViewHolder for
 * @author Maxim Ambroskin kkxmshu@gmail.com
 * @since 02.11.16
 */

public class PhotoViewHolder extends ViewHolder
        implements OnClickListener {

    private Photo mPhoto;

    private final ImageView mImageView;

    @Nullable
    private final WeakReference<ObjectsReceiver> mReceiver;

    public PhotoViewHolder(@NonNull ViewGroup parent, @Nullable WeakReference<ObjectsReceiver> receiver) {
        super(inflate(parent, R.layout.view_item_photo));
        mReceiver = receiver;

        mImageView = (ImageView) itemView.findViewById(R.id.image);
        itemView.setOnClickListener(this);
    }

    public void bind(@NonNull Photo photo, ContentResolver resolver) {
        mPhoto = photo;
        ThumbnailsLoader.getInstance().load(this, mPhoto);
    }

    public void load(Bitmap bitmap, int id) {
        if (mPhoto.getId() == id) {
            mImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onClick(View v) {
        Utils.receiveObjects(mReceiver, R.id.code_photo_view, mPhoto.getPath());
    }

    private static View inflate(@NonNull ViewGroup parent, @LayoutRes int resId) {
        return LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
    }
}
