package io.syslogic.socketio;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mSocket == null) {this.initSocket();}
        this.getOnBackPressedDispatcher().addCallback(this.navigateOnBackPressed);
        this.setContentView(R.layout.fragment_container);
    }

    public void initSocket() {
        try {
            this.mSocket = IO.socket(getServerUrl(), /* getSocketIoOptions() */ new IO.Options() );
        } catch (URISyntaxException e) {
            String message = Objects.requireNonNull(e.getMessage());
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, message);
        }
    }

    public void setUserName(String value) {
        this.userName = value;
    }

    public void setUserCount(int value) {
        this.userCount = value;
    }

    public void setRoom(String value) {
        this.currentRoom = value;
    }

    @NonNull
    public Socket getSocket() {
        return this.mSocket;
    }

    public String getUserName() {
        return this.userName;
    }

    public int getUserCount() {
        return this.userCount;
    }

    @NonNull
    private String getServerUrl() {
        return getString(R.string.server_transport) + "://" + getString(R.string.server_hostname) +
                ":" + getResources().getInteger(R.integer.server_port) +
                getResources().getString(R.string.server_path);
    }
}
