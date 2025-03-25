package io.syslogic.socketio.fragment;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;

import io.syslogic.socketio.Constants;
import io.syslogic.socketio.R;
import io.syslogic.socketio.MainActivity;
import io.syslogic.socketio.model.ClientSocket;

/**
 * Abstract Base {@link Fragment}
 * @author Martin Zeitler
 */
public abstract class BaseFragment extends Fragment {
    private static final String LOG_TAG = BaseFragment.class.getSimpleName();
    private Boolean isConnected = true;
    protected static Socket mSocket;
    protected String username;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @NonNull
    protected MainActivity requireMainActivity() {
        return (MainActivity) requireActivity();
    }

    /** @noinspection SameParameterValue */
    protected void addFragmentResultListener(String requestKey, FragmentResultListener listener, boolean add) {
        if (add) {
            this.getChildFragmentManager().setFragmentResultListener(requestKey, requireActivity(), listener);
        } else {
            this.getChildFragmentManager().clearFragmentResultListener(requestKey);
        }
    }

    protected final Emitter.Listener onConnect = args -> {
        if(! isConnected) {isConnected = true;}
        if (username != null) {
            mSocket.emit(Constants.REQUEST_KEY_USER_ADD, username);
        }
    };

    protected final Emitter.Listener onDisconnect = args -> {
        if (requireActivity() instanceof MainActivity activity) {
            activity.runOnUiThread(() -> isConnected = false);
        }
    };

    /** On {@link EngineIOException} */
    protected final Emitter.Listener onConnectError = args -> {

        if (this instanceof LoginFragment fragment) {
            String socketId = fragment.getDataBinding().getSocketId();
            fragment.getDataBinding().executePendingBindings();
        }

        if (requireActivity() instanceof MainActivity activity) {
            // final EngineIOException e = (EngineIOException) args[0];
            final String message = "Error connecting " +  getString(R.string.server_hostname) + ":" + getResources().getInteger(R.integer.server_port);
            activity.runOnUiThread(() -> {

                Log.e(LOG_TAG, message);

                if (activity.getCurrentFragment() instanceof LoginFragment fragment) {
                    fragment.getDataBinding().setSocketId(message);
                    fragment.getDataBinding().executePendingBindings();
                } else if (this instanceof ChatFragment fragment) {

                }
            });
        }
    };

    @NonNull
    protected ArrayList<ClientSocket> parseSockets(@NonNull JSONArray data) {
        ArrayList<ClientSocket> items = new ArrayList<>();
        try {
            for (int i=0; i < data.length(); i++) {
                JSONObject item = data.getJSONObject(i);
                String socketId = item.getString("socketId");
                String username = item.getString("username");
                items.add(new ClientSocket.Builder(0).setSocketId(socketId).setUsername(username).build());
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return items;
    }

    /** Inflate data-binding. */
    protected abstract void setDataBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    /** @return instance of {@link ViewDataBinding}. */
    @NonNull public abstract ViewDataBinding getDataBinding();
}
