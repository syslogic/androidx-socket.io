package io.syslogic.socketio.dialog;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.DialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;
import io.syslogic.socketio.activity.MainActivity;
import io.syslogic.socketio.model.ClientSocket;

/**
 * Base {@link DialogFragment}
 * @author Martin Zeitler
 */
public abstract class BaseDialogFragment extends DialogFragment {

    protected String username;

    @NonNull
    protected Socket getSocket() {
        return requireMainActivity().getSocket();
    }

    @NonNull
    protected MainActivity requireMainActivity() {
        return (MainActivity) requireActivity();
    }

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
