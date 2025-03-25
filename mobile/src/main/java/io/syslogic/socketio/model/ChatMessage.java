package io.syslogic.socketio.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class ChatMessage extends BaseObservable {

    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_LOG     = 1;
    public static final int TYPE_ACTION  = 2;

    private Integer mType;
    private String mMessage;
    private String mUsername;

    ChatMessage() {}

    public ChatMessage(Integer type, String message, String name) {
        mType = type;
        mMessage = message;
        mUsername = name;
    }

    public Integer getType() {
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

    /** Builder */
    public static class Builder {

        private Integer messageType;
        private String username;
        private String message;

        public Builder(Integer messageType) {
            this.messageType = messageType;
        }

        public Builder setMessageType(Integer value) {
            this.messageType = value;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public ChatMessage build() {
            return new ChatMessage(this.messageType, this.message,  this.username);
        }
    }
}
