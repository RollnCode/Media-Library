package com.rollncode.sample;

import android.content.Intent;
import android.media.CamcorderProfile;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.rollncode.media_library.activity.CameraActivity;

/**
 * @author Maxim Ambroskin kkxmshu@gmail.com
 * @since 09.11.16
 */

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 5000;

    private TextView mTvPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvPath = (TextView) findViewById(R.id.tv_path);

        findViewById(R.id.btn_launch).setOnClickListener(mClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    if (data.hasExtra(CameraActivity.EXTRA_FILE_PATH)) {
                        final String path = data.getStringExtra(CameraActivity.EXTRA_FILE_PATH);
                        mTvPath.setText(path);
                    }

                default:
                        break;
            }
        }
    }

    private final OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            CameraActivity.start(MainActivity.this, REQUEST_CAMERA, CamcorderProfile.QUALITY_QVGA);
        }
    };
}
