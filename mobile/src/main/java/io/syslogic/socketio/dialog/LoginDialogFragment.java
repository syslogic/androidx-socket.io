package io.syslogic.socketio.dialog;

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
import io.syslogic.socketio.activity.MainActivity;
import io.syslogic.socketio.databinding.DialogLoginBinding;
import io.syslogic.socketio.model.ClientSocket;

/**
 * Login {@link BaseDialogFragment}
 * @author Martin Zeitler
 */
public class LoginDialogFragment extends BaseDialogFragment {

    private static final String LOG_TAG = LoginDialogFragment.class.getSimpleName();
    private DialogLoginBinding mDataBinding = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.setDataBinding(inflater, container);

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

    private void attemptLogin() {

        Log.d(LOG_TAG, "attempt login");

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
        Socket socket = getSocket();
        if (socket != null && socket.connected()) {

            // add the login emitter, before logging in.
            socket.on(Constants.REQUEST_KEY_LOGIN, this.onLogin);
            socket.emit(Constants.REQUEST_KEY_USER_ADD, username);

        } else {
            String message = "socket not connected";
            this.mDataBinding.setSocketId(message);
            this.mDataBinding.executePendingBindings();
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
            getSocket().off(Constants.REQUEST_KEY_LOGIN, this.onLogin);

            this.navigateUp(socketId, this.username, items.size());

        } catch (JSONException e) {
            Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
        }
    };

    /** TODO: Add FragmentResultListener ... */
    private void navigateUp(String socketId, String username, int size) {
        if (requireActivity() instanceof MainActivity activity) {

            Bundle result = new Bundle();
            result.putString("socketId", socketId);
            result.putString("username", username);
            result.putInt("size", size);

            getChildFragmentManager().setFragmentResult(Constants.REQUEST_KEY_LOGIN, result);
            activity.getNavController().navigateUp();
        }
    }

    @Override
    protected void setDataBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        this.mDataBinding = DialogLoginBinding.inflate(inflater, container, false);
    }

    @NonNull
    public DialogLoginBinding getDataBinding() {
        return this.mDataBinding;
    }

}
