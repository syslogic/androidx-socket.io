package io.syslogic.socketio.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.syslogic.socketio.databinding.CardviewActionBinding;
import io.syslogic.socketio.databinding.CardviewLogBinding;
import io.syslogic.socketio.databinding.CardviewMessageBinding;
import io.syslogic.socketio.model.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private final ArrayList<ChatMessage> mItems;

    public ChatAdapter(Context context, @NonNull ArrayList<ChatMessage> items) {
        mItems = items;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return switch (viewType) {
            case ChatMessage.TYPE_LOG ->
                    new ViewHolder(CardviewLogBinding.inflate(inflater, parent, false));
            case ChatMessage.TYPE_ACTION ->
                    new ViewHolder(CardviewActionBinding.inflate(inflater, parent, false));
            /* case ChatMessage.TYPE_MESSAGE */ default ->
                    new ViewHolder(CardviewMessageBinding.inflate(inflater, parent, false));
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ChatMessage item = getItem(position);
        viewHolder.bind(item);
    }

    private ChatMessage getItem(int position) {
        return mItems.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ViewDataBinding mDataBinding;

        ViewHolder(@NonNull ViewDataBinding binding) {
            super(binding.getRoot());
            this.mDataBinding = binding;
        }

        void bind(ChatMessage item) {
            if (this.mDataBinding instanceof CardviewMessageBinding binding) {
                binding.setItem(item);
            } else if (this.mDataBinding instanceof CardviewLogBinding binding) {
                binding.setItem(item);
            } else if (this.mDataBinding instanceof CardviewActionBinding binding) {
                binding.setItem(item);
            }
            this.mDataBinding.executePendingBindings();
        }
    }
}
