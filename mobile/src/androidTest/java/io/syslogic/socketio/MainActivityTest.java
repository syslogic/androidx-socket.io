package io.syslogic.socketio;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;

import io.syslogic.socketio.activity.MainActivity;
import io.syslogic.socketio.base.BaseActivityTest;
import io.syslogic.socketio.fragment.ChatFragment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * [BaseActivityTest] for [MainActivity].
 * @author Martin Zeitler
 */
public class MainActivityTest extends BaseActivityTest {

    /**
     * Initialize the {@link ActivityScenario} with the {@link MainActivity}.
     * @noinspection Convert2MethodRef, CodeBlock2Expr
     */
    @Before
    public void setupTest() {
        this.setScenario(this.activityScenarioRule.getScenario());
        this.getScenario().onActivity( activity -> {
            this.setActivity(activity);
        });
    }

    @Test
    public void testLoginFragment() {
        Assert.assertNotNull("has data-binding", mActivity.get().getDataBinding());
        Espresso.onView(ViewMatchers.withId(R.id.input_username)).perform(ViewActions.clearText(), ViewActions.typeText("Crash Test"));
        Espresso.onView(ViewMatchers.withId(R.id.button_sign_in)).perform(ViewActions.click());
        this.waitFor(ChatFragment.class);

        // Assert.assertTrue("socket connected", mActivity.get().getSocket().connected());
        Espresso.onView(ViewMatchers.withId(R.id.input_message)).perform(ViewActions.clearText(), ViewActions.typeText("Test Message"));
        Espresso.onView(ViewMatchers.withId(R.id.button_send)).perform(ViewActions.click());
    }
}
