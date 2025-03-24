package io.syslogic.socketio.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.net.URISyntaxException;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.syslogic.socketio.R;
import io.syslogic.socketio.databinding.FragmentContainerBinding;
import io.syslogic.socketio.menu.ChatMenuProvider;

abstract public class BaseActivity extends AppCompatActivity {
    @NonNull final static String LOG_TAG = BaseActivity.class.getSimpleName();
    FragmentContainerBinding mDataBinding = null;
    MenuProvider menuProvider = null;
    Socket mSocket = null;

    /** The default {@link OnBackPressedCallback}. */
    protected OnBackPressedCallback onBackPressed = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                getNavController().navigateUp();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mDataBinding = FragmentContainerBinding.inflate(LayoutInflater.from(this), findViewById(android.R.id.content), true);
        if (this.mSocket == null) {this.initSocket();}
        if (savedInstanceState == null) {
            OnBackPressedDispatcher onBackPressedDispatcher = this.getOnBackPressedDispatcher();
            onBackPressedDispatcher.addCallback(this, this.onBackPressed);
        }
    }

    @Override
    protected void onDestroy() {
        if (this.mSocket != null && this.mSocket.connected()) {
            this.mSocket.disconnect();
        }
        super.onDestroy();
    }

    @NonNull
    public NavController getNavController() {
        return Navigation.findNavController(this.mDataBinding.navHost);
    }

    @NonNull
    public Socket getSocket() {
        if (this.mSocket != null) {return this.mSocket;}
        return this.initSocket();
    }

    public Socket initSocket() {
        try {
            this.mSocket = IO.socket(getServerUrl(), new IO.Options());
        } catch (URISyntaxException e) {
            String message = Objects.requireNonNull(e.getMessage());
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, message);
        }
        return this.mSocket;
    }

    @NonNull
    public String getServerUrl() {
        return getString(R.string.server_transport) + "://" + getString(R.string.server_hostname) + ":" + getResources().getInteger(R.integer.server_port) + getResources().getString(R.string.server_path);
    }

    public void addChatMenuProvider() {
        if (this.menuProvider == null) {
            this.menuProvider = new ChatMenuProvider(this);
            this.addMenuProvider(this.menuProvider);
        }
    }
    public void removeMenuProvider() {
        if (this.menuProvider != null) {
            this.removeMenuProvider(this.menuProvider);
            this.menuProvider = null;
        }
    }
}
