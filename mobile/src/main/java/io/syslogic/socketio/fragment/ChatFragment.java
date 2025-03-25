package io.syslogic.socketio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import io.syslogic.socketio.R;
import io.syslogic.socketio.activity.MainActivity;
import io.syslogic.socketio.databinding.FragmentChatBinding;
import io.syslogic.socketio.model.ChatMessage;
import io.syslogic.socketio.model.ChatRoom;
import io.syslogic.socketio.recyclerview.ChatMessageAdapter;

public class ChatFragment extends BaseFragment {
    private static final String LOG_TAG = ChatFragment.class.getSimpleName();
    private FragmentChatBinding mDataBinding = null;
    private final ArrayList<ChatMessage> mItems = new ArrayList<>();
    private ChatMessageAdapter mAdapter;
    private static final int TYPING_TIMER_DURATION = 600;
    private final Handler mTypingHandler = new Handler(Looper.getMainLooper());
    private boolean mTyping = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (requireActivity() instanceof MainActivity activity) {
            activity.addChatMenuProvider();
            if (savedInstanceState == null) {
                mSocket = activity.getSocket();
                mSocket.on(Socket.EVENT_CONNECT, this.onConnect);
                mSocket.on(Socket.EVENT_DISCONNECT, this.onDisconnect);
                mSocket.on(Socket.EVENT_CONNECT_ERROR, this.onConnectError);
                mSocket.on("chat message", this.onNewMessage);
                mSocket.on("direct message", this.onPrivateMessage);
                mSocket.on("user joined", this.onUserJoined);
                mSocket.on("user left", this.onUserLeft);
                mSocket.on("typing", this.onTyping);
                mSocket.on("stop typing", this.onStopTyping);
                if (!mSocket.connected()) {mSocket.connect();}
            }
        }
    }

    @Override
    public void onDestroy() {
        mSocket.off(Socket.EVENT_CONNECT, this.onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, this.onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, this.onConnectError);
        mSocket.off("chat message", this.onNewMessage);
        mSocket.off("direct message", this.onPrivateMessage);
        mSocket.off("user joined", this.onUserJoined);
        mSocket.off("user left", this.onUserLeft);
        mSocket.off("typing", this.onTyping);
        mSocket.off("stop typing", this.onStopTyping);

        if (requireActivity() instanceof MainActivity activity) {
            leaveRoom(activity);
        }
        super.onDestroy();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mAdapter = new ChatMessageAdapter(this.mItems);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        this.mDataBinding = FragmentChatBinding.inflate(inflater, parent, false);
        if (requireActivity() instanceof MainActivity activity) {
            activity.setSupportActionBar(this.getDataBinding().toolbarChat);
        }

        this.mDataBinding.recyclerviewMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        this.mDataBinding.recyclerviewMessages.setAdapter(this.mAdapter);

        this.mDataBinding.buttonSend.setOnClickListener(view -> {
            attemptSend();
        });

        this.mDataBinding.inputMessage.setOnEditorActionListener((view, id, event) -> {
            if (id == R.id.button_send /* || id == EditorInfo.IME_NULL */) {
                attemptSend();
                return true;
            }
            return false;
        });

        this.mDataBinding.inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mSocket.connected()) {return;}
                if (!mTyping) {
                    mTyping = true;
                    mSocket.emit("typing");
                }
                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_DURATION);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        return this.mDataBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        String socketId = args.getString("socketId", null);
        String username = args.getString("username", null);
        int usercount = args.getInt("usercount", 0);
        this.mDataBinding.toolbarChat.setSubtitle(socketId);

        this.mDataBinding.setItem(
                new ChatRoom.Builder()
                    .setSocketId(socketId)
                    .setUsername(username)
                    .setUsercount(usercount)
                    .build()
        );

        // addLog(getResources().getString(R.string.message_welcome));
        this.addUserCount(usercount);
        this.mDataBinding.inputMessage.requestFocus();
    }

    private void addUserCount(int numUsers) {
        String message = getResources().getQuantityString(R.plurals.message_participants, numUsers, numUsers);
        addLog(message);
    }

    private void addItem(ChatMessage item) {
        this.mItems.add(item);
        mAdapter.notifyItemInserted(this.mItems.size() - 1);
        scrollToBottom();
    }

    private void addLog(String message) {
        addItem(new ChatMessage.Builder(ChatMessage.TYPE_LOG).setMessage(message).build());
    }

    private void addMessage(String username, String message) {
        addItem(new ChatMessage.Builder(ChatMessage.TYPE_MESSAGE).setUsername(username).setMessage(message).build());
    }

    private void addTyping(String username) {
        addItem(new ChatMessage.Builder(ChatMessage.TYPE_ACTION).setUsername(username).build());
    }

    private void removeTyping(String username) {
        for (int i = this.mItems.size() - 1; i >= 0; i--) {
            ChatMessage message = this.mItems.get(i);
            if (message.getType() == ChatMessage.TYPE_ACTION && message.getUsername().equals(username)) {
                this.mItems.remove(i);
                mAdapter.notifyItemRemoved(i);
            }
        }
    }

    private void attemptSend() {

        mTyping = false;
        if (!mSocket.connected()) {return;}
        Log.d(LOG_TAG, "attempt message send");
        String message = Objects.requireNonNull(this.mDataBinding.inputMessage.getText()).toString().trim();
        if (TextUtils.isEmpty(message)) {
            this.mDataBinding.inputMessage.requestFocus();
            return;
        }

        // perform the sending message attempt
        addMessage(this.mDataBinding.getItem().getUsername(), message);
        mSocket.emit("chat message", message);

        this.mDataBinding.inputMessage.setText("");
    }

    private void startSignIn(@NonNull MainActivity activity) {
        activity.getNavController().navigateUp();
    }

    /** TODO ... */
    public void leaveRoom(@NonNull MainActivity activity) {
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.connect();
        }
        startSignIn(activity);
    }

    private void scrollToBottom() {
        this.mDataBinding.recyclerviewMessages.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private final Emitter.Listener onNewMessage = args -> requireActivity().runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        String username;
        String message;
        try {
            username = data.getString("username");
            message = data.getString("message");
        } catch (JSONException e) {
            Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
            return;
        }
        removeTyping(username);
        addMessage(username, message);
    });

    private final Emitter.Listener onPrivateMessage = args -> requireActivity().runOnUiThread(() -> {

    });

    private final Emitter.Listener onUserJoined = args -> requireActivity().runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        String username;
        int usercount;
        try {
            username = data.getString("username");
            usercount = data.getInt("usercount");
        } catch (JSONException e) {
            Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
            return;
        }
        addLog(getResources().getString(R.string.message_user_joined, username));
        addUserCount(usercount);
    });

    private final Emitter.Listener onUserLeft = args -> requireActivity().runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        String username;
        int usercount;
        try {
            username = data.getString("username");
            usercount = data.getInt("usercount");
        } catch (JSONException e) {
            Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
            return;
        }
        addLog(getResources().getString(R.string.message_user_left, username));
        removeTyping(username);
        addUserCount(usercount);
    });

    private final Emitter.Listener onTyping = args -> requireActivity().runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        String username;
        try {
            username = data.getString("username");
        } catch (JSONException e) {
            Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
            return;
        }
        addTyping(username);
    });

    private final Emitter.Listener onStopTyping = args -> requireActivity().runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        String username;
        try {
            username = data.getString("username");
        } catch (JSONException e) {
            Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
            return;
        }
        removeTyping(username);
    });

    private final Runnable onTypingTimeout = () -> {
        if (! mTyping) {return;}
        mTyping = false;
        mSocket.emit("stop typing");
    };

    /** @noinspection unused */
    public FragmentChatBinding getDataBinding() {
        return this.mDataBinding;
    }
}
