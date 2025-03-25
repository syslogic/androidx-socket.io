package io.syslogic.socketio.fragment;

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
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import io.syslogic.socketio.Constants;
import io.syslogic.socketio.R;
import io.syslogic.socketio.MainActivity;
import io.syslogic.socketio.databinding.FragmentChatBinding;
import io.syslogic.socketio.model.ChatMessage;
import io.syslogic.socketio.model.ChatRoom;
import io.syslogic.socketio.adapter.MessageAdapter;

/**
 * Chat {@link BaseFragment}
 * @author Martin Zeitler
 */
public class ChatFragment extends BaseFragment implements FragmentResultListener {
    private static final String LOG_TAG = ChatFragment.class.getSimpleName();
    private FragmentChatBinding mDataBinding = null;
    private static final int TYPING_TIMER_DURATION = 600;
    private final Handler mTypingHandler = new Handler(Looper.getMainLooper());
    private boolean mTyping = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity activity = requireMainActivity();
        this.addFragmentResultListener(Constants.REQUEST_KEY_USER_LOGIN, this, true);
        activity.addChatMenuProvider();
        if (savedInstanceState == null) {
            mSocket = activity.getSocket();
            mSocket.on(Socket.EVENT_CONNECT, this.onConnect);
            mSocket.on(Socket.EVENT_DISCONNECT, this.onDisconnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, this.onConnectError);
            mSocket.on(Constants.REQUEST_KEY_CHAT_MESSAGE, this.onChatMessage);
            mSocket.on(Constants.REQUEST_KEY_DIRECT_MESSAGE, this.onDirectMessage);
            mSocket.on(Constants.REQUEST_KEY_USER_JOINED, this.onUserJoined);
            mSocket.on(Constants.REQUEST_KEY_USER_LEFT, this.onUserLeft);
            mSocket.on(Constants.REQUEST_KEY_TYPING_START, this.onTyping);
            mSocket.on(Constants.REQUEST_KEY_TYPING_STOP, this.onStopTyping);
            if (!mSocket.connected()) {mSocket.connect();}
        }
    }

    @Override
    public void onDestroy() {
        this.addFragmentResultListener("login", this, false);
        mSocket.off(Socket.EVENT_CONNECT, this.onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, this.onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, this.onConnectError);
        mSocket.off(Constants.REQUEST_KEY_CHAT_MESSAGE, this.onChatMessage);
        mSocket.off(Constants.REQUEST_KEY_DIRECT_MESSAGE, this.onDirectMessage);
        mSocket.off(Constants.REQUEST_KEY_USER_JOINED, this.onUserJoined);
        mSocket.off(Constants.REQUEST_KEY_USER_LEFT, this.onUserLeft);
        mSocket.off(Constants.REQUEST_KEY_TYPING_START, this.onTyping);
        mSocket.off(Constants.REQUEST_KEY_TYPING_STOP, this.onStopTyping);
        leaveRoom(requireMainActivity());
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.setDataBinding(inflater, container);
        if (requireActivity() instanceof MainActivity activity) {
            activity.setSupportActionBar(this.getDataBinding().toolbarChat);
        }

        this.mDataBinding.recyclerviewMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        this.mDataBinding.recyclerviewMessages.setAdapter(new MessageAdapter());

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
                    mSocket.emit(Constants.REQUEST_KEY_TYPING_STOP);
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
        getRecyclerViewAdapter().addItem(item);
        scrollToBottom();
    }

    private MessageAdapter getRecyclerViewAdapter() {
        return ((MessageAdapter) this.mDataBinding.recyclerviewMessages.getAdapter());
    }

    private void addLog(String message) {
        addItem(new ChatMessage.Builder(ChatMessage.TYPE_LOG).setMessage(message).build());
    }

    private void addMessage(String username, String message) {
        addItem(new ChatMessage.Builder(ChatMessage.TYPE_MESSAGE).setUsername(username).setMessage(message).build());
    }
    private void addDirect(String username, String message) {
        addItem(new ChatMessage.Builder(ChatMessage.TYPE_DIRECT).setUsername(username).setMessage(message).build());
    }

    private void addTyping(String username) {
        addItem(new ChatMessage.Builder(ChatMessage.TYPE_ACTION).setUsername(username).build());
    }

    private void removeTyping(String username) {
        MessageAdapter adapter = getRecyclerViewAdapter();
        for (int i = adapter.getItemCount() - 1; i >= 0; i--) {
            ChatMessage message = adapter.getItemAt(i);
            if (message.getType() == ChatMessage.TYPE_ACTION && message.getUsername().equals(username)) {
                adapter.removeItem(i);
            }
        }
    }

    private void attemptSend() {
        mTyping = false;
        if (!mSocket.connected()) {return;}
        String message = Objects.requireNonNull(this.mDataBinding.inputMessage.getText()).toString().trim();
        if (TextUtils.isEmpty(message)) {
            this.mDataBinding.inputMessage.requestFocus();
            return;
        }

        // perform the sending message attempt
        mSocket.emit(Constants.REQUEST_KEY_CHAT_MESSAGE, message);
        addMessage(this.mDataBinding.getItem().getUsername(), message);
        this.mDataBinding.inputMessage.setText("");
    }

    private void startSignIn(@NonNull MainActivity activity) {
        activity.getNavController().navigateUp();
    }

    public void leaveRoom(@NonNull MainActivity activity) {
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.connect();
        }
        startSignIn(activity);
    }

    private void scrollToBottom() {
        this.mDataBinding.recyclerviewMessages
                .scrollToPosition(getRecyclerViewAdapter().getItemCount() - 1);
    }

    private final Emitter.Listener onChatMessage = args -> requireActivity().runOnUiThread(() -> {
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


    private final Emitter.Listener onDirectMessage = args -> requireActivity().runOnUiThread(() -> {
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
        addDirect(username, message);
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
        mSocket.emit(Constants.REQUEST_KEY_TYPING_STOP);
    };

    @Override
    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

    }

    @Override
    protected void setDataBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        this.mDataBinding = FragmentChatBinding.inflate(inflater, container, false);
    }

    @NonNull
    public FragmentChatBinding getDataBinding() {
        return this.mDataBinding;
    }

}
