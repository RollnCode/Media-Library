package com.rollncode.media_library.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.rollncode.media_library.R;
import com.rollncode.media_library.activity.CameraActivity;
import com.rollncode.media_library.task.AsyncSaveMedia;
import com.rollncode.media_library.utility.CleaningUtils;
import com.rollncode.media_library.utility.Utils;

/**
 * Fragment for displaying video or picture from camera after capturing
 *
 * @author Chekashov R.(email:roman_woland@mail.ru)
 * @since 04.11.16
 */
public class MediaFragment extends BaseFragment {

    private static final String EXTRA_BITMAP = "local.EXTRA_BITMAP";

    private ProgressBar mProgressBar;
    private MediaController mController;

    // VALUES
    private boolean mIsVideo;
    private String mVideoPath;
    private Bitmap mBitmap;

    @NonNull
    @CheckResult
    public static MediaFragment newInstance(@NonNull String filePath) {
        final Bundle args = new Bundle(1);
        args.putString(CameraActivity.EXTRA_FILE_PATH, filePath);

        final MediaFragment fragment = new MediaFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @CheckResult
    public static MediaFragment newInstance(@NonNull Bitmap bitmap) {
        final Bundle args = new Bundle(1);
        args.putParcelable(EXTRA_BITMAP, bitmap);

        final MediaFragment fragment = new MediaFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle b) {
        super.onCreate(b);

        final Bundle bundle = b != null ? b : getArguments();
        initialize(bundle);
    }

    private void initialize(@NonNull Bundle b) {
        if (b.containsKey(CameraActivity.EXTRA_FILE_PATH)) {
            mVideoPath = b.getString(CameraActivity.EXTRA_FILE_PATH);
            mIsVideo = true;

        } else if (b.containsKey(EXTRA_BITMAP)) {
            mBitmap = b.getParcelable(EXTRA_BITMAP);
        }
    }

    @Nullable
    @Override
    protected View onInheritorCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle b) {
        return inflater.inflate(R.layout.fragment_media, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        final VideoView videoView = (VideoView) v.findViewById(R.id.view_view);

        if (b != null) {
            initialize(b);
        }

        if (mIsVideo) {
            add2Gallery(mVideoPath);

            mController = new MediaController(getContext());
            Utils.changeVisibility(videoView, View.VISIBLE);
            videoView.setVideoPath(mVideoPath);
            videoView.setMediaController(mController);
            videoView.setOnPreparedListener(mPreparedListener);
            videoView.seekTo(100);

        } else {
            final ImageView media = (ImageView) v.findViewById(R.id.iv_media);
            final int nh = (int) (mBitmap.getHeight() * (512.0 / mBitmap.getWidth()));
            final Bitmap scaled = Bitmap.createScaledBitmap(mBitmap, 512, nh, true);
            media.setImageBitmap(scaled);
            Utils.changeVisibility(videoView, View.GONE);
        }

        Utils.setOnClickListener(mOnClickListener, v, R.id.iv_back, R.id.iv_next);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (!TextUtils.isEmpty(mVideoPath)) {
            outState.putString(CameraActivity.EXTRA_FILE_PATH, mVideoPath);
        } else {
            outState.putParcelable(EXTRA_BITMAP, mBitmap);
        }
    }

    @Override
    public void onCleanUp() throws Exception {
        CleaningUtils.cleanUp(mProgressBar);
        CleaningUtils.cleanUp(mController);
    }

    private void showProgress(boolean isProgress) {
        Utils.changeVisibility(mProgressBar, isProgress ? View.VISIBLE : View.INVISIBLE);
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.iv_back) {
                getActivity().onBackPressed();

            } else if (view.getId() == R.id.iv_next) {
                showProgress(true);
                if (mIsVideo) {
                    sendResult(mVideoPath);

                } else {
                    new AsyncSaveMedia(MediaFragment.this, mBitmap).execute();
                }
            }
        }
    };

    @Override
    public void onObjectsReceive(int code, @NonNull Object... objects) {
        if (code == R.id.code_async_save_media) {
            showProgress(false);
            final String path = (String) objects[0];
            if (!TextUtils.isEmpty(path)) {
                add2Gallery(path);
                sendResult(path);
            }
        }
    }

    /**
     * This method may receiving for updating gallery on device
     *
     * @param filePath path to file on external storage
     */
    private void add2Gallery(@NonNull String filePath) {
        final ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, filePath);
        values.put(MediaStore.Images.Media.MIME_TYPE, mIsVideo ? "video/mp4" : "image/jpeg");
        getContentResolver().insert(mIsVideo ? Video.Media.EXTERNAL_CONTENT_URI : Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private final OnPreparedListener mPreparedListener = new OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (mController != null) {
                mController.show();
            }
        }
    };

    /**
     * This finish point in capturing process. 
     *
     * @param path   path to file on external storage
     */
    private void sendResult(@NonNull String path) {
        final Bundle bundle = new Bundle(1);
        bundle.putString(CameraActivity.EXTRA_FILE_PATH, path);

        final Intent intent = new Intent();
        intent.putExtras(bundle);

        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }
}
