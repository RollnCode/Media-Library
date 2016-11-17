package com.rollncode.media_library.activity;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.rollncode.media_library.fragment.BaseFragment;
import com.rollncode.media_library.interfaces.IBaseUI;
import com.rollncode.media_library.utility.ALog;
import com.rollncode.media_library.utility.Utils;

/**
 * Base activity
 *
 * @author Chekashov R.(email:roman_woland@mail.ru)
 * @since 03.11.16
 */
public abstract class BaseActivity extends AppCompatActivity
        implements IBaseUI {

    public int getFragmentContainerId() {
        return 0;
    }

    public boolean startFragment(@NonNull BaseFragment fragment, boolean addToBackStack, @NonNull Object... objects) {
        try {
            return startFragmentSimple(getFragmentContainerId(), fragment, addToBackStack);

        } catch (Throwable e) {
            ALog.toLog(e);
            return false;
        }
    }

    protected final boolean startFragmentSimple(int containerId, @NonNull Fragment fragment, boolean addToBackStack) {
        final String tag = Utils.getTag(fragment);
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(containerId, fragment, tag);
        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }
        transaction.commit();

        return true;
    }

    @Override
    public void onObjectsReceive(int code, @NonNull Object... objects) {
    }
}
