package com.rollncode.media_library.fragment;

import android.Manifest.permission;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore.Images.Media;
import android.support.annotation.CheckResult;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.rollncode.media_library.R;
import com.rollncode.media_library.activity.CameraActivity;
import com.rollncode.media_library.adapter.CursorPhotosAdapter;
import com.rollncode.media_library.app.AContext;
import com.rollncode.media_library.interfaces.VideoQuality;
import com.rollncode.media_library.model.Photo;
import com.rollncode.media_library.task.AsyncCreateBitmap;
import com.rollncode.media_library.task.AsyncRecordVideo;
import com.rollncode.media_library.utility.CleaningUtils;
import com.rollncode.media_library.utility.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Fragment with camera.
 * <p>This fragment includes actions with camera:
 * <ul>
 * <li>take photos;
 * <li>take videos;
 * <li>change settings for photo and video survey;
 * </ul>
 *
 * @author Chekashov R.(email:roman_woland@mail.ru)
 * @since 03.11.16
 */
@SuppressWarnings("deprecation")
public class CameraFragment extends BaseFragment
        implements LoaderCallbacks<Cursor> {

    private static final String[] PERMISSIONS = {
            permission.CAMERA,
            permission.RECORD_AUDIO,
            permission.READ_EXTERNAL_STORAGE,
            permission.WRITE_EXTERNAL_STORAGE,
    };

    @IntDef({CameraState.AVAILABLE, CameraState.RECORDING})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface CameraState {
        int AVAILABLE = 0xAA;
        int RECORDING = 0xBB;
    }

    private static final int REQUEST_PERMISSIONS = 0xAA;
    private static final int DELAY_MILLIS = 350;

    // VIEWS
    private TextureView mTextureView;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private AppCompatImageButton mBtnChangeCamera;
    private ImageView mButton;
    private Chronometer mChronometer;
    private View mContainer;

    // VALUES
    private ArrayList<Camera.CameraInfo> mCameraInfos;
    private Camera mCamera;
    private MediaRecorder mRecorder;
    private String mFilePath;
    private boolean[] mPermissionsStatus;
    private SurfaceTexture mSurfaceTexture;
    private ObjectAnimator mObjectAnimator;
    private int mCurrentCameraId;
    private int mVideoQuality;
    private int mCameraType;

    // ADAPTER
    private CursorPhotosAdapter mAdapter;

    @NonNull
    @CheckResult
    public static CameraFragment newInstance(int quality) {
        final Bundle bundle = new Bundle(1);
        bundle.putInt(CameraActivity.EXTRA_QUALITY, quality);

        final CameraFragment fr = new CameraFragment();
        fr.setArguments(bundle);

        return fr;
    }

    @Override
    public void onCreate(@Nullable Bundle b) {
        super.onCreate(b);

        mAdapter = new CursorPhotosAdapter(this);

        final Bundle bundle = b == null ? getArguments() : b;
        mVideoQuality = bundle.getInt(CameraActivity.EXTRA_QUALITY);
    }

    @Nullable
    @Override
    protected View onInheritorCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle b) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);

        mTextureView = (TextureView) v.findViewById(R.id.texture_view);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        mChronometer = (Chronometer) v.findViewById(R.id.chronometer);
        mBtnChangeCamera = (AppCompatImageButton) v.findViewById(R.id.iv_change_camera);
        mButton = (ImageView) v.findViewById(R.id.iv_take_photo);
        mContainer = v.findViewById(R.id.container);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setAdapter(mAdapter);

        Utils.changeVisibility(mChronometer, View.INVISIBLE);
        Utils.setOnClickListener(mOnClickListener, mButton, mBtnChangeCamera);
        mButton.setOnLongClickListener(mOnLongClickListener);
        mButton.setTag(CameraState.AVAILABLE);

        mObjectAnimator = ObjectAnimator.ofFloat(mBtnChangeCamera, "rotationX", 0.0f, 360f);
        mObjectAnimator.setDuration(500);
        mObjectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        if (b != null) {
            mVideoQuality = b.getInt(CameraActivity.EXTRA_QUALITY);
        }

        if (mVideoQuality == Integer.MAX_VALUE) {
            showQualityDialog();

        } else {
            askForPermission(PERMISSIONS, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            stop();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CameraActivity.EXTRA_QUALITY, mVideoQuality);
    }

    private void showProgress(boolean isProgress) {
        Utils.changeVisibility(mProgressBar, isProgress ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     *
     * @param permissions required access permissions
     * @return permissions is granted
     */
    private boolean[] checkPermissions(@NonNull String[] permissions) {
        final boolean[] result = new boolean[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            result[i] = ContextCompat.checkSelfPermission(getContext(), permissions[i]) == PackageManager.PERMISSION_GRANTED;
        }
        return result;
    }

    /**
     * Initialize cameras.
     *
     */
    private void cameraGranted() {
        final int count = Camera.getNumberOfCameras();
        mCameraInfos = new ArrayList<>(count);
        final Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < count; i++) {
            Camera.getCameraInfo(i, info);
            mCameraInfos.add(info);
        }

        mTextureView.setSurfaceTextureListener(mTextureListener);
        if (mTextureView.isAvailable()) {
            mTextureListener.onSurfaceTextureAvailable(mTextureView.getSurfaceTexture()
                    , mTextureView.getWidth(), mTextureView.getHeight());
        }
    }

    private void storageGranted() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Utils.changeVisibility(mRecyclerView, View.VISIBLE);
            getLoaderManager().initLoader(0, null, this);

        } else {
            Utils.changeVisibility(mRecyclerView, View.GONE);
        }
    }

    @Override
    public void onCleanUp() throws Exception {
        CleaningUtils.cleanUp(mRecyclerView);
        CleaningUtils.cleanUp(mTextureView);
        CleaningUtils.cleanUp(mBtnChangeCamera);
        CleaningUtils.cleanUp(mButton);
        CleaningUtils.cleanUp(mProgressBar);
        CleaningUtils.cleanUp(mChronometer);
        CleaningUtils.cleanUp(mContainer);
    }

    @Override
    public CursorLoader onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), Media.EXTERNAL_CONTENT_URI, Photo.PROJECTION, null, null, Photo.PROJECTION[3] + " DESC");
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    private final TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mSurfaceTexture = surface;
            mCurrentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            setCameraType(-1);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            final boolean destroyed;
            if (destroyed = mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
            return destroyed;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    /**
     * Method for asking permission for work with camera/storage
     */
    private void askForPermission(@NonNull String[] permissions, int requestCode) {
        mPermissionsStatus = checkPermissions(permissions);
        final ArrayList<String> list = new ArrayList<>(mPermissionsStatus.length);

        for (int i = 0; i < mPermissionsStatus.length; i++) {
            if (mPermissionsStatus[i]) {
                switch (permissions[i]) {
                    case permission.CAMERA:
                        cameraGranted();
                        break;

                    case permission.READ_EXTERNAL_STORAGE:
                        storageGranted();
                        break;
                }

            } else {
                list.add(PERMISSIONS[i]);
            }
        }

        if (list.size() > 0) {
            requestPermissions(list.toArray(new String[list.size()]), requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            switch (permissions[i]) {
                case permission.CAMERA:
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        mPermissionsStatus[0] = true;
                        cameraGranted();
                    }
                    break;

                case permission.RECORD_AUDIO:
                    mPermissionsStatus[1] = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                    break;

                case permission.READ_EXTERNAL_STORAGE:
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        mPermissionsStatus[2] = mPermissionsStatus[3] = true;
                        storageGranted();
                    } else {
                        Utils.changeVisibility(mRecyclerView, View.GONE);
                    }
                    break;
            }
        }
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.iv_change_camera) {
                if (mRecorder != null) {
                    return;
                }
                startAnimation();
                setCameraType(0);

            } else if (view.getId() == R.id.iv_take_photo) {
                if (!mPermissionsStatus[2]) {
                    askForPermission(PERMISSIONS, REQUEST_PERMISSIONS);

                } else if ((int) mButton.getTag() == CameraState.AVAILABLE) {
                    mButton.setEnabled(false);
                    showTakePhotoEffect();
                    takeSnapShots();
                }
            }
        }
    };

    private void takeSnapShots() {
        showProgress(true);
        mCamera.takePicture(null, null, mPictureCallback);
    }

    /**
     * picture call back
     */
    private final Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            new AsyncCreateBitmap(CameraFragment.this, data, null).execute();
        }
    };

    private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            final boolean available = mRecorder == null;
            if (mPermissionsStatus[2] && available) {
                final int degree = (int) mTextureView.getTag();
                getActivity().setRequestedOrientation(degree == 90 ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        : ActivityInfo.SCREEN_ORIENTATION_LOCKED);

                mButton.setTag(CameraState.RECORDING);
                mButton.setImageResource(R.drawable.svg_recording);

                Utils.changeVisibility(mChronometer, View.VISIBLE);
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.start();
                new AsyncRecordVideo(CameraFragment.this, mCamera).execute(mPermissionsStatus[1],
                        mTextureView.getTag(), mFilePath = videoPath().getAbsolutePath(), mVideoQuality);
                return true;

            } else if (!available) {
                stop();
                startFragment(MediaFragment.newInstance(mFilePath), true);
                return true;

            } else {
                askForPermission(PERMISSIONS, REQUEST_PERMISSIONS);
            }
            return false;
        }
    };

    private static File videoPath() {
        final File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        final String timestamp = Utils.SDF_FULL.format(new Date());
        return new File(storageDir, VIDEO_PREFIX + timestamp + VIDEO_EXTENSION);
    }

    private void stop() {
        Utils.changeVisibility(mChronometer, View.INVISIBLE);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        mChronometer.stop();
        try {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
        } catch (RuntimeException ignore) {
        } finally {
            mRecorder = null;
        }

        mButton.setImageResource(R.drawable.svg_circle_white);
        mButton.setTag(CameraState.AVAILABLE);
    }

    @Override
    public void onObjectsReceive(int code, @NonNull Object... objects) {
        if (code == R.id.code_async_create_bitmap) {
            mButton.setEnabled(true);
            showProgress(false);
            final Bitmap bitmap = (Bitmap) objects[0];
            if (bitmap != null) {
                startFragment(MediaFragment.newInstance(bitmap), true, true);
            }

        } else if (code == R.id.code_async_record_video) {
            mRecorder = (MediaRecorder) objects[0];

        } else if (code == R.id.code_photo_view) {
            showProgress(true);
            final String path = (String) objects[0];
            new AsyncCreateBitmap(CameraFragment.this, null, path).execute();
        }
    }

    /**
     * Change camera type(back or frontal)
     */
    private void setCameraType(final int type) {
        mCameraType = type;
        new Thread(mChangeCameraTypeRunnable).start();
    }

    private void showQualityDialog() {
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());
        builderSingle.setTitle(R.string.Quality_dialog_title);
        builderSingle.setCancelable(false);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_selectable_list_item, VideoQuality.getStrings());
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                askForPermission(PERMISSIONS, REQUEST_PERMISSIONS);
                mVideoQuality = VideoQuality.getType(which);
            }
        });
        builderSingle.show();
    }

    /**
     * Start animation for change camera button type
     */
    private void startAnimation() {
        mObjectAnimator.start();
    }

    /**
     * Runnable which using
     */
    private final Runnable mChangeCameraTypeRunnable = new Runnable() {

        @Override
        public void run() {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }

            if (mCameraType == -1) {
                mCurrentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

            } else {
                if (mCurrentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mCurrentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;

                } else {
                    mCurrentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
            }
            mCamera = Camera.open(mCurrentCameraId);

            { // this block of code sets degree for camera according to orientation
                final int degree = 90 * AContext.getWindowManager().getDefaultDisplay().getRotation();
                final Camera.CameraInfo info = mCameraInfos.get(mCurrentCameraId);

                int result = (info.orientation + degree) % 360;
                result = (360 - result) % 360;
                mCamera.setDisplayOrientation(result);
                mTextureView.setTag(result);
            }

            if (mCurrentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                final Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                final List<Size> sizes = parameters.getSupportedPictureSizes();

                Size size;
                Size best = sizes.get(0);
                for (int i = 1; i < sizes.size(); i++) {
                    size = sizes.get(i);
                    if (size.width * size.height > best.width * best.height) {
                        best = size;
                    }
                }
                parameters.setPictureSize(best.width, best.height);
                mCamera.setParameters(parameters);
            }

            try {
                mCamera.enableShutterSound(true);
                mCamera.setPreviewTexture(mSurfaceTexture);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void showTakePhotoEffect() {
        Utils.changeVisibility(mContainer, View.VISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(mTakePhotoEffectRunnable, DELAY_MILLIS);
    }

    private final Runnable mTakePhotoEffectRunnable = new Runnable() {
        @Override
        public void run() {
            Utils.changeVisibility(mContainer, View.GONE);
        }
    };
}
