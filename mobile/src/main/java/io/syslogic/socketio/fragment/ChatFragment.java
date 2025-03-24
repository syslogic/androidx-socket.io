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
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import io.syslogic.socketio.recyclerview.ChatAdapter;

public class ChatFragment extends BaseFragment {
    private static final String LOG_TAG = ChatFragment.class.getSimpleName();
    private FragmentChatBinding mDataBinding = null;
    private static final int TYPING_TIMER_LENGTH = 600;
    private final ArrayList<ChatMessage> mMessages = new ArrayList<>();
    private final Handler mTypingHandler = new Handler(Looper.getMainLooper());
    private RecyclerView.Adapter<?> mAdapter;
    private boolean mTyping = false;
    private int[] mUsernameColors;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mAdapter = new ChatAdapter(this.mMessages);
        mUsernameColors = context.getResources().getIntArray(R.array.username_colors);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (requireActivity() instanceof MainActivity activity) {
            activity.addChatMenuProvider();
        }
    }

    @Override
    public void onDestroy() {
        mSocket.off(Socket.EVENT_CONNECT, this.onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, this.onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, this.onConnectError);
        mSocket.off("new message", onNewMessage);
        mSocket.off("user joined", onUserJoined);
        mSocket.off("user left", onUserLeft);
        mSocket.off("typing", onTyping);
        mSocket.off("stop typing", onStopTyping);
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        this.mDataBinding = FragmentChatBinding.inflate(inflater, parent, false);

        this.mDataBinding.messageInput.setOnEditorActionListener((view, id, event) -> {
            if (id == R.id.send_button || id == EditorInfo.IME_NULL) {
                attemptSend();
                return true;
            }
            return false;
        });

        this.mDataBinding.messageInput.addTextChangedListener(new TextWatcher() {
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
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        this.mDataBinding.sendButton.setOnClickListener(view -> attemptSend());

        return this.mDataBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.mDataBinding.recyclerviewMessages.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.mDataBinding.recyclerviewMessages.setAdapter(mAdapter);

        if (savedInstanceState == null) {
            Bundle args = requireArguments();
            String socketId = args.getString("socketId", null);
            String username = args.getString("username", null);
            int userCount = args.getInt("userCount", 0);

            ChatRoom item = new ChatRoom.Builder()
                    .socketId(socketId)
                    .username(username)
                    .userCount(userCount)
                    .build();

            this.mDataBinding.setItem(item);
        }

        if (requireActivity() instanceof MainActivity activity) {

            mSocket = activity.getSocket();
            mSocket.on(Socket.EVENT_CONNECT, this.onConnect);
            mSocket.on(Socket.EVENT_DISCONNECT, this.onDisconnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, this.onConnectError);
            mSocket.on("new message", onNewMessage);
            mSocket.on("user joined", onUserJoined);
            mSocket.on("user left", onUserLeft);
            mSocket.on("typing", onTyping);
            mSocket.on("stop typing", onStopTyping);
            if (! mSocket.connected()) {mSocket.connect();}

            if (activity.getUserName() == null) {
                startSignIn(activity);
            } else {
                // addLog(getResources().getString(R.string.message_welcome));
                addUserCount(getUserCount());
                this.mDataBinding.messageInput.requestFocus();
            }
        }
    }

    private int getUserCount() {
        // return this.mDataBinding.getItem().getUserCount();
        if (requireActivity() instanceof MainActivity activity) {
            return activity.getUserCount();
        } else {
            return 0;
        }
    }

    private void addUserCount(int numUsers) {
        String message = getResources().getQuantityString(R.plurals.message_participants, numUsers, numUsers);
        addLog(message);
    }

    @SuppressWarnings("unused")
    public int getUsernameColor(@NonNull String username) {
        int hash = 7;
        for (int i = 0, len = username.length(); i < len; i++) {
            hash = username.codePointAt(i) + (hash << 5) - hash;
        }
        int index = Math.abs(hash % mUsernameColors.length);
        return mUsernameColors[index];
    }

    private void addItem(ChatMessage item) {
        this.mMessages.add(item);
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addLog(String message) {
        addItem(new ChatMessage.Builder(ChatMessage.TYPE_LOG).message(message).build());
    }

    private void addMessage(String username, String message) {
        addItem(new ChatMessage.Builder(ChatMessage.TYPE_MESSAGE).username(username).message(message).build());
    }

    private void addTyping(String username) {
        addItem(new ChatMessage.Builder(ChatMessage.TYPE_ACTION).username(username).build());
    }

    private void removeTyping(String username) {
        for (int i = this.mMessages.size() - 1; i >= 0; i--) {
            ChatMessage message = mMessages.get(i);
            if (message.getType() == ChatMessage.TYPE_ACTION && message.getUsername().equals(username)) {
                this.mMessages.remove(i);
                mAdapter.notifyItemRemoved(i);
            }
        }
    }

    private void attemptSend() {

        if (requireActivity() instanceof MainActivity activity) {

            if (activity.getUserName() == null) {return;}
            if (!mSocket.connected()) {return;}

            mTyping = false;

            String message = Objects.requireNonNull(this.mDataBinding.messageInput.getText()).toString().trim();
            if (TextUtils.isEmpty(message)) {
                this.mDataBinding.messageInput.requestFocus();
                return;
            }

            this.mDataBinding.messageInput.setText("");
            addMessage(activity.getUserName(), message);

            // perform the sending message attempt
            mSocket.emit("new message", message);
        }
    }

    private void startSignIn(@NonNull MainActivity activity) {
        activity.setUserName(null);
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

    private final Emitter.Listener onNewMessage = args -> {
        requireActivity().runOnUiThread(() -> {
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
    };

    private final Emitter.Listener onUserJoined = args -> {
        requireActivity().runOnUiThread(() -> {
            JSONObject data = (JSONObject) args[0];
            String username;
            int numUsers;
            try {
                username = data.getString("username");
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
                return;
            }
            addLog(getResources().getString(R.string.message_user_joined, username));
            addUserCount(numUsers);
        });
    };

    private final Emitter.Listener onUserLeft = args -> {
        requireActivity().runOnUiThread(() -> {
            JSONObject data = (JSONObject) args[0];
            String username;
            int numUsers;
            try {
                username = data.getString("username");
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
                return;
            }
            addLog(getResources().getString(R.string.message_user_left, username));
            removeTyping(username);
            addUserCount(numUsers);
        });
    };

    private final Emitter.Listener onTyping = args -> {
        requireActivity().runOnUiThread(() -> {
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
    };

    private final Emitter.Listener onStopTyping = args -> {
        requireActivity().runOnUiThread(() -> {
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
    };

    private final Runnable onTypingTimeout = () -> {
        if (! mTyping) {return;}
        mTyping = false;
        mSocket.emit("stop typing");
    };
}
