package io.syslogic.socketio.fragment;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;
import io.syslogic.socketio.R;
import io.syslogic.socketio.activity.MainActivity;

public class BaseFragment extends Fragment {
    private static final String LOG_TAG = BaseFragment.class.getSimpleName();
    protected Socket mSocket;
    // skip the initial "connected" message
    private Boolean isConnected = true;

    protected String username;

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
        if (requireActivity() instanceof MainActivity activity) {
            // final EngineIOException e = (EngineIOException) args[0];
            final String message = "Error connecting to \"" + activity.getServerUrl() + "\".";
            activity.runOnUiThread(() -> {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, message);
            });
        }
    };

    protected final Emitter.Listener onLogin = args -> {

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

            // activity.setUserName(username);
            // activity.setUserCount(userCount);
            // activity.setSocketId(socketId);

            Bundle navArgs = new Bundle();
            navArgs.putString("socketId", socketId);
            navArgs.putString("username", username);
            navArgs.putInt("userCount", userCount);

            /* navigate to chat fragment */
            activity.runOnUiThread(() -> {
                activity.getNavController().navigate(R.id.action_loginFragment_to_chatFragment, navArgs);
            });
        }
    };
}
