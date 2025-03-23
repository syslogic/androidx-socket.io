package io.syslogic.socketio;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;
import io.syslogic.socketio.databinding.FragmentChatBinding;

public class ChatFragment extends Fragment {
    private static final String LOG_TAG = ChatFragment.class.getSimpleName();
    private FragmentChatBinding mDataBinding = null;
    private static final int TYPING_TIMER_LENGTH = 600;
    private final ArrayList<ChatMessage> mMessages = new ArrayList<>();
    private final Handler mTypingHandler = new Handler(Looper.getMainLooper());
    private RecyclerView.Adapter<?> mAdapter;
    private Boolean isConnected = true; // skip the initial "connected" message
    private boolean mTyping = false;
    private String mUsername;
    private Socket mSocket;
    private int[] mUsernameColors;

    public ChatFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mAdapter = new ChatAdapter(mMessages);
        mUsernameColors = context.getResources().getIntArray(R.array.username_colors);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().addMenuProvider(new ChatMenuProvider());
        if (savedInstanceState == null && requireActivity() instanceof MainActivity activity) {
            mSocket = activity.getSocket();
            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on("new message", onNewMessage);
            mSocket.on("user joined", onUserJoined);
            mSocket.on("user left", onUserLeft);
            mSocket.on("typing", onTyping);
            mSocket.on("stop typing", onStopTyping);
            mSocket.connect();
        }
    }

    @Override
    public void onDestroy() {
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off("new message", onNewMessage);
        mSocket.off("user joined", onUserJoined);
        mSocket.off("user left", onUserLeft);
        mSocket.off("typing", onTyping);
        mSocket.off("stop typing", onStopTyping);
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        this.mDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, parent, false);
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
                if (null == mUsername) {return;}
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
        this.mDataBinding.messages.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.mDataBinding.messages.setAdapter(mAdapter);
        if (requireActivity() instanceof MainActivity activity) {
            if (activity.getUserName() == null) {
                startSignIn();
            } else {
                // addLog(getResources().getString(R.string.message_welcome));
                updateUserCount(activity.getUserCount());
                this.mDataBinding.messageInput.requestFocus();
            }
        }
    }

    private void addLog(String message) {
        mMessages.add(new ChatMessage.Builder(ChatMessage.TYPE_LOG).message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void updateUserCount(int numUsers) {
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

    private void addMessage(String username, String message) {
        mMessages.add(new ChatMessage.Builder(ChatMessage.TYPE_MESSAGE).username(username).message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void addTyping(String username) {
        mMessages.add(new ChatMessage.Builder(ChatMessage.TYPE_ACTION).username(username).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void removeTyping(String username) {
        for (int i = mMessages.size() - 1; i >= 0; i--) {
            ChatMessage message = mMessages.get(i);
            if (message.getType() == ChatMessage.TYPE_ACTION && message.getUsername().equals(username)) {
                mMessages.remove(i);
                mAdapter.notifyItemRemoved(i);
            }
        }
    }

    private void attemptSend() {

        if (mUsername == null && getActivity() != null) {
            mUsername = ((MainActivity) getActivity()).getUserName();
        }
        if (mUsername == null) {return;}
        if (!mSocket.connected()) {return;}

        mTyping = false;

        String message = Objects.requireNonNull(this.mDataBinding.messageInput.getText()).toString().trim();
        if (TextUtils.isEmpty(message)) {
            this.mDataBinding.messageInput.requestFocus();
            return;
        }

        this.mDataBinding.messageInput.setText("");
        addMessage(mUsername, message);

        // perform the sending message attempt
        mSocket.emit("new message", message);
    }

    private void startSignIn() {
        mUsername = null;
        if (getView() != null) {
            Navigation.findNavController(getView()).navigate(R.id.action_chatFragment_to_loginFragment);
        }
    }

    /** TODO ... */
    public void leaveRoom() {
        mUsername = null;
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.connect();
        }
        startSignIn();
    }

    private void scrollToBottom() {
        this.mDataBinding.messages.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private final Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            requireActivity().runOnUiThread(() -> {
                if(! isConnected) {
                    isConnected = true;
                }
                if (mUsername != null) {
                    mSocket.emit("add user", mUsername);
                }
            });
        }
    };

    private final Emitter.Listener onDisconnect = args -> {
        requireActivity().runOnUiThread(() -> isConnected = false);
    };

    /** On {@link EngineIOException} */
    private final Emitter.Listener onConnectError = args -> {
        final EngineIOException e = (EngineIOException) args[0];
        requireActivity().runOnUiThread(() -> {
            Throwable cause = Objects.requireNonNull(e.getCause());
            String message = Objects.requireNonNull(cause.getMessage());
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "Error connecting to \"" + getString(R.string.server_hostname) + "\".");
            Log.e(LOG_TAG, message);
        });
    };

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
            updateUserCount(numUsers);
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
            updateUserCount(numUsers);
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

    private final Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (! mTyping) {return;}
            mTyping = false;
            mSocket.emit("stop typing");
        }
    };
}
