package io.syslogic.socketio.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import io.syslogic.socketio.Constants;
import io.syslogic.socketio.R;
import io.syslogic.socketio.MainActivity;
import io.syslogic.socketio.databinding.FragmentLoginBinding;
import io.syslogic.socketio.model.ClientSocket;

/**
 * Login {@link BaseFragment}
 * @author Martin Zeitler
 */
public class LoginFragment extends BaseFragment {
    private static final String LOG_TAG = LoginFragment.class.getSimpleName();
    private FragmentLoginBinding mDataBinding = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (requireActivity() instanceof MainActivity activity) {
            activity.removeMenuProvider();
            if (savedInstanceState == null) {
                mSocket = activity.getSocket();
                mSocket.on(Socket.EVENT_CONNECT, this.onConnect);
                mSocket.on(Socket.EVENT_DISCONNECT, this.onDisconnect);
                mSocket.on(Socket.EVENT_CONNECT_ERROR, this.onConnectError);
                mSocket.on(Constants.REQUEST_KEY_USER_LOGIN, this.onLogin);
                if (!mSocket.connected()) {mSocket.connect();}
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.setDataBinding(inflater, container);
        if (requireActivity() instanceof MainActivity activity) {
            activity.setSupportActionBar(this.getDataBinding().toolbarLogin);
        }

        this.mDataBinding.inputUsername.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == R.id.button_sign_in /* || id == EditorInfo.IME_NULL */) {
                this.attemptLogin();
                return true;
            }
            return false;
        });

        //noinspection CodeBlock2Expr
        this.mDataBinding.buttonSignIn.setOnClickListener(view -> {
            this.attemptLogin();
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String username = prefs.getString("username", null);
        if (username != null) {this.mDataBinding.inputUsername.setText(username);}

        return this.mDataBinding.getRoot();
    }

    @Override
    public void onDestroy() {
        mSocket.off(Socket.EVENT_CONNECT, this.onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, this.onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, this.onConnectError);
        mSocket.off(Constants.REQUEST_KEY_USER_LOGIN, this.onLogin);
        super.onDestroy();
    }

    private void attemptLogin() {

        AppCompatEditText inputUsername = this.mDataBinding.inputUsername;

        // Reset errors.
        inputUsername.setError(null);

        // Check for a valid username
        String username = Objects.requireNonNull(inputUsername.getText()).toString().trim();
        if (username.isEmpty()) {
            inputUsername.setError(getString(R.string.error_field_required));
            inputUsername.requestFocus();
            return;
        } else {
            inputUsername.setText(username); // trimmed.
        }

        // perform the login attempt
        this.username = username;
        if (mSocket != null && mSocket.connected()) {

            // add the login emitter, before logging in.
            mSocket.on(Constants.REQUEST_KEY_USER_LOGIN, this.onLogin);
            mSocket.emit(Constants.REQUEST_KEY_USER_ADD, username);

        } else {
            String message = "socket not connected";
            this.mDataBinding.setSocketId(message);
            this.mDataBinding.executePendingBindings();
        }
    }

    private void gotoChatFragment(String socketId, String username, int usercount) {

        Bundle navArgs = new Bundle();
        navArgs.putString("socketId", socketId);
        navArgs.putString("username", username);
        navArgs.putInt("usercount", usercount);

        /* Navigate to ChatFragment */
        if (requireActivity() instanceof MainActivity activity) {
            activity.runOnUiThread(() -> {
                this.mDataBinding.setSocketId(socketId);
                this.mDataBinding.executePendingBindings();
                if (activity.getCurrentFragment() instanceof LoginFragment) {
                    try {
                        activity.getNavController().navigate(
                                R.id.action_loginFragment_to_chatFragment,
                                navArgs
                        );
                    }
                    catch (IllegalArgumentException ignore) {}
                }
            });
        }
    }

    protected final Emitter.Listener onLogin = args -> {

        // Store the last username in shared preferences.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        prefs.edit().putString("username", this.username).apply();

        JSONObject data = (JSONObject) args[0];
        try {
            String socketId = data.getString("socketId");
            JSONArray sockets = data.getJSONArray("data");
            ArrayList<ClientSocket> items = this.parseSockets(sockets);

            Log.d(LOG_TAG, "room " + socketId + " has " + items.size() + " participants");

            // remove the emitter, else it will login endlessly.
            mSocket.off(Constants.REQUEST_KEY_USER_LOGIN, this.onLogin);

            this.gotoChatFragment(socketId, this.username, items.size());

        } catch (JSONException e) {
            Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
        }
    };

    @Override
    protected void setDataBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        this.mDataBinding = FragmentLoginBinding.inflate(inflater, container, false);
    }

    @NonNull
    public FragmentLoginBinding getDataBinding() {
        return this.mDataBinding;
    }
}
