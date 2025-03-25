package io.syslogic.socketio.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;

import io.syslogic.socketio.databinding.DialogSocketsBinding;
import io.syslogic.socketio.model.ClientSocket;
import io.syslogic.socketio.recyclerview.SocketAdapter;

/**
 * Sockets {@link BaseDialogFragment}
 * @author Martin Zeitler
 */
public class SocketsDialogFragment extends BaseDialogFragment {

    private static final String LOG_TAG = SocketsDialogFragment.class.getSimpleName();
    private DialogSocketsBinding mDataBinding = null;
    private final ArrayList<ClientSocket> mItems = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.setDataBinding(inflater, container);
        this.mDataBinding.recyclerviewSockets.setLayoutManager(new LinearLayoutManager(requireContext()));
        this.mDataBinding.recyclerviewSockets.setAdapter(new SocketAdapter());
        return this.mDataBinding.getRoot();
    }

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
