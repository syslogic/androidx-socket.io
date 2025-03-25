package io.syslogic.socketio.recyclerview;

// import static io.syslogic.socketio.model.ChatUser.TYPE_DEFAULT;
import static io.syslogic.socketio.model.ClientSocket.TYPE_OPERATOR;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.syslogic.socketio.databinding.CardviewSocketBinding;
import io.syslogic.socketio.databinding.CardviewSocketOpBinding;
import io.syslogic.socketio.model.ClientSocket;

/**
 * Socket {@link RecyclerView.Adapter}
 * @author Martin Zeitler
 */
public class SocketAdapter extends RecyclerView.Adapter<SocketAdapter.ViewHolder> {

    private ArrayList<ClientSocket> mItems = new ArrayList<>();
    public SocketAdapter() {}
    public SocketAdapter(@NonNull ArrayList<ClientSocket> items) {
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
                new ViewHolder(CardviewSocketOpBinding.inflate(inflater, parent, false)):
                new ViewHolder(CardviewSocketBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ClientSocket item = getItem(position);
        viewHolder.bind(item);
    }

    private ClientSocket getItem(int position) {
        return mItems.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ViewDataBinding mDataBinding;

        ViewHolder(@NonNull ViewDataBinding binding) {
            super(binding.getRoot());
            this.mDataBinding = binding;
        }

        void bind(ClientSocket item) {
            if (this.mDataBinding instanceof CardviewSocketBinding binding) {
                binding.setItem(item);
            } else if (this.mDataBinding instanceof CardviewSocketOpBinding binding) {
                binding.setItem(item);
            }
            this.mDataBinding.executePendingBindings();
        }
    }
}
