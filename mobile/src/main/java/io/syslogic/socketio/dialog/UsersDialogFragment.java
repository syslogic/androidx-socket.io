package io.syslogic.socketio.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;

import io.syslogic.socketio.databinding.DialogUsersBinding;
import io.syslogic.socketio.model.ChatUser;
import io.syslogic.socketio.recyclerview.ChatUserAdapter;

public class UsersDialogFragment extends DialogFragment {

    private static final String LOG_TAG = UsersDialogFragment.class.getSimpleName();
    private DialogUsersBinding mDataBinding = null;
    private final ArrayList<ChatUser> mItems = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        this.mDataBinding = DialogUsersBinding.inflate(inflater, parent, false);
        this.mDataBinding.recyclerviewUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        this.mDataBinding.recyclerviewUsers.setAdapter(new ChatUserAdapter());
        return this.mDataBinding.getRoot();
    }

    /** @noinspection unused */
    public DialogUsersBinding getDataBinding() {
        return this.mDataBinding;
    }
}
