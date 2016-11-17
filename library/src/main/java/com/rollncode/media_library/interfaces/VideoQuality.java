package com.rollncode.media_library.interfaces;

import android.media.CamcorderProfile;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chekashov R.(email:roman_woland@mail.ru)
 * @since 07.11.16
 */
public enum VideoQuality {

    HIGH("High (highest available resolution)", CamcorderProfile.QUALITY_HIGH),
    MEDIUM_HIGH("Quality 1920x1080", CamcorderProfile.QUALITY_1080P),
    MEDIUM("Quality 1280x720", CamcorderProfile.QUALITY_720P),
    MEDIUM_LOW("Quality 720x480", CamcorderProfile.QUALITY_480P),
    QVGA("Quality QVGA(320x240)", CamcorderProfile.QUALITY_QVGA),
    LOW("Low (lowest available resolution)", CamcorderProfile.QUALITY_LOW);

    private String mString;
    private int mType;

    VideoQuality(String str, int type) {
        mString = str;
        mType = type;
    }

    public static int getType(int which) {
        return values()[which].mType;
    }

    public String getString() {
        return mString;
    }

    @NonNull
    public static List<String> getStrings() {
        List<String> values = new ArrayList<>(values().length);
        for (VideoQuality quality : values()) {
            values.add(quality.getString());
        }
        return values;
    }
}
