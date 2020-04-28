package io.syslogic.socketio;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private ArrayList<ChatMessage> mMessages;

    ChatAdapter(ArrayList<ChatMessage> messages) {
        mMessages = messages;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = null;
        switch (viewType) {
            case ChatMessage.TYPE_MESSAGE: {
                binding = DataBindingUtil.inflate(inflater, R.layout.cardview_message, parent, false);
                break;
            }
            case ChatMessage.TYPE_ACTION: {
                binding = DataBindingUtil.inflate(inflater, R.layout.cardview_action, parent, false);
                break;
            }
            case ChatMessage.TYPE_LOG: {
                binding = DataBindingUtil.inflate(inflater, R.layout.cardview_log, parent, false);
                break;
            }
        }
        assert binding != null;
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        ChatMessage item = mMessages.get(position);
        viewHolder.bind(item);
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessages.get(position).getType();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding mDataBinding;

        ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.mDataBinding = binding;
        }

        void bind(ChatMessage item) {
            this.mDataBinding.setVariable(BR.message, item);
            this.mDataBinding.executePendingBindings();
        }
    }
}
