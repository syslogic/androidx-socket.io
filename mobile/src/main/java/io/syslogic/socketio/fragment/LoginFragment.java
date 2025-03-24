package io.syslogic.socketio.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.preference.PreferenceManager;

import io.socket.client.Socket;

import io.syslogic.socketio.R;
import io.syslogic.socketio.activity.MainActivity;
import io.syslogic.socketio.databinding.FragmentLoginBinding;

public class LoginFragment extends BaseFragment {
    private static final String LOG_TAG = LoginFragment.class.getSimpleName();
    private FragmentLoginBinding mDataBinding = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        this.mDataBinding = FragmentLoginBinding.inflate(inflater, parent, false);

        this.mDataBinding.usernameInput.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == R.id.sign_in_button || id == EditorInfo.IME_NULL) {
                this.attemptLogin();
                return true;
            }
            return false;
        });

        //noinspection CodeBlock2Expr
        this.mDataBinding.signInButton.setOnClickListener(view -> {
            this.attemptLogin();
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String username = prefs.getString("username", null);
        if (username != null) {this.mDataBinding.usernameInput.setText(username);}
        return this.mDataBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
    public void onDestroy() {
        if (requireActivity() instanceof MainActivity activity) {
            mSocket = activity.getSocket();
            mSocket.off(Socket.EVENT_CONNECT, this.onConnect);
            mSocket.off(Socket.EVENT_DISCONNECT, this.onDisconnect);
            mSocket.off(Socket.EVENT_CONNECT_ERROR, this.onConnectError);
            mSocket.off("login", this.onLogin);
        }
        super.onDestroy();
    }

    private void attemptLogin() {

        AppCompatEditText usernameInput = this.mDataBinding.usernameInput;

        // Reset errors.
        usernameInput.setError(null);

        // Check for a valid username
        assert usernameInput.getText() != null;
        String username = usernameInput.getText().toString().trim();
        usernameInput.setText(username); // trimmed.

        if (TextUtils.isEmpty(username)) {
            usernameInput.setError(getString(R.string.error_field_required));
            usernameInput.requestFocus();
            return;
        }

        // perform the login attempt
        this.username = username;
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit("add user", username);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            prefs.edit().putString("username", username).apply();
        } else {
            String message = "socket not connected";
            usernameInput.setError(message);
            Log.e(LOG_TAG, message);
        }
    }
}
