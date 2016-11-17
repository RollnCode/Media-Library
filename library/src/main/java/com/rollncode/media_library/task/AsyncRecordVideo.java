package com.rollncode.media_library.task;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.rollncode.media_library.R;
import com.rollncode.media_library.interfaces.ObjectsReceiver;
import com.rollncode.media_library.utility.Utils;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Class which extends AsyncTask and records video from camera
 *
 * @author Maxim Ambroskin kkxmshu@gmail.com
 * @since 07.11.16
 */

@SuppressWarnings("deprecation")
public class AsyncRecordVideo extends AsyncTask<Object, Void, MediaRecorder> {

    private final WeakReference<ObjectsReceiver> mReceiver;
    private final Camera mCamera;

    public AsyncRecordVideo(@NonNull ObjectsReceiver receiver, @NonNull Camera camera) {
        mReceiver = new WeakReference<>(receiver);
        mCamera = camera;
    }

    @Override
    protected MediaRecorder doInBackground(Object... params) {
        final boolean permission = (boolean) params[0];
        final int orientation = (int) params[1];
        final String path = (String) params[2];
        final int videoQuality = (int) params[3];
        final MediaRecorder recorder = new MediaRecorder();

        mCamera.stopPreview();
        mCamera.unlock();
        try {
            recorder.setCamera(mCamera);
            recorder.setOrientationHint(orientation);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            if (permission) { // check if audio record granted
                recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            }
            recorder.setOutputFile(path);
            recorder.setProfile(CamcorderProfile.get(videoQuality));
            recorder.prepare();
            recorder.start();
            return recorder;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(MediaRecorder recorder) {
        super.onPostExecute(recorder);

        Utils.receiveObjects(mReceiver, R.id.code_async_record_video, recorder);
    }
}
