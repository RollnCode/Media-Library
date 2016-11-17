package com.rollncode.media_library.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.CamcorderProfile;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.rollncode.media_library.R;
import com.rollncode.media_library.app.AContext;
import com.rollncode.media_library.fragment.CameraFragment;

/**
 * Camera activity.
 * For getting media file path, need launch activity  by method
 * <b>startActivityForResult</b> with
 * <b>EXTRA_QUALITY</b> boolean parameter for showing dialog for choosing video quality
 *
 * @author Chekashov R.(email:roman_woland@mail.ru)
 * @since 03.11.16
 */
public class CameraActivity extends BaseActivity {

    public static final String EXTRA_QUALITY = "local.EXTRA_QUALITY";
    public static final String EXTRA_FILE_PATH = "local.EXTRA_FILE_PATH";

    /**
     * This method for starting CameraActivity with quality params
     *
     * @param quality {@link Integer#MAX_VALUE} for showing quality dialog.
     *                {@link android.media.CamcorderProfile#QUALITY_720P} (or another) for set quality.
     *                <b>empty</b> for default.
     */
    public static void start(@NonNull Activity activity, int requestCode, int... quality) {
        final Intent intent = new Intent(activity, CameraActivity.class);
        if (quality.length > 0) {
            intent.putExtra(EXTRA_QUALITY, quality[0]);
        }
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_camera);

        AContext.init(this);

        if (b == null) {
            final Intent intent = getIntent();
            final int quality = intent.getIntExtra(EXTRA_QUALITY, CamcorderProfile.QUALITY_HIGH);
            startFragment(CameraFragment.newInstance(quality), false);
        }
    }

    @Override
    public int getFragmentContainerId() {
        return R.id.fragment_container;
    }

    @Override
    public void onCleanUp() throws Exception {
    }
}
