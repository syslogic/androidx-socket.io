package io.syslogic.socketio.base;

import android.app.UiAutomation;
import android.content.Context;
import android.content.res.Resources;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.runner.RunWith;

import java.lang.ref.WeakReference;

import io.syslogic.socketio.MainActivity;

/**
 * Abstract {@link TestCase} for {@link MainActivity}.
 * @author Martin Zeitler
 */
@RunWith(AndroidJUnit4.class)
public abstract class BaseActivityTest extends TestCase {
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);
    private final UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    private final UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
    private final Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private String packageName = targetContext.getPackageName();
    static final Long DEFAULT_WAIT_TIMEOUT = 200L;
    private ActivityScenario<MainActivity> mScenario;
    protected WeakReference<MainActivity> mActivity;

    protected ActivityScenario<MainActivity> getScenario() {
        return this.mScenario;
    }

    protected void setScenario(ActivityScenario<MainActivity> scenario) {
        this.mScenario = scenario;
    }

    protected void setActivity(MainActivity activity) {
        this.mActivity = new WeakReference<>(activity);
    }

    /** Getters... */
    private UiDevice getDevice() {return this.mDevice;}

    private Context getTestContext() {
        return InstrumentationRegistry.getInstrumentation().getContext();
    }

    protected Resources getTestResources() {return this.getTestContext().getResources(); }

    protected MainActivity getActivity() {return this.mActivity.get();}

    /** ***Utility*** Press the device back button. */
    private void pressBackButton() {this.getDevice().pressBack();}

    /** ***Utility*** Press the device back button and wait for class. */
    protected void pressBackAndWaitFor(Class<?> cls) {
        this.pressBackButton();
        this.waitFor(cls);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /** ***Utility*** Wait for a class. */
    protected void waitFor(Class<?> cls) {
        this.mDevice.wait(Until.hasObject(By.clazz(cls)), DEFAULT_WAIT_TIMEOUT);
    }

    /**
     * ***Utility*** Sleep.
     * @noinspection SameParameterValue
     */
    protected void sleep(Long ms) {
        try {Thread.sleep(ms);} catch (InterruptedException ignore) {}
    }
}
