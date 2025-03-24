package io.syslogic.socketio.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import io.syslogic.socketio.R;
import io.syslogic.socketio.activity.MainActivity;
import io.syslogic.socketio.databinding.FragmentLoginBinding;

public class LoginFragment extends BaseFragment {
    private static final String LOG_TAG = LoginFragment.class.getSimpleName();
    private FragmentLoginBinding mDataBinding = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (requireActivity() instanceof MainActivity activity) {
            activity.removeMenuProvider();
            mSocket = activity.getSocket();
            mSocket.on(Socket.EVENT_CONNECT, this.onConnect);
            mSocket.on(Socket.EVENT_DISCONNECT, this.onDisconnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, this.onConnectError);
            mSocket.on("login", this.onLogin);
            if (! mSocket.connected()) {mSocket.connect();}
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        this.mDataBinding = FragmentLoginBinding.inflate(inflater, parent, false);
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
        mSocket.off("login", this.onLogin);
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
            // inputUsername.setEnabled(false);
        }

        // perform the login attempt
        this.username = username;
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit("add user", username);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            prefs.edit().putString("username", username).apply();
        } else {
            String message = "socket not connected";
            this.mDataBinding.setConnection(message);
            this.mDataBinding.executePendingBindings();
        }

        // inputUsername.setEnabled(true);
    }

    private void gotoChatFragment(String socketId, String username, int usercount) {

        Bundle navArgs = new Bundle();
        navArgs.putString("socketId", socketId);
        navArgs.putString("username", username);
        navArgs.putInt("usercount", usercount);

        /* Navigate to ChatFragment */
        if (requireActivity() instanceof MainActivity activity) {
            activity.runOnUiThread(() -> {
                this.mDataBinding.setConnection(socketId);
                this.mDataBinding.executePendingBindings();
                if (activity.getCurrentFragment() instanceof LoginFragment) {
                    activity.getNavController().navigate(R.id.action_loginFragment_to_chatFragment, navArgs);
                }
            });
        }
    }

    protected final Emitter.Listener onLogin = args -> {
        JSONObject data = (JSONObject) args[0];
        try {
            String socketId = data.getString("socketId");
            String usercount = String.valueOf(data.getInt("usercount"));
            Log.d(LOG_TAG, "room " + socketId + " has " + usercount + " participants");
            this.gotoChatFragment(socketId, this.username, Integer.parseInt(usercount));
        } catch (JSONException e) {
            Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
        }
    };

    public FragmentLoginBinding getDataBinding() {
        return this.mDataBinding;
    }
}
