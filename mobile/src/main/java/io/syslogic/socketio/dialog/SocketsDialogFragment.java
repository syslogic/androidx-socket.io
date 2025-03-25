package io.syslogic.socketio.dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.syslogic.socketio.Constants;
import io.syslogic.socketio.databinding.DialogSocketsBinding;
import io.syslogic.socketio.model.ClientSocket;
import io.syslogic.socketio.adapter.SocketAdapter;

/**
 * Sockets {@link BaseDialogFragment}
 * @author Martin Zeitler
 */
public class SocketsDialogFragment extends BaseDialogFragment {

    private static final String LOG_TAG = SocketsDialogFragment.class.getSimpleName();
    private DialogSocketsBinding mDataBinding = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Socket socket = getSocket();
            socket.on(Constants.REQUEST_KEY_SOCKETS, this.onSockets);
            if (!socket.connected()) {socket.connect();}
            socket.emit("sockets");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.setDataBinding(inflater, container);
        this.mDataBinding.recyclerviewSockets.setLayoutManager(new LinearLayoutManager(requireContext()));
        this.mDataBinding.recyclerviewSockets.setAdapter(new SocketAdapter());
        return this.mDataBinding.getRoot();
    }

    protected final Emitter.Listener onSockets = args -> {
        JSONObject data = (JSONObject) args[0];
        try {

            String usercount = data.getString("usercount");
            JSONArray sockets = data.getJSONArray("data");
            ArrayList<ClientSocket> items = this.parseSockets(sockets);
            Log.d(LOG_TAG, usercount + " sockets active");

            // remove the "sockets" emitter again.
            getSocket().off(Constants.REQUEST_KEY_USER_LOGIN, this.onSockets);

            requireMainActivity().runOnUiThread(() -> {
                this.getDataBinding().recyclerviewSockets.setAdapter(new SocketAdapter(items));
                this.getDataBinding().executePendingBindings();
            });

        } catch (JSONException e) {
            Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
        }
    };

    @Override
    protected void setDataBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        this.mDataBinding = DialogSocketsBinding.inflate(inflater, container, false);
    }

    /** @noinspection unused */
    @NonNull
    @Override
    public DialogSocketsBinding getDataBinding() {
        return this.mDataBinding;
    }
}
