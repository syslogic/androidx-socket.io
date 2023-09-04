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

    ChatAdapter(@NonNull ArrayList<ChatMessage> messages) {
        mMessages = messages;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resId = 0;
        switch (viewType) {
            case ChatMessage.TYPE_MESSAGE: {resId = R.layout.cardview_message; break;}
            case ChatMessage.TYPE_ACTION:  {resId = R.layout.cardview_action; break;}
            case ChatMessage.TYPE_LOG: {resId = R.layout.cardview_log; break;}
        }
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, resId, parent, false);
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

        ViewHolder(@NonNull ViewDataBinding binding) {
            super(binding.getRoot());
            this.mDataBinding = binding;
        }

        void bind(ChatMessage item) {
            this.mDataBinding.setVariable(BR.message, item);
            this.mDataBinding.executePendingBindings();
        }
    }
}
