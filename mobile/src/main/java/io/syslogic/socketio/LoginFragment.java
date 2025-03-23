package io.syslogic.socketio;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.syslogic.socketio.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private static final String LOG_TAG = LoginFragment.class.getSimpleName();
    private FragmentLoginBinding mDataBinding = null;
    private String mUserName;
    private Socket mSocket;

    public LoginFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, parent, false);
        mDataBinding.usernameInput.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == R.id.sign_in_button || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });
        mDataBinding.signInButton.setOnClickListener(view -> attemptLogin());
        return mDataBinding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (requireActivity() instanceof MainActivity activity) {
            mSocket = activity.getSocket();
            mSocket.on("login", onLogin);
        }
    }

    @Override
    public void onDestroy() {
        if (requireActivity() instanceof MainActivity activity) {
            mSocket = activity.getSocket();
            mSocket.off("login", onLogin);
        }
        super.onDestroy();
    }

    private void attemptLogin() {

        // Reset errors.
        mDataBinding.usernameInput.setError(null);

        // Check for a valid username
        assert mDataBinding.usernameInput.getText() != null;
        String username = mDataBinding.usernameInput.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            mDataBinding.usernameInput.setError(getString(R.string.error_field_required));
            mDataBinding.usernameInput.requestFocus();
            return;
        }

        // perform the login attempt
        mUserName = username;
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit("add user", username);
        } else {
            String message = "socket not connected";
            mDataBinding.usernameInput.setError(message);
            Log.e(LOG_TAG, message);

        }
    }

    private final Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(@NonNull Object... args) {
            JSONObject data = (JSONObject) args[0];
            int userCount; String socketId;
            try {
                socketId = data.getString("socketId");
                userCount = data.getInt("userCount");
                Log.d(LOG_TAG, "room " + socketId + " has " + userCount + " participants");
            } catch (JSONException e) {
                Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
                return;
            }

            if (requireActivity() instanceof MainActivity activity) {
                activity.setUserName(mUserName);
                activity.setUserCount(userCount);
                activity.setRoom(socketId);

                /* navigate upwards */
                if (getView() != null) {
                    activity.runOnUiThread(() ->
                            Navigation.findNavController(getView()).navigateUp()
                    );
                }
            }
        }
    };
}
