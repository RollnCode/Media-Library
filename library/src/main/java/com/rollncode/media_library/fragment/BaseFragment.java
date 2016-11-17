package com.rollncode.media_library.fragment;

import android.content.ContentResolver;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rollncode.media_library.R;
import com.rollncode.media_library.activity.BaseActivity;
import com.rollncode.media_library.interfaces.IBaseUI;
import com.rollncode.media_library.utility.Utils;

/**
 * Base fragment
 *
 * @author Chekashov R.(email:roman_woland@mail.ru)
 * @since 03.11.16
 */
public abstract class BaseFragment extends Fragment
        implements IBaseUI {

    protected static final String VIDEO_PREFIX = "VID_";
    protected static final String VIDEO_EXTENSION = ".mp4";

    private int mFragmentContainerId;
    private ContentResolver mContentResolver;

    @Override
    public void onCreate(@Nullable Bundle b) {
        super.onCreate(b);

        mContentResolver = getActivity().getContentResolver();
    }

    @Override
    public void onStart() {
        super.onStart();

        final BaseActivity activity = (BaseActivity) getActivity();
        mFragmentContainerId = activity.getFragmentContainerId();
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle b) {
        return onInheritorCreateView(LayoutInflater.from(getActivity()), container, b);
    }

    @Nullable
    protected View onInheritorCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle b) {
        return super.onCreateView(inflater, container, b);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
    }

    @Override
    public void onObjectsReceive(int code, @NonNull Object... objects) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            onCleanUp();

        } catch (Exception ignore) {
        } finally {
            System.gc();
        }
    }

    protected boolean startFragment(@NonNull BaseFragment fragment, boolean addToBackStack, @NonNull Object... objects) {
        return startFragmentSimple(fragment, addToBackStack, objects.length > 0 && (boolean) objects[0]);
    }

    private boolean startFragmentSimple(@NonNull BaseFragment fragment, boolean addToBackStack, boolean animate) {
        final String tag = Utils.getTag(fragment);
        final FragmentTransaction transaction = getFragmentManager().beginTransaction();

        if (animate) {
            transaction.setCustomAnimations(0, 0, 0, R.anim.out);
        } else {
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        }

        transaction.replace(getFragmentContainerId(), fragment, tag);
        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }
        transaction.commit();

        return true;
    }

    private int getFragmentContainerId() {
        return mFragmentContainerId;
    }

    @MenuRes
    protected int getOptionsMenuId() {
        return -1;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        @MenuRes final int menuRes = getOptionsMenuId();
        if (menuRes > 0) {
            menu.clear();
            inflater.inflate(menuRes, menu);
        }
    }

    public ContentResolver getContentResolver() {
        return mContentResolver;
    }
}
