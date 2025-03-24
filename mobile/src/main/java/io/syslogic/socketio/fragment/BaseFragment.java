package io.syslogic.socketio.fragment;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;

import io.syslogic.socketio.activity.MainActivity;

public class BaseFragment extends Fragment {
    private static final String LOG_TAG = BaseFragment.class.getSimpleName();
    private Boolean isConnected = true;
    protected static Socket mSocket;
    protected String username;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    protected final Emitter.Listener onConnect = args -> {
        if(! isConnected) {isConnected = true;}
        if (username != null) {
            mSocket.emit("add user", username);
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
            String socketId = fragment.getDataBinding().getConnection();
            fragment.getDataBinding().executePendingBindings();
        }

        if (requireActivity() instanceof MainActivity activity) {
            // final EngineIOException e = (EngineIOException) args[0];
            final String message = "Error connecting to " + activity.getServerUrl();
            activity.runOnUiThread(() -> {

                Log.e(LOG_TAG, message);

                if (activity.getCurrentFragment() instanceof LoginFragment fragment) {
                    fragment.getDataBinding().setConnection(message);
                    fragment.getDataBinding().executePendingBindings();
                } else if (this instanceof ChatFragment fragment) {

                }
            });
        }
    };
}
