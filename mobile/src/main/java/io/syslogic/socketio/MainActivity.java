package io.syslogic.socketio;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.navigation.NavController;

import java.net.URISyntaxException;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity {
    @NonNull final static String LOG_TAG = MainActivity.class.getSimpleName();
    LiveData<NavController> currentNavController = null;
    private String userName = null;
    private int userCount = 0;
    private Socket mSocket;
    String currentRoom;

    /** The default {@link OnBackPressedCallback}. */
    protected final OnBackPressedCallback navigateOnBackPressed = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else if (currentNavController.getValue() != null) {
                currentNavController.getValue().navigateUp();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getOnBackPressedDispatcher().addCallback(this.navigateOnBackPressed);
        this.setContentView(R.layout.fragment_container);
        try {
            IO.Options opts = new IO.Options();
            this.mSocket = IO.socket(Constants.CHAT_SERVER_URL, opts);
        } catch (URISyntaxException e) {
            Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    public void setUserName(String value) {
        this.userName = value;
    }

    public void setUserCount(int value) {
        this.userCount = value;
    }

    public Socket getSocket() {
        return this.mSocket;
    }

    public String getUserName() {
        return this.userName;
    }

    public int getUserCount() {
        return this.userCount;
    }

    public void setRoom(String value) {
        this.currentRoom = value;
    }
}
