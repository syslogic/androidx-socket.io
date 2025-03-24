package io.syslogic.socketio.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;

public class MainActivity extends BaseActivity {
    @NonNull final static String LOG_TAG = MainActivity.class.getSimpleName();
    String socketId = null;
    String userName = null;
    int userCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {

            OnBackPressedDispatcher onBackPressedDispatcher = this.getOnBackPressedDispatcher();
            onBackPressedDispatcher.addCallback(this, this.onBackPressed);

            getNavController().addOnDestinationChangedListener((navController, navDestination, bundle) -> {
                Log.d(LOG_TAG, String.valueOf(navDestination.getLabel()));
            });
        }
    }

    public void setSocketId(String value) {
        this.socketId = value;
    }

    public void setUserName(String value) {
        this.userName = value;
    }

    public void setUserCount(int value) {
        this.userCount = value;
    }

    public String getSocketId() {
        return this.socketId;
    }

    public String getUserName() {
        return this.userName;
    }

    public int getUserCount() {
        return this.userCount;
    }
}
