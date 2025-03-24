package io.syslogic.socketio.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class ChatMessage extends BaseObservable {

    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_LOG     = 1;
    public static final int TYPE_ACTION  = 2;

    private int mType;
    private String mMessage;
    private String mUsername;

    ChatMessage() {}

    public ChatMessage(int type, String message, String name) {
        mType = type;
        mMessage = message;
        mUsername = name;
    }

    public int getType() {
        return this.mType;
    }

    @Bindable
    public String getMessage() {
        return this.mMessage;
    }

    @Bindable
    public String getUsername() {
        return this.mUsername;
    }

    public static class Builder {

        private final int mType;
        private String mUsername;
        private String mMessage;

        public Builder(int type) {
            mType = type;
        }

        public Builder username(String username) {
            this.mUsername = username;
            return this;
        }

        public Builder message(String message) {
            this.mMessage = message;
            return this;
        }

        public ChatMessage build() {
            ChatMessage message = new ChatMessage();
            message.mType = this.mType;
            message.mUsername = this.mUsername;
            message.mMessage = this.mMessage;
            return message;
        }
    }
}
