package io.syslogic.socketio;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import org.json.JSONException;
import org.json.JSONObject;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);
        mDataBinding.usernameInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.sign_in_button || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mDataBinding.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        return mDataBinding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getActivity() != null) {
            mSocket = ((MainActivity) getActivity()).getSocket();
            mSocket.on("login", onLogin);
        }
    }


    @Override
    public void onDestroy() {
        mSocket.off("login", onLogin);
        super.onDestroy();
    }

    private void attemptLogin() {

        // Reset errors.
        mDataBinding.usernameInput.setError(null);

        // Check for a valid username
        String username = mDataBinding.usernameInput.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            mDataBinding.usernameInput.setError(getString(R.string.error_field_required));
            mDataBinding.usernameInput.requestFocus();
            return;
        }

        // perform the login attempt
        mUserName = username;
        if(mSocket.id() == null) {mSocket.connect();}
        mSocket.emit("add user", username);
    }

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            JSONObject data = (JSONObject) args[0];
            int userCount;
            try {
                userCount = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }

            if(getActivity() != null) {
                ((MainActivity) getActivity()).setUserName(mUserName);
                ((MainActivity) getActivity()).setUserCount(userCount);
            }

            /* navigating back */
            if(getView() != null) {
                Navigation.findNavController(getView()).navigateUp();
            }
        }
    };
}
