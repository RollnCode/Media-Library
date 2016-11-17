package com.rollncode.media_library;

import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.util.HumanReadables;
import android.support.test.espresso.util.TreeIterables;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.View;

import com.rollncode.media_library.activity.CameraActivity;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class InstrumentedTest {

    private static final int DELAY = 5000;

    @Rule
    public ActivityTestRule<CameraActivity> mCameraRule = new ActivityTestRule<>(CameraActivity.class);

    @Test
    public void makePhoto() throws InterruptedException {
        Thread.sleep(DELAY);

        onView(withId(R.id.iv_take_photo)).perform(click());
        onView(isRoot()).perform(waitId(R.id.iv_back, DELAY));
        onView(withId(R.id.iv_back)).perform(click());
    }

    @Test
    public void makeVideo() throws InterruptedException {
        Thread.sleep(DELAY);

        onView(withId(R.id.iv_take_photo)).perform(ViewActions.longClick());
        Thread.sleep(DELAY);
        onView(withId(R.id.iv_take_photo)).perform(ViewActions.longClick());
    }

    @Test
    public void openPhoto() throws InterruptedException {
        Thread.sleep(DELAY);

        final RecyclerView recyclerView = (RecyclerView) mCameraRule.getActivity().findViewById(R.id.recycler_view);
        final Adapter adapter = recyclerView.getAdapter();
        if (adapter.getItemCount() == 0) {
            return;
        }

        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    }

    @Test
    public void changeCamera() throws InterruptedException {
        Thread.sleep(DELAY);

        onView(withId(R.id.iv_change_camera)).perform(click());
        Thread.sleep(DELAY);
        onView(withId(R.id.iv_change_camera)).perform(click());
    }

    public static ViewAction waitId(final int viewId, final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for a specific view with id <" + viewId + "> during " + millis + " millis.";
            }

            @Override
            public void perform(final UiController uiController, final View view) {
                uiController.loopMainThreadUntilIdle();
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + millis;
                final Matcher<View> viewMatcher = withId(viewId);

                do {
                    for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
                        // found view with required ID
                        if (viewMatcher.matches(child)) {
                            return;
                        }
                    }

                    uiController.loopMainThreadForAtLeast(50);
                }
                while (System.currentTimeMillis() < endTime);

                // timeout happens
                throw new PerformException.Builder()
                        .withActionDescription(this.getDescription())
                        .withViewDescription(HumanReadables.describe(view))
                        .withCause(new TimeoutException())
                        .build();
            }
        };
    }
}
