package com.example.g10kandidat2019;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;
import android.support.constraint.ConstraintLayout;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.v4.content.ContextCompat;
import android.view.Surface;
import android.view.View;

import com.example.g10kandidat2019.graph.VideoListActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;


import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.intent.Checks;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.*;

/**
 * Instrumented test_file, which will execute on an Android device.
 * Tests if clicking and swiping on the main activity works as it is supposed to
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class MainActivityTest {


    @Rule
    public IntentsTestRule<MainActivity> mActivityRule = new IntentsTestRule<>(
            MainActivity.class);



    @Test
    public void TestSwipe(){
        final ViewInteraction uiElement = onView(withId(R.id.mainConstraintLayout));
        onView(withId(R.id.mainConstraintLayout)).perform(new GeneralSwipeAction(
                Swipe.FAST, GeneralLocation.TOP_LEFT, GeneralLocation.TOP_RIGHT, Press.FINGER));
        uiElement.check(matches(isDisplayed()));
    }

    @Test
    public void TestRotation(){
        try {
            UiDevice uiDevice = UiDevice.getInstance(getInstrumentation());
            uiDevice.setOrientationLeft();
            assertEquals(Surface.ROTATION_0,uiDevice.getDisplayRotation());
            uiDevice.setOrientationRight();
            assertEquals(Surface.ROTATION_0,uiDevice.getDisplayRotation());
        } catch (RemoteException e) {
            e.printStackTrace();
            fail();
        }

    }

    @Test
    public void TestClickCamera(){
        onView(withId(R.id.mainCameraBtn)).perform(click());
        intended(hasComponent(CameraActivity.class.getName()));

    }

    @Rule public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);
    //TODO: try to make this programmatically so we can test what happens when we don't have the permission.
    @Test
    public void TestClickGraph(){
        //allowPermissionsIfNeeded(Manifest.permission.READ_EXTERNAL_STORAGE);
        onView(withId(R.id.mainGraphBtn)).perform(click());
        intended(hasComponent(VideoListActivity.class.getName()));
    }

    @Test
    public void TestClickSettings(){
        onView(withId(R.id.mainSettingsbtn)).perform(click());
        intended(hasComponent(SettingsActivity.class.getName()));
    }

    @ClassRule public static ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    public static Matcher<View> withBackground(final int drawable) {
        Checks.checkNotNull(drawable);
        return new BoundedMatcher<View, ConstraintLayout>(ConstraintLayout.class) {
            @Override
            public boolean matchesSafely(ConstraintLayout warning) {
                System.out.println(warning);
                return warning.getTag() != null && warning.getTag().equals(drawable);
            }
            @Override
            public void describeTo(Description description) {
                activityTestRule.getActivity().findViewById(R.id.mainConstraintLayout);
            }
        };
    }

    private static final int PERMISSIONS_DIALOG_DELAY = 3000;
    private static final int GRANT_BUTTON_INDEX = 1;

    private static void allowPermissionsIfNeeded(String permissionNeeded) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasNeededPermission(permissionNeeded)) {
                sleep(PERMISSIONS_DIALOG_DELAY);
                UiDevice device = UiDevice.getInstance(getInstrumentation());
                UiObject allowPermissions = device.findObject(new UiSelector()
                        .clickable(true)
                        .checkable(false)
                        .index(GRANT_BUTTON_INDEX));
                if (allowPermissions.exists()) {
                    allowPermissions.click();
                }
            }
        } catch (UiObjectNotFoundException e) {
            System.out.println("There is no permissions dialog to interact with");
        }
    }

    private static boolean hasNeededPermission(String permissionNeeded) {
        Context context = getInstrumentation().getTargetContext();
        int permissionStatus = ContextCompat.checkSelfPermission(context, permissionNeeded);
        return permissionStatus == PackageManager.PERMISSION_GRANTED;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException("Cannot execute Thread.sleep()");
        }
    }

}
