package io.syslogic.socketio.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import java.net.URISyntaxException;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;

import io.syslogic.socketio.R;
import io.syslogic.socketio.databinding.FragmentNavHostBinding;
import io.syslogic.socketio.fragment.BaseFragment;
import io.syslogic.socketio.menu.ChatMenuProvider;

public class MainActivity extends AppCompatActivity {
    @NonNull final static String LOG_TAG = MainActivity.class.getSimpleName();
    FragmentNavHostBinding mDataBinding = null;
    MenuProvider menuProvider = null;
    Socket mSocket = null;

    /** The default {@link OnBackPressedCallback}. */
    protected OnBackPressedCallback onBackPressed = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                getNavController().popBackStack();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            OnBackPressedDispatcher onBackPressedDispatcher = this.getOnBackPressedDispatcher();
            onBackPressedDispatcher.addCallback(this, this.onBackPressed);
        }
        if (this.mSocket == null) {this.getSocket();}
        this.mDataBinding = FragmentNavHostBinding.inflate(LayoutInflater.from(this), findViewById(android.R.id.content), true);
        this.getNavController().addOnDestinationChangedListener((controller, destination, arguments) -> {
            String message = "onDestinationChanged: " + destination.getNavigatorName()  + " " + destination.getLabel();
            Log.d(LOG_TAG, message);
        });
    }

    @Override
    protected void onDestroy() {
        if (this.mSocket != null && this.mSocket.connected()) {
            this.mSocket.disconnect();
        }
        super.onDestroy();
    }

    @NonNull
    public Socket getSocket() {
        if (this.mSocket == null) {
            try {
                String url =getString(R.string.server_transport) + "://" + getString(R.string.server_hostname) + ":" + getResources().getInteger(R.integer.server_port) + getResources().getString(R.string.server_path);
                this.mSocket = IO.socket(url, new IO.Options());
            } catch (URISyntaxException e) {
                String message = Objects.requireNonNull(e.getMessage());
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, message);
            }
        }
        return this.mSocket;
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

    @NonNull
    private NavHostFragment getNavHostFragment() {
        return (NavHostFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.nav_host));
    }

    public BaseFragment getCurrentFragment() {
        return (BaseFragment) getNavHostFragment().getChildFragmentManager().getFragments().get(0);
    }

    @NonNull
    public NavController getNavController() {
        return NavHostFragment.findNavController(getNavHostFragment());
    }

    @VisibleForTesting
    public FragmentNavHostBinding getDataBinding() {
        return this.mDataBinding;
    }
}
