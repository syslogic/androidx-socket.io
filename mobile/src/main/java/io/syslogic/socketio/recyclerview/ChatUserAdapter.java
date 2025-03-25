package io.syslogic.socketio.recyclerview;

// import static io.syslogic.socketio.model.ChatUser.TYPE_DEFAULT;
import static io.syslogic.socketio.model.ChatUser.TYPE_OPERATOR;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.syslogic.socketio.databinding.CardviewActionBinding;
import io.syslogic.socketio.databinding.CardviewMessageBinding;
import io.syslogic.socketio.databinding.CardviewUserBinding;
import io.syslogic.socketio.model.ChatUser;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ViewHolder> {

    private ArrayList<ChatUser> mItems = new ArrayList<>();
    public ChatUserAdapter() {}
    public ChatUserAdapter(@NonNull ArrayList<ChatUser> items) {
        this.mItems = items;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getUserType();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return viewType == TYPE_OPERATOR ?
                new ViewHolder(CardviewActionBinding.inflate(inflater, parent, false)):
                new ViewHolder(CardviewMessageBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ChatUser item = getItem(position);
        viewHolder.bind(item);
    }

    private ChatUser getItem(int position) {
        return mItems.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ViewDataBinding mDataBinding;

        ViewHolder(@NonNull ViewDataBinding binding) {
            super(binding.getRoot());
            this.mDataBinding = binding;
        }

        void bind(ChatUser item) {
            if (this.mDataBinding instanceof CardviewUserBinding binding) {
                binding.setItem(item);
            }
            this.mDataBinding.executePendingBindings();
        }
    }
}
