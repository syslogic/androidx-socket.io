package io.syslogic.socketio.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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

abstract public class BaseActivity extends AppCompatActivity {
    @NonNull final static String LOG_TAG = BaseActivity.class.getSimpleName();
    BaseFragment currentFragment = null;
    FragmentNavHostBinding mDataBinding = null;
    NavController navController = null;
    MenuProvider menuProvider = null;
    Socket mSocket = null;

    /** The default {@link OnBackPressedCallback}. */
    protected OnBackPressedCallback onBackPressed = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else if (getNavController().popBackStack()) {

                // TODO: rebind button click events ??
                // NavDestination dest = getNavController().getCurrentDestination();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mSocket == null) {this.initSocket();}
        this.mDataBinding = FragmentNavHostBinding.inflate(LayoutInflater.from(this), findViewById(android.R.id.content), true);
        this.navController = NavHostFragment.findNavController(getNavHostFragment());
        this.navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            String message = "onDestinationChanged: " + destination.getNavigatorName()  + " " + destination.getLabel();
            Log.d(LOG_TAG, message);
        });
    }

    @NonNull
    private NavHostFragment getNavHostFragment() {
        return (NavHostFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.nav_host));
    }

    public BaseFragment getCurrentFragment() {
        return (BaseFragment) getNavHostFragment().getChildFragmentManager().getFragments().get(0);
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
        return this.navController;
    }

    @NonNull
    public Socket getSocket() {
        return this.initSocket();
    }

    public Socket initSocket() {
        if (this.mSocket == null) {
            try {
                this.mSocket = IO.socket(getServerUrl(), new IO.Options());
            } catch (URISyntaxException e) {
                String message = Objects.requireNonNull(e.getMessage());
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, message);
            }
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

    @VisibleForTesting
    public FragmentNavHostBinding getDataBinding() {
        return this.mDataBinding;
    }
}
